package com.busanit501.__team_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화 (Stateless 서버이므로)
        http.csrf(csrf -> csrf.disable());

        // CORS 설정 (WebConfig에서 설정했다면 여기서도 적용해주는 것이 좋음)
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 세션을 사용하지 않도록 설정 (Stateless)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // API 경로별 접근 권한 설정
        http.authorizeHttpRequests(authorize -> authorize
                // [핵심] '/api/users/signup' 경로는 누구나 접근 가능하도록 허용
                .requestMatchers("/api/users/signup").permitAll()
                // TODO: '/api/users/login' 경로도 추가로 허용해야 함
                // 그 외의 모든 요청은 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
        );

        // TODO: JWT 필터를 추가하는 로직이 여기에 있을 것입니다.
        // http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 프론트엔드 주소
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // TODO: PasswordEncoder Bean을 여기에 등록해야 합니다.
}