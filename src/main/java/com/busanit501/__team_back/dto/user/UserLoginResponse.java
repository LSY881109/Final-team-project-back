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
    // 🚩 FIX: APIUser의 mid(ID)가 String이므로, Long에서 String으로 변경
    private String userId;
    private String username;
    private String email;

    // JWT 토큰 정보
    private String grantType;
    private String accessToken;
    private Long accessTokenExpiresIn;
    private String refreshToken;

    // 오류 발생 시 메시지를 담기 위한 필드
    private String message;
}