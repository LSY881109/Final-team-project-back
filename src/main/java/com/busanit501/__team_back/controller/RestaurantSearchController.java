// [ ⬇️ 이 코드로 파일을 덮어쓰세요 ⬇️ ]
package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.map.RestaurantInfo;
import com.busanit501.__team_back.service.RestaurantSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "*") // CORS 허용 (개발 환경)
public class RestaurantSearchController {

    private final RestaurantSearchService restaurantSearchService;

    @Operation(summary = "음식 이름으로 주변 식당 검색")
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantInfo>> searchRestaurants(
            @Parameter(description = "검색할 음식 이름 (예: 돈가스)")
            @RequestParam("foodName") String foodName,

            @Parameter(description = "사용자 현재 위도 (Latitude)")
            @RequestParam("latitude") double latitude,

            @Parameter(description = "사용자 현재 경도 (Longitude)")
            @RequestParam("longitude") double longitude) {

        log.info("Request received: foodName={}, lat={}, lon={}", foodName, latitude, longitude);

        List<RestaurantInfo> restaurants = restaurantSearchService.findNearbyRestaurants(foodName, latitude, longitude);

        return ResponseEntity.ok(restaurants);
    }

    // ========================================================================
    // 💡 [수정 완료된 최종 API]
    // ========================================================================

    @Operation(summary = "Place ID로 특정 장소의 상세 정보 검색")
    @GetMapping("/details")
    public ResponseEntity<RestaurantInfo> getRestaurantDetails( // 💡 1. 반환 타입이 RestaurantInfo로 변경됨
                                                                @Parameter(description = "Google Place ID")
                                                                @RequestParam("placeId") String placeId) {

        log.info("Request received for Place Details: placeId={}", placeId);

        // 💡 2. 주석이 풀리고 실제 Service 메소드 호출
        RestaurantInfo details = restaurantSearchService.getRestaurantDetails(placeId);

        if (details != null) {
            return ResponseEntity.ok(details); // 💡 3. 실제 DTO 반환
        } else {
            // Service에서 null을 반환했을 때 (API 에러 등)
            return ResponseEntity.notFound().build();
        }
    }
}