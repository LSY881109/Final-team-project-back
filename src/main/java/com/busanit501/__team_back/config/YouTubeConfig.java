package com.busanit501.__team_back.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class YouTubeConfig {

    @Value("${youtube.api.key}")
    private String apiKey;

    @Bean
    public YouTube youTube() throws GeneralSecurityException, IOException {
        // YouTube 객체를 생성하여 Spring Bean으로 등록합니다.
        // 이 객체는 API 요청을 인증하고 실행하는 데 사용됩니다.
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                request -> {}
        ).setApplicationName("__team_back").build();
    }
}
