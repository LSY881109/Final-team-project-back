package com.busanit501.__team_back.service.ai;

import com.busanit501.__team_back.domain.mongo.FoodReference;
import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import com.busanit501.__team_back.dto.analysis.NutritionData;
import com.busanit501.__team_back.dto.analysis.YoutubeRecipeDTO;
import com.busanit501.__team_back.repository.mongo.FoodReferenceRepository;
import com.busanit501.__team_back.service.api.YoutubeApiService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class AIAnalysisService {

    // --- 기존 의존성 ---
    private final OkHttpClient okHttpClient;
    private final Gson gson;

    // --- 새로 추가된 의존성 ---
    private final FoodReferenceRepository foodReferenceRepository;
    private final YoutubeApiService youtubeApiService;
    private final ModelMapper modelMapper;

    @Value("${flask.api.url}")
    private String flaskApiUrl;

    public FoodAnalysisResultDTO analyzeImage(MultipartFile imageFile) {
        log.info("Flask 서버로 이미지 분석 요청 시작: {}", imageFile.getOriginalFilename());

        FoodAnalysisResultDTO initialResultDTO;

        // 1. Flask 서버와 통신하여 AI 분석 결과 가져오기
        try {
            RequestBody fileBody = RequestBody.create(
                    imageFile.getBytes(),
                    MediaType.parse(imageFile.getContentType())
            );

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", imageFile.getOriginalFilename(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(flaskApiUrl + "/analyze")
                    .post(requestBody)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.error("Flask 서버 통신 실패 (HTTP {}): {}", response.code(), response.message());
                    throw new IOException("AI 서버 통신 오류");
                }
                String jsonResponse = response.body().string();
                log.info("Flask 서버 응답 JSON 수신: {}", jsonResponse);
                initialResultDTO = gson.fromJson(jsonResponse, FoodAnalysisResultDTO.class);
            }

        } catch (IOException e) {
            log.error("Flask 서버 통신 중 예외 발생", e);
            // 통신 실패 시 에러 DTO 반환
            return FoodAnalysisResultDTO.builder()
                    .message("AI 분석 서버 연결 오류: " + e.getMessage())
                    .foodName("N/A")
                    .build();
        }

        // AI 분석 결과가 없으면 여기서 중단
        if (initialResultDTO == null || initialResultDTO.getFoodName() == null) {
            return FoodAnalysisResultDTO.builder().message("AI 분석 결과 없음").foodName("N/A").build();
        }

        String foodName = initialResultDTO.getFoodName();
        log.info("AI 분석 음식 이름: {}", foodName);

        // 2. DB에서 영양 정보 조회 및 DTO 변환
        Optional<FoodReference> foodRefOptional = foodReferenceRepository.findByFoodName(foodName);
        NutritionData nutritionData = null;
        if (foodRefOptional.isPresent()) {
            // ModelMapper를 사용하여 Entity -> DTO 변환
            nutritionData = modelMapper.map(foodRefOptional.get(), NutritionData.class);
            log.info("{} 영양 정보 조회 성공", foodName);
        } else {
            log.warn("{} 영양 정보 없음", foodName);
        }

        // 3. YouTube API로 레시피 검색
        List<YoutubeRecipeDTO> youtubeRecipes = youtubeApiService.searchRecipes(foodName);
        log.info("{} 레시피 검색 결과 {}개", foodName, youtubeRecipes.size());

        // 4. 모든 정보를 취합하여 최종 DTO 조립 후 반환
        return FoodAnalysisResultDTO.builder()
                .foodName(initialResultDTO.getFoodName())
                .accuracy(initialResultDTO.getAccuracy())
                .nutritionData(nutritionData) // 영양 정보 설정
                .youtubeRecipes(youtubeRecipes) // 유튜브 레시피 목록 설정
                .message("분석 및 정보 조회 성공")
                .build();
    }
}
