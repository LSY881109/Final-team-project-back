package com.busanit501.__team_back.entity.MongoDB;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// 직접 입력하거나 크롤링한 표준 음식 데이터를 저장.
// AI가 "파스타"라고 예측 -> 이 컬렉션에서 "파스타"의 영양 정보를 찾아 화면에 출력
@Document(collection = "food_references")
@Getter
@Builder
public class FoodReference {

    @Id
    private String id;

    @Indexed(unique = true) // 최대한 빠르게 보여주기위해서 인덱스작업 함
    private String foodName; // "파스타", "감바스"...

    private NutritionInfo nutritionInfo; // 영양 정보 (칼로리, 5대영양소)
}

// 위 FoodReference 클래스에 포함될 하위 객체 (임베디드 도큐먼트)
@Getter
@Builder
class NutritionInfo {
    private double calories; // 칼로리 (kcal단위)
    private double carbohydrate; // 탄수화물 (g단위)
    private double protein; // 단백질 (g)
    private double fat; // 지방 (g)
    // TODO: 비타민, 무기질 등 필요한 영양소 필드 추가
}