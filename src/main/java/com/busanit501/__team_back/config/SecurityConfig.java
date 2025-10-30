package com.busanit501.__team_back.config;

import com.busanit501.__team_back.security.jwt.JwtAuthenticationFilter;
import com.busanit501.__team_back.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 핵심 설정 클래스
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 비밀번호 암호화 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈 등록 (AuthService에서 사용)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 구성
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. CSRF, Session 비활성화 및 StateLess 설정
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 2. 인증이 필요한 요청과 그렇지 않은 요청 설정
        http.authorizeHttpRequests(authorize -> authorize
                // 회원가입, 로그인 API, Swagger UI는 인증 없이도 접근 허용
                .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 나머지 모든 요청은 인증(JWT 토큰)이 필요합니다.
                .anyRequest().authenticated()
        );

        // 3. JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        log.info("Spring Security 설정이 JWT 필터와 함께 최종적으로 마무리되었습니다.");

        return http.build();
    }
}