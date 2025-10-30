package com.busanit501.__team_back.service.user;

import com.busanit501.__team_back.domain.user.entity.User;
import com.busanit501.__team_back.domain.user.UserRepository;
import com.busanit501.__team_back.dto.user.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder; // <--- PasswordEncoder import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 계층
 */
@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class UserService {

    // 의존성 주입: UserRepository (DB 접근)
    private final UserRepository userRepository;

    // 의존성 주입: PasswordEncoder (비밀번호 암호화)
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 로직
     * @param request 클라이언트로부터 받은 회원가입 DTO
     * @return 저장된 사용자 엔티티의 ID
     */
    public Long signUp(UserSignUpRequest request) {
        log.info("회원가입 요청 처리 시작: 사용자 ID: {}", request.getUsername());

        // 1. (나중에) 사용자 ID 중복 검사 로직 추가 예정

        // 2. DTO를 Entity로 변환 및 비밀번호 암호화 적용 (핵심 수정 사항)
        //
        User user = User.builder()
                .username(request.getUsername())
                // 평문 비밀번호를 Spring Security의 BCrypt로 암호화하여 저장
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        // 3. DB에 저장
        User savedUser = userRepository.save(user);

        log.info("회원가입 완료: ID {}", savedUser.getUserId());
        return savedUser.getUserId();
    }

    // 이후 로그인, 사용자 정보 조회 등의 메서드가 추가될 예정입니다.
}