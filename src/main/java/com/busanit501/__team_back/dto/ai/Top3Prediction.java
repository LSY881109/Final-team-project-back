package com.busanit501.__team_back.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Flask에서 반환하는 상위 3개 예측 결과를 담는 DTO
 */
@Getter
@Setter
public class Top3Prediction {
    @JsonProperty("class")
    private String className;
    
    @JsonProperty("confidence")
    private double confidence;
}

