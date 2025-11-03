package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.user.UserSignUpRequest;
import com.busanit501.__team_back.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper 주입

    // consumes : 들어오는 데이터 타입을 명시. multipart/form-data 타입만 허용.
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @Valid
            @RequestParam("signupData") String signupDataJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("회원가입 요청 수신: " + signupDataJson);
        log.info("프로필 이미지: " + (profileImage != null ? profileImage.getOriginalFilename() : "없음"));

        UserSignUpRequest signUpRequest;
        try {
            // [수정] 수신한 JSON 문자열을 DTO 객체로 직접 변환합니다.
            signUpRequest = objectMapper.readValue(signupDataJson, UserSignUpRequest.class);
        } catch (Exception e) {
            log.error("JSON 파싱 오류", e);
            return ResponseEntity.badRequest().body("요청 데이터 형식이 올바르지 않습니다.");
        }

        // DTO 유효성 검사 실패 시 처리
//        if (bindingResult.hasErrors()) {
//            String errorMsg = bindingResult.getFieldErrors().stream()
//                    .map(FieldError::getDefaultMessage)
//                    .collect(Collectors.joining(", "));
//            log.warn("유효성 검사 오류: " + errorMsg);
//            return ResponseEntity.badRequest().body(errorMsg);
//        }

        // 비밀번호와 비밀번호 확인 일치 여부 검사
        if (!signUpRequest.getPassword().equals(signUpRequest.getPasswordConfirm())) {
            log.warn("비밀번호 불일치 오류");
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        try {
            userService.registerUser(signUpRequest, profileImage);
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            // Service 계층에서 발생한 중복 관련 예외 처리
            log.error("회원가입 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예상치 못한 예외 처리
            log.error("서버 내부 오류 발생", e);
            return ResponseEntity.internalServerError().body("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}