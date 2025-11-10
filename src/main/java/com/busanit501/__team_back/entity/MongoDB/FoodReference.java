package com.busanit501.__team_back.entity.MongoDB;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// 직접 입력하거나 크롤링한 표준 음식 데이터를 저장.
// AI가 "파스타"라고 예측 -> 이 컬렉션에서 "파스타"의 영양 정보를 찾아 화면에 출력
@Document(collection = "food_references")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodReference {

    @Id
    private String id;

    @Indexed(unique = true) // 최대한 빠르게 보여주기위해서 인덱스작업 함
    private String foodName; // "파스타", "감바스"...

    private NutritionInfo nutritionInfo; // 영양 정보 (칼로리, 5대영양소)
}