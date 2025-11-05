package com.busanit501.__team_back.service.ai;

import com.busanit501.__team_back.dto.ai.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final OkHttpClient okHttpClient; // OkHttpConfig에서 등록한 Bean 주입
    private final ObjectMapper objectMapper;

    @Value("${flask.api.url}") // application.properties에서 Flask 서버 주소 가져오기
    private String flaskApiUrl;

    @Override
    public AiResponse analyzeImage(MultipartFile imageFile) throws IOException {
        // 1. 요청 바디(Body) 생성: Multipart 형식
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "image", // Flask에서 받을 파일의 key 이름
                        imageFile.getOriginalFilename(),
                        RequestBody.create(imageFile.getBytes(), MediaType.parse(imageFile.getContentType()))
                )
                .build();

//        application.properties에서 전체 URL을 관리하므로, 서비스 코드에서는 URL을 조합할 필요XX
        // 2. HTTP 요청(Request) 생성
        Request request = new Request.Builder()
//                .url(flaskApiUrl + "/analyze") // Flask API 엔드포인트
                .url(flaskApiUrl) // flaskApiUrl에 이미 전체 엔드포인트 주소가 들어있음 - 변경
                .post(requestBody)
                .build();

        // 3. 요청 실행 및 응답 수신
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "응답 본문 없음";
                throw new IOException(String.format(
                    "Flask 서버 응답 실패 - HTTP %d: %s", 
                    response.code(), 
                    errorBody
                ));
            }
            // 4. 응답받은 JSON 문자열을 DTO 객체로 변환하여 반환
            String responseBody = response.body().string();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new IOException("Flask 서버로부터 빈 응답을 받았습니다.");
            }
            return objectMapper.readValue(responseBody, AiResponse.class);
        } catch (java.net.ConnectException e) {
            throw new IOException("Flask 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인하세요: " + flaskApiUrl, e);
        } catch (java.net.SocketTimeoutException e) {
            throw new IOException("Flask 서버 응답 시간 초과. 서버 상태를 확인하세요.", e);
        }
    }
}