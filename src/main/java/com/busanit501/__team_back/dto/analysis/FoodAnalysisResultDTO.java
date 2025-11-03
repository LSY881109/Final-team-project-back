package com.busanit501.__team_back.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flask AI 서버로부터 받은 음식 분석 결과와
 * 백엔드에서 추가적으로 처리된 정보를 담는 최종 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodAnalysisResultDTO {

    // ==== Flask 서버 분석 결과 (AI 인식) ====

    // 인식된 음식 이름 (예: "김치찌개", "샐러드")
    private String recognizedFoodName;

    // AI 모델의 인식 확률 (신뢰도)
    private Double confidenceScore;

    // 음식의 양/크기 정보 (Flask에서 부가적으로 전달될 수 있음)
    private String servingSizeInfo;

    // ==== 백엔드 처리/DB 연동 결과 (추가 정보) ====

    // 해당 음식의 칼로리 (DB에서 조회)
    private Integer totalCalories;

    // 주요 영양 정보 요약 (예: 탄수화물, 단백질, 지방)
    private String macroNutrientsSummary;

    // 관련 레시피 ID (MongoDB 또는 MariaDB에서 조회)
    private String recipeId;

    // 관련 레시피를 바로 볼 수 있는 외부 링크 (유튜브 등)
    private String externalRecipeLink;

    // 분석 성공 여부 메시지
    private String message;
}