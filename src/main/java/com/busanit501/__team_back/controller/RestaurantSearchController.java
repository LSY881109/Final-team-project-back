package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.map.RestaurantInfo;
import com.busanit501.__team_back.service.RestaurantSearchService;
// ... (나머지 import) ...
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
// ... (나머지 import) ...
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "*") // CORS 허용 (개발 환경)
public class RestaurantSearchController {

    // 주입된 Service 변수명: restaurantSearchService
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

        // 💡 수정된 부분: 'aws' 대신 'restaurantSearchService' 사용
        List<RestaurantInfo> restaurants = restaurantSearchService.findNearbyRestaurants(foodName, latitude, longitude);

        return ResponseEntity.ok(restaurants);
    }
}