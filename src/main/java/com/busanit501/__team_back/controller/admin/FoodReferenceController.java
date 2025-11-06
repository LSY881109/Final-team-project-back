package com.busanit501.__team_back.controller.admin;

import com.busanit501.__team_back.dto.admin.FoodReferenceDTO;
import com.busanit501.__team_back.service.admin.FoodReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/food-references")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "*") // React 개발 서버용 CORS 허용
public class FoodReferenceController {

    private final FoodReferenceService foodReferenceService;

    /**
     * 모든 음식 참조 정보 조회
     * GET /api/admin/food-references
     */
    @GetMapping
    public ResponseEntity<List<FoodReferenceDTO>> getAllFoodReferences() {
        try {
            log.info("음식 참조 정보 전체 조회 요청");
            List<FoodReferenceDTO> foodReferences = foodReferenceService.getAllFoodReferences();
            return ResponseEntity.ok(foodReferences);
        } catch (Exception e) {
            log.error("음식 참조 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 음식 참조 정보 생성
     * POST /api/admin/food-references
     */
    @PostMapping
    public ResponseEntity<?> createFoodReference(@RequestBody FoodReferenceDTO dto) {
        try {
            log.info("음식 참조 정보 생성 요청: {}", dto.getFoodName());

            // 유효성 검증
            if (dto.getFoodName() == null || dto.getFoodName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "음식 이름을 입력해주세요.");
                return ResponseEntity.badRequest().body(error);
            }

            FoodReferenceDTO created = foodReferenceService.createFoodReference(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.warn("음식 참조 정보 생성 실패: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("음식 참조 정보 생성 중 오류 발생", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 음식 참조 정보 수정
     * PUT /api/admin/food-references/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFoodReference(
            @PathVariable String id,
            @RequestBody FoodReferenceDTO dto) {
        try {
            log.info("음식 참조 정보 수정 요청: ID={}, 이름={}", id, dto.getFoodName());

            // 유효성 검증
            if (dto.getFoodName() == null || dto.getFoodName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "음식 이름을 입력해주세요.");
                return ResponseEntity.badRequest().body(error);
            }

            FoodReferenceDTO updated = foodReferenceService.updateFoodReference(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("음식 참조 정보 수정 실패: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("음식 참조 정보 수정 중 오류 발생", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 음식 참조 정보 삭제
     * DELETE /api/admin/food-references/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFoodReference(@PathVariable String id) {
        try {
            log.info("음식 참조 정보 삭제 요청: ID={}", id);
            foodReferenceService.deleteFoodReference(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("음식 참조 정보 삭제 실패: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("음식 참조 정보 삭제 중 오류 발생", e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

