package com.busanit501.__team_back.security.oauth;

import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.repository.maria.UserRepository;
import com.busanit501.__team_back.service.ProfileImageService;
import com.busanit501.__team_back.repository.mongo.ProfileImageRepository;
import com.busanit501.__team_back.entity.MongoDB.ProfileImage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ProfileImageService profileImageService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        if (!(event.getAuthentication() instanceof OAuth2AuthenticationToken token)) return;

        String registrationId = token.getAuthorizedClientRegistrationId(); // "google" | "naver"
        Map<String, Object> attrs = token.getPrincipal().getAttributes();
        String freshUrl = extractProfileImageUrl(registrationId, attrs);
        String email = extractEmail(registrationId, attrs);
        if (email == null) return;

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        String currentPid = getProfileImageId(user);
        if (currentPid == null || currentPid.isBlank()) return;

        // 1) 현재 Mongo 문서 로드
        var docOpt = profileImageRepository.findById(currentPid);
        if (docOpt.isEmpty()) {
            // 문서 없으면(예외 케이스) 새로 생성 시도
            if (freshUrl != null && !freshUrl.isBlank()) {
                profileImageService.overwriteOrCreateWithUrl(null, freshUrl);
            }
            return;
        }
        ProfileImage doc = docOpt.get();

        // 2) imageData 비었으면 즉시 채움
        boolean needFillBinary = (doc.getImageData() == null || doc.getImageData().getData() == null || doc.getImageData().getData().length == 0);

        // 3) URL 변경 시 같은 문서 id에 덮어쓰기
        boolean urlChanged = (freshUrl != null && !freshUrl.isBlank() && (doc.getImageUrl() == null || !freshUrl.equals(doc.getImageUrl())));

        if (needFillBinary || urlChanged) {
            String useUrl = urlChanged ? freshUrl : doc.getImageUrl();
            if (useUrl != null && !useUrl.isBlank()) {
                profileImageService.overwriteOrCreateWithUrl(currentPid, useUrl);
            }
        }
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

    private String extractProfileImageUrl(String registrationId, Map<String, Object> attrs) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return (String) attrs.get("picture");
        } else if ("naver".equalsIgnoreCase(registrationId)) {
            Object resp = attrs.get("response");
            if (resp instanceof Map<?,?> r) {
                Object url = r.get("profile_image");
                return url != null ? url.toString() : null;
            }
        }
        return null;
    }

    private String getProfileImageId(User user) {
        try {
            var f = User.class.getDeclaredField("profileImageId");
            f.setAccessible(true);
            Object v = f.get(user);
            return v != null ? v.toString() : null;
        } catch (Exception ignore) {
            return null;
        }
    }
}

