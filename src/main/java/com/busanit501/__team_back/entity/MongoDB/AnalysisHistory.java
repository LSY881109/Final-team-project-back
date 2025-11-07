package com.busanit501.__team_back.entity.MongoDB;
//"마이페이지" 기능의 핵심 데이터 소스

import jakarta.persistence.Id;
import lombok.*;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "analysis_histories")
@Getter
@Setter // YouTube 레시피 저장을 위해 추가
@Builder
@AllArgsConstructor // Builder를 위해 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisHistory {

    @Id
    private String id;

    private Long userId; // MariaDB의 User.id (어떤 사용자의 기록인지 식별)

    // [요구사항 3번] 마이페이지 표시용 썸네일 이미지
    private Binary thumbnailImageData;
    private String thumbnailContentType;

    // AI 분석 결과
    private String recognizedFoodName; // 인식된 음식 이름 ("파스타"...)
    private double accuracy; // 정확도 (예: 71% (혹은 0.71로 표기))

    // [요구사항 4번] 추천된 유튜브 레시피 링크
    private List<YoutubeRecipe> youtubeRecipes;

    private LocalDateTime analysisDate; // 분석 요청 시간

    @Getter
    @Setter // MongoDB 매핑을 위해 추가
    @Builder
    @AllArgsConstructor // Builder를 위해 추가
    @NoArgsConstructor // ModelMapper 등을 위해 추가
    @ToString
    public static class YoutubeRecipe {
        private String title;
        private String url;
        private String thumbnailUrl; // 유튜브 링크의 영상 썸네일 (사용안함. TODO로 남겨둠)
    }
}