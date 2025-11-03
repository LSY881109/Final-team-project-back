package com.busanit501.__team_back.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 토큰 정보를 담는 내부 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfo {
    private String grantType;
    private String accessToken;
    private Long accessTokenExpiresIn; // 만료 시간을 Long 타입으로 저장
    private String refreshToken;
}