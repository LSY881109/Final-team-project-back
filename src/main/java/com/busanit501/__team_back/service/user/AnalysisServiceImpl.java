package com.busanit501.__team_back.service.user;
import com.busanit501.__team_back.dto.ai.AiResponse;
import com.busanit501.__team_back.repository.mongo.AnalysisHistoryRepository;
import com.busanit501.__team_back.repository.mongo.FoodAnalysisDataRepository;
import com.busanit501.__team_back.repository.mongo.FoodReferenceRepository;
import com.busanit501.__team_back.service.ai.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class AnalysisServiceImpl implements AnalysisService {

    private final FoodAnalysisDataRepository foodAnalysisDataRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final FoodReferenceRepository foodReferenceRepository;
    private final AIAnalysisService aiAnalysisService; // AIAnalysisService 주입

    // 외부 API 통신을 위한 컴포넌트 (추후 생성)
    // private final FlaskClient flaskClient;
    // private final YoutubeClient youtubeClient;

    @Override
    public String analyzeImage(Long userId, MultipartFile image) {
        log.info("AnalysisService - analyzeImage 실행...");
        log.info("사용자 ID: " + userId);
        log.info("이미지 파일: " + image.getOriginalFilename());

    try {
        // [STEP 1] Flask AI 서버로 이미지 전송 및 결과 수신
        AiResponse aiResult = aiAnalysisService.analyzeImage(image);
        String foodName = aiResult.getFoodName();
        double accuracy = aiResult.getAccuracy();
        log.info("AI 분석 결과: " + foodName + " (" + accuracy * 100 + "%)");

        // [STEP 2] 인식된 음식 이름으로 FoodReference DB에서 영양 정보 조회
        // FoodReference foodReference = foodReferenceRepository.findByFoodName(foodName)
        //        .orElseThrow(() -> new IllegalArgumentException("영양 정보를 찾을 수 없는 음식입니다."));
        log.info(foodName + "의 영양 정보 조회 완료 (임시)");

        // [STEP 3] 음식 이름으로 YouTube API 검색 (가상)
        // List<YoutubeRecipe> recipes = youtubeClient.searchRecipes(foodName);
        log.info(foodName + " 관련 유튜브 레시피 검색 완료 (임시)");

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

        // [STEP 6] 최종 결과를 DTO로 가공하여 반환
        return "분석 완료: " + foodName; // 최종 응답 DTO 반환 예정

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("FLASK 서버와 통신 오류발생");
        }

    }
}