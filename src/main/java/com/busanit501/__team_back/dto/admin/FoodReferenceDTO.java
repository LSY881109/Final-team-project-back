package com.busanit501.__team_back.dto.admin;

import com.busanit501.__team_back.dto.analysis.NutritionData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 페이지에서 사용하는 FoodReference DTO
 * React에서 받는/보내는 데이터 형식
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodReferenceDTO {
    private String id;
    private String foodName;
    private NutritionData nutritionData; // calories, carbohydrates, protein, fat
}

