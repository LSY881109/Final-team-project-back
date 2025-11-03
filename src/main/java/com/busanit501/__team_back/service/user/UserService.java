package com.busanit501.__team_back.service.user;

import com.busanit501.__team_back.dto.user.UserSignUpRequest;
import org.springframework.web.multipart.MultipartFile;
// 회원가입
public interface UserService {

    // DTO와 프로필 이미지 파일을 받아 처리한다는 규칙을 명시
    void registerUser(UserSignUpRequest signUpRequestDto, MultipartFile profileImage);

    // TODO: 추후 추가될 기능들의 명세를 여기에 추가할 수 있습니다.
    // 예: UserDTO.UserResponseDto findUserById(Long id);
    // 예: void updateUser(UserDTO.UpdateRequestDto updateRequestDto);
    // 예: void deleteUser(Long id);
}