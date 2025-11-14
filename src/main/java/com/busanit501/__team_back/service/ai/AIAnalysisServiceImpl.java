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

    private final OkHttpClient okHttpClient; // Bean injected from OkHttpConfig
    private final ObjectMapper objectMapper;

    @Value("${flask.api.url}") // Flask server URL from application.properties
    private String flaskApiUrl;

    @Override
    public AiResponse analyzeImage(MultipartFile imageFile) throws IOException {
        // 1. Create request body: Multipart format
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "image", // Key name for the file received by Flask
                        imageFile.getOriginalFilename(),
                        RequestBody.create(imageFile.getBytes(), MediaType.parse(imageFile.getContentType()))
                )
                .build();

        // 2. Create HTTP request
        Request.Builder requestBuilder = new Request.Builder()
                .url(flaskApiUrl) // flaskApiUrl already contains the full endpoint address
                .post(requestBody);
        
        // Add Ngrok header to skip browser warning page
        if (flaskApiUrl.contains("ngrok")) {
            requestBuilder.header("ngrok-skip-browser-warning", "true");
        }
        
        Request request = requestBuilder.build();
        
        System.out.println("🔗 Flask API URL: " + flaskApiUrl);

        // 3. Execute request and receive response
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                System.err.println("❌ Flask server error response (HTTP " + response.code() + "): " + errorBody.substring(0, Math.min(500, errorBody.length())));
                throw new IOException(String.format(
                    "Flask server response failed - HTTP %d: %s", 
                    response.code(), 
                    errorBody.length() > 500 ? errorBody.substring(0, 500) + "..." : errorBody
                ));
            }
            // 4. Convert received JSON string to DTO object and return
            String responseBody = response.body().string();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new IOException("Received empty response from Flask server.");
            }
            
            // Debug: Log response body (first 500 chars)
            System.out.println("📦 Flask response body (first 500 chars): " + responseBody.substring(0, Math.min(500, responseBody.length())));
            
            // Check if response is HTML (Ngrok warning page or error page)
            String trimmedBody = responseBody.trim();
            if (trimmedBody.startsWith("<") || trimmedBody.startsWith("<!DOCTYPE")) {
                System.err.println("❌ Flask server returned HTML instead of JSON. Response: " + trimmedBody.substring(0, Math.min(1000, trimmedBody.length())));
                throw new IOException("Flask server returned HTML instead of JSON. This may be due to Ngrok warning page or server error. Please check Flask server status and Ngrok configuration.");
            }
            
            try {
                return objectMapper.readValue(responseBody, AiResponse.class);
            } catch (com.fasterxml.jackson.core.JsonParseException e) {
                System.err.println("❌ JSON parsing error. Response body: " + responseBody.substring(0, Math.min(1000, responseBody.length())));
                throw new IOException("Failed to parse JSON response from Flask server. Response may be HTML or invalid JSON: " + e.getMessage(), e);
            }
        } catch (java.net.ConnectException e) {
            throw new IOException("Cannot connect to Flask server. Please check if the server is running: " + flaskApiUrl, e);
        } catch (java.net.SocketTimeoutException e) {
            throw new IOException("Flask server response timeout. Please check the server status.", e);
        }
    }
}