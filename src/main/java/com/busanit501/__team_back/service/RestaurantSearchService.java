// [ ⬇️ 여기서부터 복사하세요 ⬇️ ]
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
                        // 💡 DTO에 phoneNumber, website 필드가 있다면 여기서 null로 초기화 가능
                        // .phoneNumber(null) 
                        // .website(null)
                        .build();
                restaurants.add(restaurant);
            }
            return restaurants;

        } catch (Exception e) {
            log.error("Error calling Places API", e);
            return Collections.emptyList();
        }
    }


    // ========================================================================
    // 💡 [새로 추가된 메소드]
    // ========================================================================

    /**
     * Google Places API (Place Details)로 특정 장소의 상세 정보 검색
     * @param placeId "ChIJ..."로 시작하는 Google Place ID
     * @return 가게 상세정보 (전화번호, 웹사이트 등)가 담긴 DTO
     */
    public RestaurantInfo getRestaurantDetails(String placeId) {

        // Google에 요청할 필드 목록
        // (전화번호, 웹사이트, 영업시간 등)
        String fields = "place_id,name,formatted_phone_number,website,opening_hours,formatted_address";

        try {
            // Place Details API 호출
            JsonNode response = googlePlacesWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/details/json") // 💡 API 경로 변경
                            .queryParam("place_id", placeId) // 💡 파라미터 변경
                            .queryParam("key", placesApiKey)
                            .queryParam("fields", fields) // 💡 상세정보 필드 요청
                            .queryParam("language", "ko") // 한국어
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(); // 동기 방식

            if (response == null || !response.path("status").asText().equals("OK")) {
                log.error("Place Details API Error: {}", response != null ? response.path("status").asText() : "Response is null");
                return null; // 또는 예외 처리
            }

            // 결과를 DTO로 파싱
            JsonNode result = response.path("result");

            // 💡 RestaurantInfo DTO를 재사용
            // ⚠️ (주의!) RestaurantInfo DTO 파일에 'phoneNumber', 'website' 필드가 없다면
            //    DTO 파일에도 해당 필드를 추가해 주셔야 합니다!
            return RestaurantInfo.builder()
                    .placeId(result.path("place_id").asText())
                    .name(result.path("name").asText())
                    .address(result.path("formatted_address").asText(null)) // 주소 (이미 목록에 있지만 추가)
                    .phoneNumber(result.path("formatted_phone_number").asText(null)) // 💡 전화번호
                    .website(result.path("website").asText(null)) // 💡 웹사이트
                    // .openingHours(...) // 영업시간(opening_hours)은 구조가 복잡하여 별도 파싱 필요
                    .build();

        } catch (Exception e) {
            log.error("Error calling Place Details API", e);
            return null; // 또는 예외 처리
        }
    }

} // [ ⬆️ 여기까지 복사하세요 (클래스 닫는 괄호) ⬆️ ]