package com.busanit501.__team_back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class PermitPathsSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain publicPaths(HttpSecurity http) throws Exception {
        http.securityMatcher("/images/**", "/test/**", "/profile/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/images/**", "/test/**", "/profile/**"))
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}

