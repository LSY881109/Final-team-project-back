package com.busanit501.__team_back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 성공 시 응답 DTO
 * (사용자 정보와 JWT 토큰을 포함)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse {

    // 사용자 정보
    private Long userId;
    private String username;
    private String email;

    // JWT 토큰 정보
    private String grantType;
    private String accessToken;
    private Long accessTokenExpiresIn;
    private String refreshToken;

    // [수정사항 적용]: 오류 발생 시 메시지를 담기 위한 필드 추가
    private String message;
}