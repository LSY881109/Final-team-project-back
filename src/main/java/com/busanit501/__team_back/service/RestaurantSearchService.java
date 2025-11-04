package com.busanit501.__team_back.service;

import com.busanit501.__team_back.dto.map.RestaurantInfo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class RestaurantSearchService {

    private final WebClient googlePlacesWebClient;

    @Value("${google.places.api.key}")
    private String placesApiKey;

    /**
     * Google Places API (Text Search)로 주변 식당 검색
     * @param foodName "돈가스", "파스타" 등 검색할 음식 이름
     * @param latitude 사용자 현재 위도
     * @param longitude 사용자 현재 경도
     * @return 식당 정보 리스트
     */
    public List<RestaurantInfo> findNearbyRestaurants(String foodName, double latitude, double longitude) {

        // 검색 반경 (미터)
        int radius = 5000; // 5km (조정 가능)
        // 검색 쿼리 (예: "돈가스", "돈가스 맛집", "돈가스 식당")
        String query = foodName;

        try {
            // Places API 호출
            // WebClient를 사용한 비동기 호출 후 동기(block)로 변환
            JsonNode response = googlePlacesWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/textsearch/json")
                            .queryParam("query", query)
                            .queryParam("location", String.format("%f,%f", latitude, longitude))
                            .queryParam("radius", radius)
                            .queryParam("key", placesApiKey)
                            .queryParam("language", "ko") // 한국어로 결과 요청
                            .queryParam("type", "restaurant") // 식당 타입으로 제한
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(); // 동기 방식

            if (response == null || !response.path("status").asText().equals("OK")) {
                log.error("Places API Error: {}", response != null ? response.path("status").asText() : "Response is null");
                return Collections.emptyList();
            }

            // 결과(JsonNode)를 DTO(List<RestaurantInfo>)로 파싱
            List<RestaurantInfo> restaurants = new ArrayList<>();
            for (JsonNode result : response.path("results")) {
                JsonNode location = result.path("geometry").path("location");
                RestaurantInfo restaurant = RestaurantInfo.builder()
                        .name(result.path("name").asText())
                        .address(result.path("formatted_address").asText())
                        .latitude(location.path("lat").asDouble())
                        .longitude(location.path("lng").asDouble())
                        .rating(result.path("rating").asDouble(0.0))
                        .placeId(result.path("place_id").asText())
                        .build();
                restaurants.add(restaurant);
            }
            return restaurants;

        } catch (Exception e) {
            log.error("Error calling Places API", e);
            return Collections.emptyList();
        }
    }
}