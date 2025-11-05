package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.repository.maria.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestProfileViewController {

    private final UserRepository userRepository;

    /** 세션(OAuth2)이 있으면 현재 사용자 기준으로 프로필 렌더링. 없으면 간단한 안내/입력폼 표시 */
    @GetMapping("/profile")
    public String profile(Principal principal, Authentication authentication, Model model) {
        // 템플릿 대신 정적 페이지로 리다이렉트
        return "redirect:/test/test-profile.html";
    }

    /** 로그인 없이 특정 userId로 바로 확인(우회 테스트용) */
    @GetMapping("/profile/by-user")
    public String profileByUser(@RequestParam String userId, Model model) {
        // 템플릿 대신 정적 페이지로 리다이렉트 (입력은 정적 페이지에서 처리)
        return "redirect:/test/test-profile.html";
    }

    private String extractEmail(String registrationId, Map<String, Object> attrs) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return (String) attrs.get("email");
        } else if ("naver".equalsIgnoreCase(registrationId)) {
            Object resp = attrs.get("response");
            if (resp instanceof Map<?,?> r) {
                Object email = r.get("email");
                return email != null ? email.toString() : null;
            }
        }
        return null;
    }

    private String safeUserId(User u) {
        try {
            var f = User.class.getDeclaredField("userId");
            f.setAccessible(true);
            Object v = f.get(u);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
