package com.busanit501.__team_back.service.user;
import com.busanit501.__team_back.dto.ai.AiResponse;
import com.busanit501.__team_back.dto.analysis.AnalysisHistoryDTO;
import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import com.busanit501.__team_back.dto.analysis.NutritionData;
import com.busanit501.__team_back.dto.analysis.YoutubeRecipeDTO;
import com.busanit501.__team_back.entity.MongoDB.AnalysisHistory;
import com.busanit501.__team_back.entity.MongoDB.FoodAnalysisData;
import com.busanit501.__team_back.entity.MongoDB.FoodReference;
import com.busanit501.__team_back.repository.mongo.AnalysisHistoryRepository;
import com.busanit501.__team_back.repository.mongo.FoodAnalysisDataRepository;
import com.busanit501.__team_back.repository.mongo.FoodReferenceRepository;
import com.busanit501.__team_back.service.ai.AIAnalysisService;
import com.busanit501.__team_back.service.api.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.bson.types.Binary;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// 수정된 부분은 주석으로 //으로 표시함.
@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class AnalysisServiceImpl implements AnalysisService {

    private final FoodAnalysisDataRepository foodAnalysisDataRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final FoodReferenceRepository foodReferenceRepository;
    private final AIAnalysisService aiAnalysisService;
    private final YoutubeApiService youtubeApiService;
    private final ModelMapper modelMapper;



    @Override
    public FoodAnalysisResultDTO analyzeImage(Long userId, MultipartFile image) {
        log.info("AnalysisService - analyzeImage 실행...");
        log.info("사용자 ID: {}", userId);
        log.info("이미지 파일: {}", image.getOriginalFilename());

        try {
            // [STEP 1] Flask AI 서버로 이미지 전송 및 결과 수신
            AiResponse aiResult = aiAnalysisService.analyzeImage(image);

            String foodName = aiResult.getPredictedClass();

            //  Flask-> confidence가 %단위 DB 저장을 위해 나누기100
            double accuracyForDB = aiResult.getConfidence() / 100.0;
            log.info("AI 분석 결과: {} (정확도: {}%)", foodName, aiResult.getConfidence());


            // [STEP 2] 인식된 음식 이름으로 FoodReference DB에서 영양 정보 조회 및 DTO 변환
            Optional<FoodReference> foodRefOptional = foodReferenceRepository.findByFoodName(foodName);
            NutritionData nutritionData = null;
            
            if (foodRefOptional.isPresent()) {
                var nutritionInfo = foodRefOptional.get().getNutritionInfo();
                // ModelMapper 대신 직접 변환 (carbohydrate -> carbohydrates 필드명 차이)
                nutritionData = NutritionData.builder()
                        .calories(nutritionInfo.getCalories())
                        .carbohydrates(nutritionInfo.getCarbohydrate()) // NutritionInfo의 carbohydrate를 carbohydrates로 매핑
                        .protein(nutritionInfo.getProtein())
                        .fat(nutritionInfo.getFat())
                        .build();
                log.info("{} 영양 정보 조회 성공", foodName);
            } else {
                log.warn("{} 영양 정보를 찾을 수 없습니다. DB에 영양 정보를 추가해주세요.", foodName);
            }

            // [STEP 3] 음식 이름으로 YouTube API 검색 (실패해도 계속 진행)
            List<YoutubeRecipeDTO> youtubeRecipes = Collections.emptyList();
            try {
                youtubeRecipes = youtubeApiService.searchRecipes(foodName);
                log.info("{} 관련 유튜브 레시피 검색 완료: {}개", foodName, youtubeRecipes.size());
            } catch (Exception e) {
                log.warn("YouTube API 검색 실패 (계속 진행): {}", e.getMessage());
                // YouTube API 실패해도 분석 결과는 반환
            }

//====================================================================
            // [STEP 4] 원본 이미지를 학습용 DB에 저장 (비동기 처리 고려)
                // 이 작업은 사용자 응답 시간에 영향을 주지 않도록 @Async 등을
                // 사용하여 비동기적으로 처리하는 것이 좋음. 우선 동기방식으로 만듦.
;
            try {
                FoodAnalysisData trainingData = FoodAnalysisData.builder()
                        .foodCategory(foodName) // AI가 분석한 음식 이름으로 카테고리 지정
                        .originalImageData(new Binary(image.getBytes())) // 원본 이미지 데이터 저장
                        .contentType(image.getContentType())
                        .createdAt(LocalDateTime.now())
                        .build();
                foodAnalysisDataRepository.save(trainingData);
                log.info("학습용 원본 이미지 저장 완료. Category: {}", foodName);
            } catch (IOException e) {
                log.error("학습용 이미지 저장 실패", e);
                // 이 작업이 실패하더라도 사용자에게 보내는 최종 분석 결과에는 영향을 주지 않도록
                // 여기서 예외를 잡아서 처리하고 계속 진행하는 것이 좋습니다.
            }

            // [STEP 5] 썸네일 생성 및 최종 분석 결과를 AnalysisHistory DB에 저장
            String savedHistoryId = null;
            try {
                // (1) 리사이징 수행: createThumbnail 헬퍼 메소드를 호출하여
                    // 사용자가 업로드한 원본 이미지(image)를 256x256 크기로 리사이즈합니다.
                byte[] thumbnailData = createThumbnail(image, 256);

                AnalysisHistory history = AnalysisHistory.builder()
                        .userId(userId)
                        .thumbnailImageData(new Binary(thumbnailData))
                        .thumbnailContentType(image.getContentType())
                        .recognizedFoodName(foodName)
                        .accuracy(accuracyForDB) // 0.0 ~ 1.0 사이 값으로 저장
                        .youtubeRecipes(Collections.emptyList()) // YouTube 레시피는 클릭 시에만 저장
                        .analysisDate(LocalDateTime.now())
                        .build();

                // (3) MongoDB에 최종 저장
                AnalysisHistory savedHistory = analysisHistoryRepository.save(history);
                savedHistoryId = savedHistory.getId();

                log.info("사용자 분석 기록 저장 완료. History ID: {}", savedHistoryId);
            } catch (IOException e) {
                log.error("썸네일 생성 실패 (분석 결과는 반환): {}", e.getMessage());
                // 썸네일 생성 실패해도 분석 결과는 반환
            } catch (Exception e) {
                log.error("분석 기록 저장 실패 (분석 결과는 반환): {}", e.getMessage());
                // MongoDB 저장 실패해도 분석 결과는 반환
            }
            //====================================================================


            // [STEP 6] 조회 및 변환된 모든 데이터를 최종 FoodAnalysisResultDTO에 담아 반환
            return FoodAnalysisResultDTO.builder()
                    .foodName(foodName)
                    .accuracy(aiResult.getConfidence())
                    .top3(aiResult.getTop3()) // 상위 3개 예측 결과 추가
                    .nutritionData(nutritionData)
                    .youtubeRecipes(youtubeRecipes)
                    .historyId(savedHistoryId) // 분석 이력 ID 추가
                    .message("분석 완료")
                    .build();

        } catch (Exception e) {
            log.error("이미지 분석 중 오류 발생", e);
            // 오류 발생 시에도 기본 정보는 담아서 반환
            return FoodAnalysisResultDTO.builder()
                    .foodName("N/A")
                    .accuracy(0.0)
                    .nutritionData(null)
                    .youtubeRecipes(List.of())
                    .message("분석 중 오류 발생: " + e.getMessage())
                    .build();
        }
    }
    // YouTube 검색 옵션을 포함한 이미지 분석 메서드
    @Override
    public FoodAnalysisResultDTO analyzeImage(Long userId, MultipartFile image, String youtubeKeyword, String youtubeOrder) {
        log.info("AnalysisService - analyzeImage 실행 (YouTube 옵션 포함)...");
        log.info("사용자 ID: {}, YouTube 키워드: {}, 정렬: {}", userId, youtubeKeyword, youtubeOrder);

        try {
            // [STEP 1] Flask AI 서버로 이미지 전송 및 결과 수신
            AiResponse aiResult = aiAnalysisService.analyzeImage(image);

            String foodName = aiResult.getPredictedClass();
            double accuracyForDB = aiResult.getConfidence() / 100.0;
            log.info("AI 분석 결과: {} (정확도: {}%)", foodName, aiResult.getConfidence());

            // [STEP 2] 인식된 음식 이름으로 FoodReference DB에서 영양 정보 조회 및 DTO 변환
            Optional<FoodReference> foodRefOptional = foodReferenceRepository.findByFoodName(foodName);
            NutritionData nutritionData = null;

            if (foodRefOptional.isPresent()) {
                var nutritionInfo = foodRefOptional.get().getNutritionInfo();
                // ModelMapper 대신 직접 변환 (carbohydrate -> carbohydrates 필드명 차이)
                nutritionData = NutritionData.builder()
                        .calories(nutritionInfo.getCalories())
                        .carbohydrates(nutritionInfo.getCarbohydrate()) // NutritionInfo의 carbohydrate를 carbohydrates로 매핑
                        .protein(nutritionInfo.getProtein())
                        .fat(nutritionInfo.getFat())
                        .build();
                log.info("{} 영양 정보 조회 성공", foodName);
            } else {
                log.warn("{} 영양 정보를 찾을 수 없습니다. DB에 영양 정보를 추가해주세요.", foodName);
            }

            // [STEP 3] 음식 이름과 사용자 키워드로 YouTube API 검색 (정렬 옵션 포함)
            List<YoutubeRecipeDTO> youtubeRecipes = Collections.emptyList();
            try {
                youtubeRecipes = youtubeApiService.searchRecipes(foodName, youtubeKeyword, youtubeOrder);
                log.info("{} 관련 유튜브 레시피 검색 완료: {}개 (키워드: {}, 정렬: {})",
                        foodName, youtubeRecipes.size(), youtubeKeyword, youtubeOrder);
            } catch (Exception e) {
                log.warn("YouTube API 검색 실패 (계속 진행): {}", e.getMessage());
                // YouTube API 실패해도 분석 결과는 반환
            }

            // [STEP 4] 원본 이미지를 학습용 DB에 저장
            try {
                FoodAnalysisData trainingData = FoodAnalysisData.builder()
                        .foodCategory(foodName)
                        .originalImageData(new Binary(image.getBytes()))
                        .contentType(image.getContentType())
                        .createdAt(LocalDateTime.now())
                        .build();
                foodAnalysisDataRepository.save(trainingData);
                log.info("학습용 원본 이미지 저장 완료. Category: {}", foodName);
            } catch (IOException e) {
                log.error("학습용 이미지 저장 실패", e);
            }

            // [STEP 5] 썸네일 생성 및 최종 분석 결과를 AnalysisHistory DB에 저장
            String savedHistoryId = null;
            try {
                // 썸네일 생성: 256x256 크기로 리사이즈
                byte[] thumbnailData = createThumbnail(image, 256);

                AnalysisHistory history = AnalysisHistory.builder()
                        .userId(userId)
                        .thumbnailImageData(new Binary(thumbnailData))
                        .thumbnailContentType(image.getContentType())
                        .recognizedFoodName(foodName)
                        .accuracy(accuracyForDB)
                        .youtubeRecipes(Collections.emptyList()) // YouTube 레시피는 클릭 시에만 저장
                        .analysisDate(LocalDateTime.now())
                        .build();

                AnalysisHistory savedHistory = analysisHistoryRepository.save(history);
                savedHistoryId = savedHistory.getId();
                log.info("사용자 분석 기록 저장 완료. History ID: {}", savedHistoryId);
            } catch (IOException e) {
                log.error("썸네일 생성 실패 (분석 결과는 반환): {}", e.getMessage());
            } catch (Exception e) {
                log.error("분석 기록 저장 실패 (분석 결과는 반환): {}", e.getMessage());
            }

            // [STEP 6] 조회 및 변환된 모든 데이터를 최종 FoodAnalysisResultDTO에 담아 반환
            // accuracy는 Flask에서 반환한 confidence 값 그대로 사용 (0~100 범위)
            return FoodAnalysisResultDTO.builder()
                    .foodName(foodName)
                    .accuracy(aiResult.getConfidence())
                    .top3(aiResult.getTop3()) // 상위 3개 예측 결과 추가
                    .nutritionData(nutritionData)
                    .youtubeRecipes(youtubeRecipes)
                    .historyId(savedHistoryId) // 분석 이력 ID 추가
                    .message("분석 완료")
                    .build();

        } catch (Exception e) {
            log.error("이미지 분석 중 오류 발생", e);
            return FoodAnalysisResultDTO.builder()
                    .foodName("N/A")
                    .accuracy(0.0)
                    .nutritionData(null)
                    .youtubeRecipes(List.of())
                    .message("분석 중 오류 발생: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public List<AnalysisHistoryDTO> getAnalysisHistory(Long userId, int page, int size) {
        log.info("사용자 {}의 분석 히스토리 조회 - 페이지: {}, 크기: {}", userId, page, size);
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<AnalysisHistory> historyPage = 
            analysisHistoryRepository.findByUserIdOrderByAnalysisDateDesc(userId, pageable);
        
        return historyPage.getContent().stream()
                .map(history -> {
                    // MongoDB에서 조회된 원본 데이터 확인
                    log.info("📦 MongoDB에서 조회된 히스토리 - ID: {}, 음식: {}", history.getId(), history.getRecognizedFoodName());
                    log.info("   youtubeRecipes 필드: {}", history.getYoutubeRecipes());
                    log.info("   youtubeRecipes null 여부: {}", history.getYoutubeRecipes() == null);
                    if (history.getYoutubeRecipes() != null) {
                        log.info("   youtubeRecipes 크기: {}", history.getYoutubeRecipes().size());
                        log.info("   youtubeRecipes 비어있음 여부: {}", history.getYoutubeRecipes().isEmpty());
                        if (!history.getYoutubeRecipes().isEmpty()) {
                            history.getYoutubeRecipes().forEach(recipe -> {
                                log.info("     레시피 - 제목: {}, URL: {}", recipe.getTitle(), recipe.getUrl());
                            });
                        }
                    }
                    
                    // YouTube 레시피 목록 변환
                    List<YoutubeRecipeDTO> youtubeRecipes = Collections.emptyList();
                    if (history.getYoutubeRecipes() != null && !history.getYoutubeRecipes().isEmpty()) {
                        log.info("✅ 히스토리 {}에서 YouTube 레시피 {}개 발견", history.getId(), history.getYoutubeRecipes().size());
                        youtubeRecipes = history.getYoutubeRecipes().stream()
                                .map(recipe -> {
                                    log.debug("레시피 변환 - 제목: {}, URL: {}", recipe.getTitle(), recipe.getUrl());
                                    return YoutubeRecipeDTO.builder()
                                            .title(recipe.getTitle())
                                            .url(recipe.getUrl())
                                            .videoId(extractVideoIdFromUrl(recipe.getUrl()))
                                            .build();
                                })
                                .collect(Collectors.toList());
                    } else {
                        log.warn("⚠️ 히스토리 {}에서 YouTube 레시피가 없음 (null: {}, empty: {})", 
                            history.getId(), 
                            history.getYoutubeRecipes() == null,
                            history.getYoutubeRecipes() != null && history.getYoutubeRecipes().isEmpty());
                    }
                    
                    // null 대신 빈 리스트 보장
                    if (youtubeRecipes == null) {
                        youtubeRecipes = Collections.emptyList();
                    }
                    
                    AnalysisHistoryDTO dto = AnalysisHistoryDTO.builder()
                            .historyId(history.getId())
                            .recognizedFoodName(history.getRecognizedFoodName())
                            .accuracy(history.getAccuracy())
                            .analysisDate(history.getAnalysisDate())
                            .thumbnailImageId(history.getId()) // 썸네일 이미지는 history ID로 조회
                            .youtubeRecipes(youtubeRecipes) // null이 아닌 빈 리스트 또는 실제 리스트
                            .build();
                    
                    log.info("AnalysisHistoryDTO 생성 완료 - ID: {}, 음식: {}, 레시피 개수: {}", 
                        dto.getHistoryId(), dto.getRecognizedFoodName(), 
                        dto.getYoutubeRecipes() != null ? dto.getYoutubeRecipes().size() : 0);
                    
                    // 최종 검증: youtubeRecipes가 null이면 빈 리스트로 설정
                    if (dto.getYoutubeRecipes() == null) {
                        log.warn("⚠️ DTO의 youtubeRecipes가 null입니다! 빈 리스트로 설정합니다.");
                        // Builder 패턴이므로 setter를 사용해야 함
                        dto.setYoutubeRecipes(Collections.emptyList());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * YouTube URL에서 videoId 추출
     * @param url YouTube URL
     * @return videoId 또는 null
     */
    private String extractVideoIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            // https://www.youtube.com/watch?v=VIDEO_ID 형식
            if (url.contains("watch?v=")) {
                return url.substring(url.indexOf("watch?v=") + 8).split("&")[0];
            }
            // https://youtu.be/VIDEO_ID 형식
            if (url.contains("youtu.be/")) {
                return url.substring(url.indexOf("youtu.be/") + 9).split("\\?")[0];
            }
        } catch (Exception e) {
            log.warn("YouTube URL에서 videoId 추출 실패: {}", url);
        }
        return null;
    }

    // 썸네일 생성을 담당하는 헬퍼(helper) 메소드
    private byte[] createThumbnail(MultipartFile image, int size) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(image.getBytes()))
                .size(size, size)
                .outputQuality(0.85)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    /**
     * YouTube 레시피 클릭 시 저장
     * @param userId 사용자 ID
     * @param historyId 분석 이력 ID
     * @param title YouTube 영상 제목
     * @param url YouTube 영상 URL
     */
    @Override
    public void saveClickedYouTubeRecipe(Long userId, String historyId, String title, String url) {
        log.info("🔍 YouTube 레시피 클릭 저장 요청 - 사용자 ID: {}, 히스토리 ID: {}, 제목: {}", userId, historyId, title);
        
        try {
            // MongoDB에서 해당 히스토리 조회
            Optional<AnalysisHistory> historyOptional = analysisHistoryRepository.findById(historyId);
            
            if (historyOptional.isEmpty()) {
                log.error("❌ 분석 이력을 찾을 수 없음 - History ID: {}", historyId);
                throw new IllegalArgumentException("분석 이력을 찾을 수 없습니다: " + historyId);
            }
            
            AnalysisHistory history = historyOptional.get();
            log.info("🔍 히스토리 조회 성공 - userId: {}, historyId: {}", history.getUserId(), historyId);
            
            // 사용자 ID 검증
            if (!history.getUserId().equals(userId)) {
                log.error("❌ 사용자 ID 불일치 - 요청한 사용자: {}, 히스토리 소유자: {}", userId, history.getUserId());
                throw new IllegalArgumentException("사용자 ID가 일치하지 않습니다.");
            }
            
            // YouTube 레시피 목록 가져오기 (null이면 빈 리스트로 초기화)
            List<AnalysisHistory.YoutubeRecipe> youtubeRecipes = history.getYoutubeRecipes();
            if (youtubeRecipes == null) {
                youtubeRecipes = new ArrayList<>();
                log.info("🔍 YouTube 레시피 목록이 null이므로 빈 리스트로 초기화");
            } else {
                log.info("🔍 기존 YouTube 레시피 개수: {}", youtubeRecipes.size());
            }
            
            // 중복 체크 (같은 URL이 이미 있는지 확인)
            boolean alreadyExists = youtubeRecipes.stream()
                    .anyMatch(recipe -> recipe.getUrl() != null && recipe.getUrl().equals(url));
            
            if (alreadyExists) {
                log.info("⚠️ 이미 저장된 YouTube 레시피입니다. URL: {}", url);
                return; // 중복은 예외가 아니므로 정상 반환
            }
            
            // 새로운 YouTube 레시피 추가
            AnalysisHistory.YoutubeRecipe newRecipe = AnalysisHistory.YoutubeRecipe.builder()
                    .title(title)
                    .url(url)
                    .build();
            
            youtubeRecipes.add(newRecipe);
            history.setYoutubeRecipes(youtubeRecipes);
            
            // MongoDB에 저장
            analysisHistoryRepository.save(history);
            
            log.info("✅ YouTube 레시피 저장 완료 - 히스토리 ID: {}, 제목: {}, 총 레시피 개수: {}", 
                    historyId, title, youtubeRecipes.size());
        } catch (IllegalArgumentException e) {
            // 검증 실패는 예외를 다시 던져서 컨트롤러에서 처리
            log.error("❌ YouTube 레시피 저장 검증 실패 - 히스토리 ID: {}", historyId, e);
            throw e;
        } catch (Exception e) {
            log.error("❌ YouTube 레시피 저장 중 오류 발생 - 히스토리 ID: {}", historyId, e);
            throw new RuntimeException("YouTube 레시피 저장 중 오류가 발생했습니다.", e);
        }
    }
}