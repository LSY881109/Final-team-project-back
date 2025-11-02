package com.busanit501.__team_back.domain.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "food_references")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodReference {

    @Id
    private String id;

    private String foodName;

    private NutritionData nutritionData;

    @Getter
    @Setter
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NutritionData {
        private double calories; // 칼로리
        private double protein; // 단백질
        private double fat; // 지방
        private double carbohydrates; // 탄수화물
    }
}

