package com.busanit501.__team_back.service.admin;

import com.busanit501.__team_back.dto.admin.FoodReferenceDTO;
import com.busanit501.__team_back.dto.analysis.NutritionData;
import com.busanit501.__team_back.entity.MongoDB.FoodReference;
import com.busanit501.__team_back.entity.MongoDB.NutritionInfo;
import com.busanit501.__team_back.repository.mongo.FoodReferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class FoodReferenceService {

    private final FoodReferenceRepository foodReferenceRepository;

    /**
     * 모든 음식 참조 정보 조회
     */
    public List<FoodReferenceDTO> getAllFoodReferences() {
        log.info("모든 음식 참조 정보 조회");
        List<FoodReference> allReferences = foodReferenceRepository.findAll();
        return allReferences.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 음식 참조 정보 생성
     */
    @Transactional
    public FoodReferenceDTO createFoodReference(FoodReferenceDTO dto) {
        log.info("음식 참조 정보 생성: {}", dto.getFoodName());

        // 중복 체크
        if (foodReferenceRepository.findByFoodName(dto.getFoodName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 음식 이름입니다: " + dto.getFoodName());
        }

        // Entity 생성
        FoodReference foodReference = FoodReference.builder()
                .foodName(dto.getFoodName())
                .nutritionInfo(convertNutritionDataToInfo(dto.getNutritionData()))
                .build();

        FoodReference saved = foodReferenceRepository.save(foodReference);
        log.info("음식 참조 정보 저장 완료: ID={}, 이름={}", saved.getId(), saved.getFoodName());

        return convertToDTO(saved);
    }

    /**
     * 음식 참조 정보 수정
     */
    @Transactional
    public FoodReferenceDTO updateFoodReference(String id, FoodReferenceDTO dto) {
        log.info("음식 참조 정보 수정: ID={}, 이름={}", id, dto.getFoodName());

        FoodReference foodReference = foodReferenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음식 참조 정보입니다: " + id));

        // 음식 이름 변경 시 중복 체크
        if (!foodReference.getFoodName().equals(dto.getFoodName())) {
            if (foodReferenceRepository.findByFoodName(dto.getFoodName()).isPresent()) {
                throw new IllegalArgumentException("이미 존재하는 음식 이름입니다: " + dto.getFoodName());
            }
        }

        // 수정할 Entity 생성 (기존 ID 유지)
        FoodReference updated = FoodReference.builder()
                .id(foodReference.getId())
                .foodName(dto.getFoodName())
                .nutritionInfo(convertNutritionDataToInfo(dto.getNutritionData()))
                .build();

        FoodReference saved = foodReferenceRepository.save(updated);
        log.info("음식 참조 정보 수정 완료: ID={}, 이름={}", saved.getId(), saved.getFoodName());

        return convertToDTO(saved);
    }

    /**
     * 음식 참조 정보 삭제
     */
    @Transactional
    public void deleteFoodReference(String id) {
        log.info("음식 참조 정보 삭제: ID={}", id);

        if (!foodReferenceRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 음식 참조 정보입니다: " + id);
        }

        foodReferenceRepository.deleteById(id);
        log.info("음식 참조 정보 삭제 완료: ID={}", id);
    }

    /**
     * FoodReference Entity를 DTO로 변환
     */
    private FoodReferenceDTO convertToDTO(FoodReference entity) {
        NutritionData nutritionData = null;
        if (entity.getNutritionInfo() != null) {
            NutritionInfo info = entity.getNutritionInfo();
            nutritionData = NutritionData.builder()
                    .calories(info.getCalories())
                    .protein(info.getProtein())
                    .fat(info.getFat())
                    .carbohydrates(info.getCarbohydrate()) // Entity는 carbohydrate, DTO는 carbohydrates
                    .build();
        }

        return FoodReferenceDTO.builder()
                .id(entity.getId())
                .foodName(entity.getFoodName())
                .nutritionData(nutritionData)
                .build();
    }

    /**
     * NutritionData DTO를 NutritionInfo Entity로 변환
     */
    private NutritionInfo convertNutritionDataToInfo(NutritionData data) {
        if (data == null) {
            return NutritionInfo.builder()
                    .calories(0.0)
                    .protein(0.0)
                    .fat(0.0)
                    .carbohydrate(0.0)
                    .build();
        }

        return NutritionInfo.builder()
                .calories(data.getCalories() != null ? data.getCalories() : 0.0)
                .protein(data.getProtein() != null ? data.getProtein() : 0.0)
                .fat(data.getFat() != null ? data.getFat() : 0.0)
                .carbohydrate(data.getCarbohydrates() != null ? data.getCarbohydrates() : 0.0) // DTO는 carbohydrates, Entity는 carbohydrate
                .build();
    }
}

