package com.busanit501.__team_back.config;

import com.busanit501.__team_back.dto.admin.FoodReferenceDTO;
import com.busanit501.__team_back.dto.analysis.NutritionData;
import com.busanit501.__team_back.service.admin.FoodReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialization class that automatically inserts food nutrition data when the application starts
 * 
 * Usage:
 * 1. To activate this class, keep the @Component annotation
 * 2. To deactivate, comment out the @Component annotation
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FoodDataInitializer implements CommandLineRunner {

    private final FoodReferenceService foodReferenceService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting food nutrition data initialization...");

        // Food data array
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
                // Create nutrition data
                NutritionData nutritionData = NutritionData.builder()
                        .calories(food.calories)
                        .protein(food.protein)
                        .fat(food.fat)
                        .carbohydrates(food.carbohydrates)
                        .build();

                // Create DTO
                FoodReferenceDTO dto = FoodReferenceDTO.builder()
                        .foodName(food.name)
                        .nutritionData(nutritionData)
                        .build();

                // Attempt to save
                foodReferenceService.createFoodReference(dto);
                log.info("Food data inserted successfully: {}", food.name);
                successCount++;
            } catch (IllegalArgumentException e) {
                // Skip if already exists
                log.warn("Food data already exists: {} - {}", food.name, e.getMessage());
                skipCount++;
            } catch (Exception e) {
                log.error("Failed to insert food data: {} - {}", food.name, e.getMessage());
            }
        }

        log.info("Food nutrition data initialization completed - Success: {}, Skipped: {}", successCount, skipCount);
    }

    /**
     * Inner class to hold food data
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




