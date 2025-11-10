package com.busanit501.__team_back.service.user;

import com.busanit501.__team_back.dto.user.UserLoginRequest;
import com.busanit501.__team_back.dto.user.UserSignUpRequest;
import com.busanit501.__team_back.security.jwt.TokenInfo;
import org.springframework.web.multipart.MultipartFile;
// 회원가입
public interface UserService {

    // DTO와 프로필 이미지 파일을 받아 처리한다는 규칙을 명시
    void registerUser(UserSignUpRequest signUpRequestDto, MultipartFile profileImage);

    // 로그인 메소드 명세 추가
    TokenInfo login(UserLoginRequest loginRequest);
    // 예: void updateUser(UserDTO.UpdateRequestDto updateRequestDto);
    // 예: void deleteUser(Long id);
}