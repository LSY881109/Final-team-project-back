package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.repository.maria.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/api")
public class TestProfileApiController {

    private final UserRepository userRepository;

    /** 현재 로그인 세션(가능하면 OAuth2) 기준으로 사용자/이미지 URL 반환 */
    @GetMapping("/me")
    public Map<String, Object> me(Principal principal, Authentication authentication) {
        Map<String, Object> out = new HashMap<>();
        Optional<User> userOpt = Optional.empty();

        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            String registrationId = oauth.getAuthorizedClientRegistrationId();
            String email = extractEmail(registrationId, oauth.getPrincipal().getAttributes());
            if (StringUtils.hasText(email)) {
                userOpt = userRepository.findByEmail(email);
            }
        } else if (principal != null && StringUtils.hasText(principal.getName())) {
            String name = principal.getName();
            userOpt = userRepository.findByUserId(name);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(name);
            }
        }

        if (userOpt.isPresent()) {
            String userId = getField(userOpt.get(), "userId");
            out.put("resolved", true);
            out.put("userId", userId);
            out.put("imageUrl", "/profile/" + userId + "/image");
        } else {
            out.put("resolved", false);
        }
        return out;
    }

    /** userId로 직접 조회 (로그인 없이 확인용) */
    @GetMapping("/by-user")
    public Map<String, Object> byUser(@RequestParam String userId) {
        Map<String, Object> out = new HashMap<>();
        userRepository.findByUserId(userId).ifPresent(u -> {
            out.put("resolved", true);
            out.put("userId", userId);
            out.put("imageUrl", "/profile/" + userId + "/image");
        });
        out.putIfAbsent("resolved", false);
        return out;
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

    private String getField(User user, String field) {
        try {
            var f = User.class.getDeclaredField(field);
            f.setAccessible(true);
            Object v = f.get(user);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
