package com.busanit501.__team_back.service.user;
import com.busanit501.__team_back.dto.ai.AiResponse;
import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import com.busanit501.__team_back.dto.analysis.NutritionData;
import com.busanit501.__team_back.dto.analysis.YoutubeRecipeDTO;
import com.busanit501.__team_back.entity.MongoDB.FoodReference;
import com.busanit501.__team_back.repository.mongo.AnalysisHistoryRepository;
import com.busanit501.__team_back.repository.mongo.FoodAnalysisDataRepository;
import com.busanit501.__team_back.repository.mongo.FoodReferenceRepository;
import com.busanit501.__team_back.service.ai.AIAnalysisService;
import com.busanit501.__team_back.service.api.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
            String foodName = aiResult.getFoodName();
            Double accuracy = aiResult.getAccuracy();
            log.info("AI 분석 결과: {} ({}%)", foodName, accuracy * 100);

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
                log.warn("{} 영양 정보를 찾을 수 없습니다.", foodName);
            }

            // [STEP 3] 음식 이름으로 YouTube API 검색
            List<YoutubeRecipeDTO> youtubeRecipes = youtubeApiService.searchRecipes(foodName);
            log.info("{} 관련 유튜브 레시피 검색 완료: {}개", foodName, youtubeRecipes.size());

            // [STEP 4] 원본 이미지를 학습용 DB에 저장 (비동기 처리 고려)
            // 이 작업은 사용자 응답 시간에 영향을 주지 않도록 @Async 등을 사용하여 비동기적으로 처리하는 것이 좋습니다.
            // FoodAnalysisData data = FoodAnalysisData.builder()...
            // foodAnalysisDataRepository.save(data);
            log.info("학습용 이미지 데이터 저장 완료 (임시)");

            // [STEP 5] 썸네일 생성 및 최종 분석 결과를 AnalysisHistory DB에 저장
            // byte[] thumbnailData = createThumbnail(image);
            // AnalysisHistory history = AnalysisHistory.builder()...
            // analysisHistoryRepository.save(history);
            log.info("사용자 분석 기록 저장 완료 (임시)");

            // [STEP 6] 조회 및 변환된 모든 데이터를 최종 FoodAnalysisResultDTO에 담아 반환
            return FoodAnalysisResultDTO.builder()
                    .foodName(foodName)
                    .accuracy(accuracy)
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
}