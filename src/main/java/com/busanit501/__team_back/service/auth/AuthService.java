package com.busanit501.__team_back.service.auth;

import com.busanit501.__team_back.domain.user.APIUser;
import com.busanit501.__team_back.domain.user.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
     */
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("인증 서비스 시작: {}", request.getUsername());

        // 1. ID와 Password를 기반으로 인증 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        // 2. 실제 인증 수행 (UserDetailsService 호출)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        log.info("인증 성공: {}", authentication.getName());

        // 3. 인증 정보를 기반으로 JWT Access/Refresh Token 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 4. DB에서 사용자 상세 정보 조회
        // 🚩 수정 완료: findByMid()를 사용하여 DB에 저장된 mid 필드를 조회
        Optional<APIUser> userOptional = userRepository.findByMid(request.getUsername());

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("인증은 성공했으나, DB에서 사용자 정보 로드 실패 (MID: " + request.getUsername() + ")");
        }

        APIUser apiUser = userOptional.get();

        // 5. 클라이언트에 보낼 UserLoginResponse DTO 구성
        return UserLoginResponse.builder()
                .userId(apiUser.getMid())
                .username(apiUser.getUsername())
                .email("N/A (APIUser 엔티티에 이메일 필드 없음)")
                .grantType(tokenInfo.getGrantType())
                .accessToken(tokenInfo.getAccessToken())
                .accessTokenExpiresIn(tokenInfo.getAccessTokenExpiresIn())
                .refreshToken(tokenInfo.getRefreshToken())
                .build();
    }
}