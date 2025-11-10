package com.busanit501.__team_back.service.user;

import com.busanit501.__team_back.dto.user.UserLoginRequest;
import com.busanit501.__team_back.dto.user.UserSignUpRequest;
import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.repository.maria.UserRepository;
import com.busanit501.__team_back.security.jwt.JwtTokenProvider;
import com.busanit501.__team_back.security.jwt.TokenInfo;
import com.busanit501.__team_back.service.user.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FileService fileService;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public void registerUser(UserSignUpRequest signUpRequestDto, MultipartFile profileImage) {
        log.info("UserServiceImpl - registerUser 실행...");

        // 아이디 및 이메일 중복 검사
        if (userRepository.existsByUserId(signUpRequestDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 프로필 이미지 저장
        String profileImageId = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profileImageId = fileService.storeImage(profileImage);
            } catch (IOException e) {
                log.error("프로필 이미지 저장 실패", e);
                // 예외 처리 전략에 따라 사용자 정의 예외를 던지거나 할 수 있습니다.
                throw new RuntimeException("프로필 이미지 저장에 실패했습니다.");
            }
        }

        User user = User.builder()
                .userId(signUpRequestDto.getUserId())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword())) // 암호화 적용
                .email(signUpRequestDto.getEmail())
                .profileImageId(profileImageId)
                .build();

        userRepository.save(user);

        log.info("회원가입 로직 처리 완료: " + user.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public TokenInfo login(UserLoginRequest loginRequest) {

        // 1. Login ID/PW를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUserId(), loginRequest.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        // authenticate() 메소드가 실행될 때 CustomUserDetailsService의 loadUserByUsername 메소드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // Refresh Token을 DB에 저장 할지 말지 선택하면됨.
        // redisRepository.save(authentication.getName(), tokenInfo.getRefreshToken());

        return tokenInfo;
    }
}