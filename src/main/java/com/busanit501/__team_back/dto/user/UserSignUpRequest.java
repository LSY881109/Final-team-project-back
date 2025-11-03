package com.busanit501.__team_back.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSignUpRequest {
    @NotBlank(message = "필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자의 영문과 숫자만 사용 가능합니다.")
    private String userId;

    @NotBlank(message = "필수 입력 항목입니다.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "필수 입력 항목입니다.")
    private String passwordConfirm;

    @NotBlank(message = "필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 프로필 이미지는 MultipartFile 타입으로 Controller에서 별도로 받으므로 추가X
}