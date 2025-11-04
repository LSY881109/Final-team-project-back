package com.busanit501.__team_back.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NutritionData {
    private Double calories;      // 칼로리 (kcal)
    private Double protein;       // 단백질 (g)
    private Double fat;           // 지방 (g)
    private Double carbohydrates; // 탄수화물 (g)
}
