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
                .requestMatchers("/", "/login", "/error", "/oauth2/authorization/**", "/css/**", "/js/**", "/images/**").permitAll()
                // '/api/users/signup' 회원가입,로그인 경로는 누구나 접근 가능하도록
                .requestMatchers("/api/users/signup", "/api/users/login", "/api/map/**","/api/food-images/**").permitAll()
                // 이미지 분석 API는 테스트를 위해 인증 없이 접근 가능하도록 설정 (개발 환경)
                .requestMatchers("/api/analysis/**").permitAll()
                // YouTube 검색 API는 개발 환경에서 인증 없이 접근 가능 (프로덕션에서는 인증 필요)
                // ⚠️ 프로덕션 배포 시: 아래 줄을 주석 처리하여 인증이 필요하도록 변경
                .requestMatchers("/api/youtube/**").permitAll()
                // 관리자 페이지 API는 개발 환경에서 인증 없이 접근 가능하도록 설정
                .requestMatchers("/api/admin/**").permitAll()
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
        
        // ⚠️ 개발 환경: 모든 Origin 허용 (Flutter 앱은 Origin이 null일 수 있음)
        // ⚠️ 프로덕션 배포 시: 아래 주석 처리하고 특정 Origin만 허용하도록 변경 필수!
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 개발 환경: 모든 Origin 허용
        
        // 프로덕션 환경에서는 아래처럼 특정 Origin만 허용하도록 변경:
        // configuration.setAllowedOrigins(Arrays.asList(
        //         "https://your-production-domain.com",  // 프로덕션 도메인
        //         "https://www.your-production-domain.com"
        // ));
        // 개발 환경용 예시 (주석 처리됨):
        // configuration.setAllowedOrigins(Arrays.asList(
        //         "http://localhost:5173",           // React 개발 서버
        //         "http://127.0.0.1:8080",          // Flutter iOS 시뮬레이터
        //         "http://localhost:8080",          // Flutter (일부 환경)
        //         "http://10.0.2.2:8080"            // Flutter Android 에뮬레이터
        // ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // allowCredentials는 와일드카드와 함께 사용 시 문제가 될 수 있으므로 false로 설정
        // JWT 토큰은 Authorization 헤더로 전송되므로 credentials가 필요 없음
        configuration.setAllowCredentials(false);
        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);
        
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