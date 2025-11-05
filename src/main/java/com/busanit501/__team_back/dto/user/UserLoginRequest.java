package com.busanit501.__team_back.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class UserLoginRequest {
    @NotBlank(message = "사용자 ID는 필수 입력 항목입니다.")
    private String userId; // username -> userId

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;
}