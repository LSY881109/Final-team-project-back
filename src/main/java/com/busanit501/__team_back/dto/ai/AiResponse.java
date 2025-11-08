package com.busanit501.__team_back.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

//Flask가 실제로 반환하는 JSON({"class": ..., "confidence": ..., "top3": [...]})을 받을 수 있도록
// DTO 필드명을 정확히 일치 (Hammer -> 파스타, Nipper -> 감바스 로 받게)
@Getter
@Setter
public class AiResponse {

//     JSON의 'class' 키를 'predictedClass' 필드에 매핑
    @JsonProperty("class")
    private String predictedClass;

    // JSON의 'confidence' 키를 'confidence' 필드에 매핑
    @JsonProperty("confidence")
    private double confidence;

    // JSON의 'top3' 키를 'top3' 필드에 매핑 (상위 3개 예측 결과)
    @JsonProperty("top3")
    private List<Top3Prediction> top3;

//    @JsonProperty("class"): class는 Java에서 예약어(keyword)이므로 변수명으로 사용할 수 없음.
    //    class라는 키 값을 Java의 predictedClass라는 필드에 자동으로 매핑(연결)
}