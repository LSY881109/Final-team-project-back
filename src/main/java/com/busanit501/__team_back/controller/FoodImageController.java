package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.FoodImageDTO;
import com.busanit501.__team_back.service.FoodImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food-images")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "*") // CORS 설정 (필요시 수정)
public class FoodImageController {

    private final FoodImageService foodImageService;

    /**
     * 이미지 업로드
     * POST /api/food-images/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {
            FoodImageDTO savedImage = foodImageService.uploadImage(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이미지가 성공적으로 업로드되었습니다.");
            response.put("data", savedImage);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 이미지 조회 (ID로)
     * GET /api/food-images/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getImage(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeImage) {

        try {
            FoodImageDTO image = foodImageService.getImage(id, includeImage);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", image);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 모든 이미지 조회
     * GET /api/food-images
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllImages(
            @RequestParam(defaultValue = "false") boolean includeImage) {

        List<FoodImageDTO> images = foodImageService.getAllImages(includeImage);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", images);
        response.put("count", images.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 이미지 삭제
     * DELETE /api/food-images/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String id) {

        try {
            foodImageService.deleteImage(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이미지가 성공적으로 삭제되었습니다.");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 음식 이름 업데이트 (AI 분석 결과)
     * PUT /api/food-images/{id}/food-name
     */
    @PutMapping("/{id}/food-name")
    public ResponseEntity<Map<String, Object>> updateFoodName(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        try {
            String foodName = request.get("foodName");
            if (foodName == null || foodName.trim().isEmpty()) {
                throw new IllegalArgumentException("음식 이름을 입력해주세요.");
            }

            FoodImageDTO updated = foodImageService.updateFoodName(id, foodName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "음식 이름이 업데이트되었습니다.");
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}