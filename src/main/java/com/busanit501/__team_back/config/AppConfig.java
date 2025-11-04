package com.busanit501.__team_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    /**
     * Google Places API 호출을 위한 WebClient Bean
     */
    @Bean
    public WebClient googlePlacesWebClient() {
        return WebClient.builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place")
                .build();
    }

    // ImageAnnotatorClient Bean은 여기서 삭제되었습니다.
}