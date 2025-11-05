package com.busanit501.__team_back.config;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public WebClient googlePlacesWebClient() {
        return WebClient.builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place")
                .build();
        }
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    // ImageAnnotatorClient Bean은 여기서 삭제되었습니다.
    @Bean
    public Gson gson() {
        return new Gson();
    }
}