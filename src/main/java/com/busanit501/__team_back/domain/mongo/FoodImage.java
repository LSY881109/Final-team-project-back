package com.busanit501.__team_back.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "food_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodImage {

    @Id
    private String id;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private byte[] imageData;

    private String foodName; // AI 분석 결과

    private LocalDateTime uploadedAt;

    private String userId; // 추후 회원가입 기능 추가시 사용
}