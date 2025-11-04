package com.busanit501.__team_back.dto.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantInfo {

    private String name;        // 식당 이름
    private String address;     // 식당 주소
    private double latitude;    // 위도
    private double longitude;   // 경도
    private double rating;      // 평점 (있을 경우)
    private String placeId;     // Google Place ID (클라이언트에서 상세정보 요청 시 사용)
}