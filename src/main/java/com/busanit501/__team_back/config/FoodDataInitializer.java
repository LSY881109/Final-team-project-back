package com.busanit501.__team_back.config;

import com.busanit501.__team_back.dto.admin.FoodReferenceDTO;
import com.busanit501.__team_back.dto.analysis.NutritionData;
import com.busanit501.__team_back.service.admin.FoodReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 음식 영양 정보 데이터를 자동으로 삽입하는 초기화 클래스
 * 
 * 사용법:
 * 1. 이 클래스를 활성화하려면 @Component 어노테이션을 유지
 * 2. 비활성화하려면 @Component를 주석 처리
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FoodDataInitializer implements CommandLineRunner {

    private final FoodReferenceService foodReferenceService;

    @Override
    public void run(String... args) throws Exception {
        log.info("음식 영양 정보 데이터 초기화 시작...");

        // 음식 데이터 배열
        FoodData[] foods = {
            new FoodData("숯불치킨", 1900.0, 195.0, 90.0, 40.0),
            new FoodData("양념치킨", 2650.0, 190.0, 90.0, 130.0),
            new FoodData("후라이드치킨", 2040.0, 135.0, 120.0, 25.0),
            new FoodData("감바스", 540.0, 26.0, 26.0, 4.0),
            new FoodData("파스타", 600.0, 20.0, 10.0, 80.0)
        };

        int successCount = 0;
        int skipCount = 0;

        for (FoodData food : foods) {
            try {
                // 영양 정보 생성
                NutritionData nutritionData = NutritionData.builder()
                        .calories(food.calories)
                        .protein(food.protein)
                        .fat(food.fat)
                        .carbohydrates(food.carbohydrates)
                        .build();

                // DTO 생성
                FoodReferenceDTO dto = FoodReferenceDTO.builder()
                        .foodName(food.name)
                        .nutritionData(nutritionData)
                        .build();

                // 저장 시도
                foodReferenceService.createFoodReference(dto);
                log.info("✅ 음식 데이터 삽입 성공: {}", food.name);
                successCount++;
            } catch (IllegalArgumentException e) {
                // 이미 존재하는 경우 스킵
                log.warn("⚠️ 음식 데이터 이미 존재: {} - {}", food.name, e.getMessage());
                skipCount++;
            } catch (Exception e) {
                log.error("❌ 음식 데이터 삽입 실패: {} - {}", food.name, e.getMessage());
            }
        }

        log.info("음식 영양 정보 데이터 초기화 완료 - 성공: {}, 스킵: {}", successCount, skipCount);
    }

    /**
     * 음식 데이터를 담는 내부 클래스
     */
    private static class FoodData {
        String name;
        double calories;
        double protein;
        double fat;
        double carbohydrates;

        FoodData(String name, double calories, double protein, double fat, double carbohydrates) {
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.fat = fat;
            this.carbohydrates = carbohydrates;
        }
    }
}


