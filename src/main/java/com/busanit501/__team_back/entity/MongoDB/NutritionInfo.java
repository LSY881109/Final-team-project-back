package com.busanit501.__team_back.entity.MongoDB;

import lombok.Builder;
import lombok.Getter;

/**
 * FoodReference에 포함될 영양 정보 (임베디드 도큐먼트)
 * MongoDB에서 FoodReference의 하위 객체로 저장됨
 */
@Getter
@Builder
public class NutritionInfo {
    private double calories; // 칼로리 (kcal단위)
    private double carbohydrate; // 탄수화물 (g단위)
    private double protein; // 단백질 (g)
    private double fat; // 지방 (g)
    // TODO: 비타민, 무기질 등 필요한 영양소 필드 추가
}

