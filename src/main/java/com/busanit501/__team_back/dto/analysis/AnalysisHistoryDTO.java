package com.busanit501.__team_back.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisHistoryDTO {
    private String historyId;
    private String recognizedFoodName;
    private Double accuracy;
    private LocalDateTime analysisDate;
    private String thumbnailImageId; // 썸네일 이미지 조회용 ID
    private List<YoutubeRecipeDTO> youtubeRecipes; // YouTube 레시피 목록
}

