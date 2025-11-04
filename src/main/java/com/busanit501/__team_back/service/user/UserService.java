package com.busanit501.__team_back.service.user;



import com.busanit501.__team_back.domain.user.APIUser; // 🚩 APIUser 엔티티 사용

import com.busanit501.__team_back.domain.user.UserRepository; // 🚩 UserRepository 사용

import com.busanit501.__team_back.dto.user.UserSignUpRequest;

import com.busanit501.__team_back.exception.DuplicateUsernameException; // 🚩 중복 예외 import

import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



/**

 * 사용자 관련 비즈니스 로직을 처리하는 서비스 계층

 * 파일명은 UserService.java 입니다.

 */

@Service

@RequiredArgsConstructor

@Log4j2

@Transactional

public class UserService { // 🚩 클래스 이름을 UserService로 복원



    // 의존성 주입: UserRepository (DB 접근)

    private final UserRepository userRepository;



    // 의존성 주입: PasswordEncoder (비밀번호 암호화)

    private final PasswordEncoder passwordEncoder;



    /**

     * 회원가입 로직 (중복 검사 로직 추가)

     * @param request 클라이언트로부터 받은 회원가입 DTO

     * @return 저장된 사용자 엔티티의 ID

     */

    public String signUp(UserSignUpRequest request) { // Long 대신 String (APIUser의 mid가 String이므로)

        log.info("회원가입 요청 처리 시작: 사용자 ID: {}", request.getUsername());



        // 1. 🚩 사용자 ID 중복 검사 로직 추가

        userRepository.findById(request.getUsername()).ifPresent(user -> {

            log.warn("ID 중복 발생: {}", request.getUsername());

            throw new DuplicateUsernameException("이미 존재하는 사용자 ID입니다: " + request.getUsername());

        });



        // 2. DTO를 Entity로 변환 및 비밀번호 암호화 적용

        APIUser user = APIUser.builder() // 🚩 APIUser 엔티티 사용

                .mid(request.getUsername()) // APIUser는 mid 필드를 사용

                .mpw(passwordEncoder.encode(request.getPassword())) // mpw 필드 사용

                // email은 APIUser에 없으므로, 필요하다면 APIUser에 추가해야 합니다.

                // 현재 코드에 맞춰 이메일은 일단 제외하고, mid/mpw만 사용합니다.

                .build();



        // 3. DB에 저장

        APIUser savedUser = userRepository.save(user);



        log.info("회원가입 완료: ID {}", savedUser.getMid());

        return savedUser.getMid();

    }



    // 이후 로그인, 사용자 정보 조회 등의 메서드가 추가될 예정입니다.

}