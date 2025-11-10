package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.user.UserInfoResponse;
import com.busanit501.__team_back.dto.user.UserLoginRequest;
import com.busanit501.__team_back.dto.user.UserSignUpRequest;
import com.busanit501.__team_back.entity.MariaDB.OAuth2Account;
import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.repository.maria.OAuth2AccountRepository;
import com.busanit501.__team_back.repository.maria.UserRepository;
import com.busanit501.__team_back.security.jwt.TokenInfo;
import com.busanit501.__team_back.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users") // Maria DB의 users 테이블에 접근
@RequiredArgsConstructor
@Log4j2
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OAuth2AccountRepository oauth2AccountRepository;
    private final ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper 주입
    private final Validator validator;

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
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(signUpRequest);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("유효성 검사 오류: " + errorMsg);
            return ResponseEntity.badRequest().body(errorMsg);
        }

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

    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@RequestBody @Valid UserLoginRequest loginRequest) {
        TokenInfo tokenInfo = userService.login(loginRequest);
        return ResponseEntity.ok(tokenInfo);
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     * JWT 토큰에서 사용자 ID를 추출하여 사용자 정보를 반환합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("인증되지 않은 사용자의 정보 조회 요청");
                return ResponseEntity.status(401).build();
            }

            // JWT 토큰에서 userId (String) 추출
            Object principal = authentication.getPrincipal();
            String userIdString = null;

            if (principal instanceof UserDetails) {
                userIdString = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                userIdString = (String) principal;
            }

            if (userIdString == null || userIdString.isEmpty()) {
                log.warn("userId를 추출할 수 없습니다.");
                return ResponseEntity.status(401).build();
            }

            // userId로 User 조회
            Optional<User> userOpt = userRepository.findByUserId(userIdString);
            if (userOpt.isEmpty()) {
                log.warn("사용자를 찾을 수 없습니다. userId: {}", userIdString);
                return ResponseEntity.status(404).build();
            }

            User user = userOpt.get();
            
            log.info("🔍 사용자 정보 조회 - userId: {}, email: {}, user.id: {}", 
                    user.getUserId(), user.getEmail(), user.getId());

            // OAuth2 계정 정보 조회 (oauth2_account 테이블에서 user_id (FK)로 조회)
            // oauth2_account.user_id는 users.id (Long)를 참조합니다
            List<OAuth2Account> oauthAccounts = oauth2AccountRepository.findByUser(user);
            log.info("🔍 OAuth2 계정 조회 - user.id: {}, 조회된 계정 개수: {}", 
                    user.getId(), oauthAccounts.size());
            
            if (oauthAccounts.isEmpty()) {
                log.warn("⚠️ OAuth2 계정이 없습니다. 일반 가입 사용자일 수 있습니다.");
            } else {
                log.info("✅ OAuth2 계정 발견:");
                for (OAuth2Account account : oauthAccounts) {
                    log.info("   - provider: '{}', providerId: '{}', oauth2_account.user_id (FK): {}", 
                            account.getProvider(), account.getProviderId(), 
                            account.getUser().getId());
                }
            }
            
            // provider 목록 추출 (예: ["google", "naver"])
            List<String> providers = oauthAccounts.stream()
                    .map(OAuth2Account::getProvider)
                    .collect(Collectors.toList());
            
            boolean isOAuthUser = !providers.isEmpty();
            log.info("🔍 최종 응답 - providers: {}, isOAuthUser: {}", providers, isOAuthUser);
            
            // providers가 비어있지 않으면 OAuth2 사용자
            if (isOAuthUser) {
                log.info("✅ OAuth2 사용자로 판단 - providers: {}", providers);
            } else {
                log.info("ℹ️ 일반 가입 사용자로 판단");
            }

            UserInfoResponse response = UserInfoResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .oauthProviders(providers)
                    .isOAuthUser(!providers.isEmpty())
                    .profileImageId(user.getProfileImageId())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}