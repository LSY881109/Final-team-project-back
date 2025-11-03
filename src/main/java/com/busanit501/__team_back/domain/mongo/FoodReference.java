package com.busanit501.__team_back.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// NutritionData 클래스는 별도의 파일로 생성하거나, FoodReference 내의 static 클래스로 정의할 수 있습니다.
// 여기서는 FoodAnalysisResultDTO와 공유하기 위해 별도 파일로 생성했다고 가정합니다.
import com.busanit501.__team_back.dto.analysis.NutritionData;

@Document(collection = "food_references")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodReference {

    @Id
    private String id; // MongoDB의 ID는 보통 String 타입으로 매핑됩니다.

    private String foodName;

    private NutritionData nutritionData;
}