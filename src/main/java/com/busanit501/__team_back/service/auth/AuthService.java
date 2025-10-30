package com.busanit501.__team_back.service.auth;

import com.busanit501.__team_back.domain.user.entity.User;
import com.busanit501.__team_back.domain.user.repository.UserRepository;
import com.busanit501.__team_back.dto.user.UserLoginRequest;
import com.busanit501.__team_back.dto.user.UserLoginResponse;
import com.busanit501.__team_back.security.jwt.JwtTokenProvider;
import com.busanit501.__team_back.security.jwt.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 인증(로그인, 토큰 관리) 관련 비즈니스 로직을 처리하는 서비스 계층
 */
@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 로그인 요청 처리 (인증 및 JWT 토큰 발급)
     * @param request 로그인 요청 DTO (username, password)
     * @return JWT 토큰과 사용자 정보를 포함한 응답 DTO
     */
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("인증 서비스 시작: {}", request.getUsername());

        // 1. ID와 Password를 기반으로 인증 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        // 2. 실제 인증 수행 (ID/PW 불일치 시 AuthenticationException 발생)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        log.info("인증 성공: {}", authentication.getName());

        // 3. 인증 정보를 기반으로 JWT Access/Refresh Token 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 4. DB에서 사용자 상세 정보 조회
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("인증 후 사용자 정보 로드 실패");
        }
        User user = userOptional.get();

        // 5. 클라이언트에 보낼 UserLoginResponse DTO 구성
        return UserLoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                // JWT 토큰 정보
                .grantType(tokenInfo.getGrantType())
                .accessToken(tokenInfo.getAccessToken())
                .accessTokenExpiresIn(tokenInfo.getAccessTokenExpiresIn())
                .refreshToken(tokenInfo.getRefreshToken())
                .build();
    }
}