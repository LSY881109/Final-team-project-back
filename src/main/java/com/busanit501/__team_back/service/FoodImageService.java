package com.busanit501.__team_back.service;

import com.busanit501.__team_back.domain.mongo.FoodImage;
import com.busanit501.__team_back.dto.FoodImageDTO;
import com.busanit501.__team_back.repository.FoodImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class FoodImageService {

    private final FoodImageRepository foodImageRepository;

    /**
     * 이미지 업로드 및 저장
     */
    @Transactional
    public FoodImageDTO uploadImage(MultipartFile file) throws IOException {

        // 파일 유효성 검사
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 이미지 파일 확인
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // FoodImage 엔티티 생성
        FoodImage foodImage = FoodImage.builder()
                .fileName(file.getOriginalFilename())
                .contentType(contentType)
                .fileSize(file.getSize())
                .imageData(file.getBytes())
                .uploadedAt(LocalDateTime.now())
                .build();

        // MongoDB에 저장
        FoodImage savedImage = foodImageRepository.save(foodImage);

        log.info("이미지 저장 완료: {}, 크기: {} bytes", savedImage.getFileName(), savedImage.getFileSize());

        return convertToDTO(savedImage, false);
    }

    /**
     * 이미지 조회 (ID로)
     */
    public FoodImageDTO getImage(String id, boolean includeImageData) {
        FoodImage foodImage = foodImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다: " + id));

        return convertToDTO(foodImage, includeImageData);
    }

    /**
     * 모든 이미지 조회
     */
    public List<FoodImageDTO> getAllImages(boolean includeImageData) {
        return foodImageRepository.findAll().stream()
                .map(image -> convertToDTO(image, includeImageData))
                .collect(Collectors.toList());
    }

    /**
     * 이미지 삭제
     */
    @Transactional
    public void deleteImage(String id) {
        if (!foodImageRepository.existsById(id)) {
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다: " + id);
        }
        foodImageRepository.deleteById(id);
        log.info("이미지 삭제 완료: {}", id);
    }

    /**
     * AI 분석 결과 업데이트
     */
    @Transactional
    public FoodImageDTO updateFoodName(String id, String foodName) {
        FoodImage foodImage = foodImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다: " + id));

        foodImage.setFoodName(foodName);
        FoodImage updated = foodImageRepository.save(foodImage);

        log.info("음식 이름 업데이트 완료: {} -> {}", id, foodName);

        return convertToDTO(updated, false);
    }

    /**
     * Entity를 DTO로 변환
     */
    private FoodImageDTO convertToDTO(FoodImage foodImage, boolean includeImageData) {
        FoodImageDTO dto = FoodImageDTO.builder()
                .id(foodImage.getId())
                .fileName(foodImage.getFileName())
                .contentType(foodImage.getContentType())
                .fileSize(foodImage.getFileSize())
                .foodName(foodImage.getFoodName())
                .uploadedAt(foodImage.getUploadedAt())
                .build();

        // 이미지 데이터 포함 여부
        if (includeImageData && foodImage.getImageData() != null) {
            String base64Image = Base64.getEncoder().encodeToString(foodImage.getImageData());
            dto.setImageBase64(base64Image);
        }

        return dto;
    }
}