package com.busanit501.__team_back.service.user;
import com.busanit501.__team_back.dto.ai.AiResponse;
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
                nutritionData = modelMapper.map(
                    foodRefOptional.get().getNutritionInfo(), 
                    NutritionData.class
                );
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
                        .youtubeRecipes(
                                // YoutubeRecipeDto 리스트를 AnalysisHistory의 YoutubeRecipe 리스트로 변환
                                youtubeRecipes.stream()
                                        .map(dto ->
                                                modelMapper.map(dto, AnalysisHistory.YoutubeRecipe.class))
                                        .collect(Collectors.toList())
                        )
                        .analysisDate(LocalDateTime.now())
                        .build();

                // (3) MongoDB에 최종 저장
                analysisHistoryRepository.save(history);

                log.info("사용자 분석 기록 저장 완료. History ID: {}", history.getId());
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
                    .nutritionData(nutritionData)
                    .youtubeRecipes(youtubeRecipes)
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
    // 썸네일 생성을 담당하는 헬퍼(helper) 메소드
    private byte[] createThumbnail(MultipartFile image, int size) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(image.getBytes()))
                .size(size, size)
                .outputQuality(0.85)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }
}