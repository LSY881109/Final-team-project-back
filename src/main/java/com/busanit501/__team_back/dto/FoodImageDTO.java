package com.busanit501.__team_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodImageDTO {

    private String id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String foodName;
    private LocalDateTime uploadedAt;

    // Base64 인코딩된 이미지 (선택적)
    private String imageBase64;
}