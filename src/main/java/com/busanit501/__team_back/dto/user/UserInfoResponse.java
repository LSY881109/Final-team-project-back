package com.busanit501.__team_back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 현재 로그인한 사용자 정보 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private String userId;
    private String email;
    private List<String> oauthProviders; // "google", "naver" 등
    private boolean isOAuthUser; // OAuth2로 가입한 사용자인지 여부
    private String profileImageId; // MongoDB의 프로필 이미지 ID
}

