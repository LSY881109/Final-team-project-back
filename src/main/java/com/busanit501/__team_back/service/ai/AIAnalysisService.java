package com.busanit501.__team_back.service.ai;

import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Flask AI 서버와 통신하여 음식 이미지 분석을 요청하고 결과를 수신하는 서비스
 */
@Service
@Log4j2
public class AIAnalysisService {

    // OkHttp 클라이언트 인스턴스 (Spring이 아닌 OkHttp 라이브러리)
    private final OkHttpClient okHttpClient;
    // application.properties에서 Flask 서버 URL 주입
    private final String flaskApiUrl;
    // JSON 직렬화/역직렬화를 위한 Gson 인스턴스
    private final Gson gson;

    // @Value를 사용하여 설정 파일의 값을 주입받고, OkHttpClient를 초기화합니다.
    public AIAnalysisService(
            @Value("${flask.api.url}") String flaskApiUrl,
            OkHttpClient okHttpClient,
            Gson gson) {
        this.flaskApiUrl = flaskApiUrl + "/analyze"; // Flask의 분석 엔드포인트는 /analyze라고 가정
        this.okHttpClient = okHttpClient;
        this.gson = gson;
        log.info("Flask API URL 설정 완료: {}", this.flaskApiUrl);
    }

    /**
     * 이미지 파일을 Flask 서버로 전송하고 분석 결과를 받아옵니다.
     * @param imageFile 클라이언트로부터 받은 이미지 파일
     * @return 분석 결과 DTO
     */
    public FoodAnalysisResultDTO analyzeImage(MultipartFile imageFile) {
        log.info("Flask 서버로 이미지 분석 요청 시작: {}", imageFile.getOriginalFilename());

        // 1. 요청 본문(RequestBody) 구성: MultipartFile 형태의 이미지 전송
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "image", // Flask 서버에서 요청 받을 파일 파라미터 이름 (Flask와 통일해야 함)
                        imageFile.getOriginalFilename(),
                        RequestBody.create(imageFile.getResource(), MediaType.parse(imageFile.getContentType()))
                )
                .build();

        // 2. OkHttp Request 객체 생성
        Request request = new Request.Builder()
                .url(flaskApiUrl)
                .post(requestBody)
                .build();

        // 3. 동기적으로 요청 실행 및 응답 처리
        try (Response response = okHttpClient.newCall(request).execute()) {

            // HTTP 요청/응답 과정

            if (!response.isSuccessful()) {
                log.error("Flask 서버 통신 실패 (HTTP {}): {}", response.code(), response.message());
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                return FoodAnalysisResultDTO.builder()
                        .message("AI 분석 서버 통신 오류 발생: " + response.code() + ", 응답: " + responseBody)
                        .recognizedFoodName("N/A")
                        .build();
            }

            // 4. 성공 응답 본문(JSON) 추출 및 DTO로 변환
            if (response.body() == null) {
                return FoodAnalysisResultDTO.builder().message("Flask 서버로부터 빈 응답 수신").recognizedFoodName("N/A").build();
            }

            String jsonResponse = response.body().string();
            log.info("Flask 서버 응답 JSON 수신: {}", jsonResponse);

            // Gson을 사용하여 JSON 문자열을 DTO 객체로 역직렬화
            FoodAnalysisResultDTO resultDTO = gson.fromJson(jsonResponse, FoodAnalysisResultDTO.class);
            resultDTO.setMessage("AI 분석 성공");

            return resultDTO;

        } catch (IOException e) {
            log.error("Flask 서버 통신 중 예외 발생", e);
            return FoodAnalysisResultDTO.builder()
                    .message("AI 분석 서버 연결 오류 발생: " + e.getMessage())
                    .recognizedFoodName("N/A")
                    .build();
        }
    }
}