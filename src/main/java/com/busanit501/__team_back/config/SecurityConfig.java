package com.busanit501.__team_back.config;

import com.busanit501.__team_back.security.jwt.JwtAuthenticationFilter; // <--- 새로 추가
import com.busanit501.__team_back.security.jwt.JwtTokenProvider; // <--- 새로 추가
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // <--- 새로 추가

/**
 * Spring Security 핵심 설정 클래스
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    // JWT 필터를 생성하기 위해 JwtTokenProvider를 주입
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
                // 회원가입, 로그인 API는 인증 없이도 접근 허용
                .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login").permitAll()
                // 나머지 모든 요청은 인증(JWT 토큰)이 필요합니다.
                .anyRequest().authenticated()
        );

        // 3. [수정사항 적용]: JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
        // 클라이언트 요청을 받을 때마다 헤더의 JWT 토큰을 검사합니다.
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider), // JWT 필터 인스턴스 생성
                UsernamePasswordAuthenticationFilter.class
        );

        // 4. (나중에) 인증/인가 실패 핸들러가 여기에 추가될 예정입니다.

        log.info("Spring Security 설정이 JWT 필터와 함께 최종적으로 마무리되었습니다.");

        return http.build();
    }
}
