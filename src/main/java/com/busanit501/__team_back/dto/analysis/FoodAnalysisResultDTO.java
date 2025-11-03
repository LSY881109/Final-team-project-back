package com.busanit501.__team_back.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; // List 임포트 추가

/**
 * AI 서버의 분석 결과와 DB의 영양 정보, 유튜브 레시피를
 * 통합하여 클라이언트에 최종적으로 전달하는 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodAnalysisResultDTO {

    // ==== AI 분석 결과 ====
    // (AI 담당 팀원과 최종 필드명 협의 필요)
    private String foodName;    // 예: AI가 분석한 음식 이름
    private Double accuracy;    // 예: 정확도

    // 기존 Flask 서버 분석 결과 (AI 인식) - 주석 처리
    // private String recognizedFoodName;
    // private Double confidenceScore;
    // private String servingSizeInfo;

    // ==== 백엔드 처리/DB 연동 결과 (추가 정보) ====

    // 영양 정보 (FoodReference로부터 변환)
    private NutritionData nutritionData;

    // 유튜브 레시피 정보
    private List<YoutubeRecipeDTO> youtubeRecipes;

    // 기존 백엔드 처리/DB 연동 결과 (추가 정보) - 주석 처리
    // private Integer totalCalories;
    // private String recipeId;
    // private String externalRecipeLink;

    // 분석 성공 여부 메시지 (필요시 유지)
    private String message;
}