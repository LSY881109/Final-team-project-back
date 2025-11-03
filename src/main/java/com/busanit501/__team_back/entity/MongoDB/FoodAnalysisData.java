package com.busanit501.__team_back.entity.MongoDB;

import lombok.Builder;
import lombok.Getter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

//모델학습용 이미지 수집을 위함.(사용자들이 업로드한 원본 이미지들을 정해진 음식 카테고리에 넣음)
@Document(collection = "food_analysis_data") // 하나의 컬렉션으로 통합
@Getter
@Builder
public class FoodAnalysisData {

    @Id
    private String id;

    private String foodCategory; // "파스타", "감바스", "양념치킨" 등

    private Binary originalImageData; // 사용자가 업로드한 원본 이미지 데이터

    private String contentType;

    private LocalDateTime createdAt; // 저장된 시간
}