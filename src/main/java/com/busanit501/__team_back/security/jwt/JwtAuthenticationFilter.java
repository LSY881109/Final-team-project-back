package com.busanit501.__team_back.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * HTTP 요청당 한 번만 실행되며, JWT 토큰을 검증하고
 * Spring Security의 SecurityContext에 인증 정보를 설정하는 필터
 */
@Log4j2
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 토큰 생성/검증 로직을 가진 Provider를 주입받습니다.
    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 인증이 필요하지 않은 경로는 필터를 건너뜀
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/analysis/") || 
            requestPath.startsWith("/api/users/signup") ||
            requestPath.startsWith("/api/users/login") ||
            requestPath.startsWith("/api/youtube/") ||
            requestPath.startsWith("/api/map/") ||
            requestPath.startsWith("/api/food-images/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Request Header에서 JWT 토큰 추출
        String jwt = resolveToken(request);

        // 2. 토큰 유효성 검사
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

            // 3. 토큰이 유효할 경우, 토큰에서 인증(Authentication) 객체를 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);

            // 4. SecurityContext에 Authentication 객체 저장 (인증 완료 처리)
            // 이를 통해 이후 Spring Security가 현재 요청을 '인증된 상태'로 처리합니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 주의: JwtTokenProvider에는 getUserEmailFromToken() 메서드가 없으므로,
            // 로그는 토큰 검증 성공까지만 기록합니다. (이전 코드 수정)
            log.info("JWT 토큰 인증 성공 및 SecurityContext에 설정 완료.");
        }

        // 다음 필터로 요청을 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 정보를 추출하는 메서드
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // "Bearer " 부분을 제외한 실제 토큰 값만 반환
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}