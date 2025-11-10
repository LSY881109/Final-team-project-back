package com.busanit501.__team_back.security.oauth;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserAdapter oAuth2UserAdapter;

    public CustomOAuth2UserService(OAuth2UserAdapter oAuth2UserAdapter) {
        this.oAuth2UserAdapter = oAuth2UserAdapter;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User user = delegate.loadUser(req);

        String registrationId = req.getClientRegistration().getRegistrationId(); // google | naver
        Map<String, Object> attributes = user.getAttributes();

        Map<String, Object> mapped = mapAttributes(registrationId, attributes);
        // DB upsert & 연결하고 userId 반환 (DB에 저장된 실제 userId)
        String dbUserId = oAuth2UserAdapter.upsertLinkAndEnsureUser(mapped);
        
        // DB의 userId를 attributes에 추가 (JWT의 sub로 사용됨)
        mapped.put("userId", dbUserId);
        
        // 로깅: DB userId와 JWT sub가 일치하는지 확인
        System.out.println("🔍 OAuth2 로그인 - DB userId: " + dbUserId);
        System.out.println("🔍 OAuth2 로그인 - mapped.get('userId'): " + mapped.get("userId"));

        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                mapped,
                "userId"  // JWT의 sub가 userId가 되도록 변경
        );
        
        // 로깅: DefaultOAuth2User.getName()이 DB userId와 일치하는지 확인
        System.out.println("🔍 OAuth2 로그인 - oauth2User.getName(): " + oauth2User.getName());
        
        return oauth2User;
    }

    private Map<String, Object> mapAttributes(String registrationId, Map<String, Object> attrs) {
        String email = null, name = null, picture = null, providerId = null;

        if ("naver".equals(registrationId)) {
            Object rObj = attrs.get("response");
            if (rObj instanceof Map<?, ?> r) {
                email = asString(r.get("email"));
                name = asString(r.get("name"));
                picture = asString(r.get("profile_image"));
                providerId = asString(r.get("id"));
            }
        } else { // google
            email = asString(attrs.get("email"));
            name = asString(attrs.get("name"));
            picture = asString(attrs.get("picture"));
            providerId = asString(attrs.get("sub"));
        }

        Map<String, Object> out = new HashMap<>();
        out.put("email", email);
        out.put("name", name);
        out.put("picture", picture);
        out.put("provider", registrationId);
        out.put("providerId", providerId);
        return out;
    }

    private String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
