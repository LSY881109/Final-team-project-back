package com.busanit501.__team_back.security.oauth;

import com.busanit501.__team_back.security.jwt.JwtTokenProvider;
import com.busanit501.__team_back.security.jwt.TokenInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Log4j2
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 앱 딥링크 (AndroidManifest.xml의 scheme/host/pathPrefix와 일치)
    private static final String APP_SCHEME = "myapp://oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {
        try {
            // 1️⃣ 토큰 발급
            TokenInfo token = jwtTokenProvider.generateToken(authentication);
            String access = URLEncoder.encode(token.getAccessToken(), StandardCharsets.UTF_8);
            String refresh = URLEncoder.encode(token.getRefreshToken(), StandardCharsets.UTF_8);

            // 2️⃣ Flutter 앱으로 리다이렉트
            String redirect = APP_SCHEME + "?access=" + access + "&refresh=" + refresh;
            log.debug("OAuth2 success → {}", redirect);
            response.sendRedirect(redirect);

        } catch (Exception e) {
            log.error("OAuth2 success handler error", e);
            try {
                response.sendRedirect("/");
            } catch (Exception ignore) {}
        }
    }
}
