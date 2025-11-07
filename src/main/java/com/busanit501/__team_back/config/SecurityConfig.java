package com.busanit501.__team_back.config;

import com.busanit501.__team_back.security.jwt.JwtAuthenticationFilter;
import com.busanit501.__team_back.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import com.busanit501.__team_back.security.oauth.CustomOAuth2UserService;
import com.busanit501.__team_back.security.oauth.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화 (Stateless 서버이므로)
        http.csrf(csrf ->
                csrf.disable());

        // CORS 설정 (WebConfig에서 설정했다면 여기서도 적용해주는 것이 좋음)
        http.cors(cors ->
                cors.configurationSource(corsConfigurationSource()));

        // 세션을 사용하지 않도록 설정 (Stateless)
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // API 경로별 접근 권한 설정
        http.authorizeHttpRequests(authorize -> authorize
                // [핵심] '/api/users/signup' 경로는 누구나 접근 가능하도록 허용
                .requestMatchers("/api/users/signup").permitAll()
                // 추가: 소셜 로그인 및 정적 자원 허용
                .requestMatchers("/", "/login", "/oauth2/authorization/**", "/css/**", "/js/**", "/images/**").permitAll()
                // '/api/users/signup' 회원가입,로그인 경로는 누구나 접근 가능하도록
                .requestMatchers("/api/users/signup", "/api/users/login", "/api/map/**","/api/food-images/**").permitAll()
                // 이미지 분석 API는 테스트를 위해 인증 없이 접근 가능하도록 설정
                .requestMatchers("/api/analysis/**").permitAll()
                // 혹시 클라이언트가 /api/auth/** 로 부르면 이것도 같이 열어두기
                .requestMatchers("/api/auth/**").permitAll()
                // 그 외의 모든 요청은 인증된 사용자만 접근 가능
                .anyRequest().authenticated()

        );

        // 추가: OAuth2 Login 설정 (ApplicationContext에서 Bean 획득, 기존 체이닝 유지)
        var appCtx = http.getSharedObject(org.springframework.context.ApplicationContext.class);
        CustomOAuth2UserService customOAuth2UserService = appCtx.getBean(CustomOAuth2UserService.class);
        OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler = appCtx.getBean(OAuth2LoginSuccessHandler.class);
        http.oauth2Login(o -> o
                .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
        );

        // TODO: JWT 필터를 추가하는 로직이 여기에 있을 것입니다.
        // http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        // JWT필터
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS정책
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // React 개발 서버 포트들 허용
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // PasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}