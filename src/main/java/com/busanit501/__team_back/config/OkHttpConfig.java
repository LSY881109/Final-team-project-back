package com.busanit501.__team_back.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OkHttpConfig {

    /**
     * OkHttpClient 인스턴스를 스프링 빈으로 등록하여 AIAnalysisService에서 주입받아 사용합니다.
     */
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10)) // 연결 타임아웃 10초
                .writeTimeout(Duration.ofSeconds(10))   // 쓰기 타임아웃 10초
                .readTimeout(Duration.ofSeconds(30))    // 읽기 타임아웃 30초
                .build();
    }
}