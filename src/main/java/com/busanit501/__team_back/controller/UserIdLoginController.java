package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.user.UserLoginRequest;
import com.busanit501.__team_back.repository.maria.UserRepository;
import com.busanit501.__team_back.security.jwt.TokenInfo;
import com.busanit501.__team_back.service.user.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Log4j2
@Validated
public class UserIdLoginController {

    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/login-by-id")
    public ResponseEntity<?> loginByUserId(@RequestBody @Validated UserIdLoginRequest req) {
        return userRepository.findByUserId(req.userId())
                .map(u -> {
                    try {
                        UserLoginRequest loginReq = new UserLoginRequest();
                        loginReq.setUserId(req.userId());
                        loginReq.setPassword(req.password());
                        TokenInfo token = userService.login(loginReq);
                        return ResponseEntity.ok(token);
                    } catch (Exception e) {
                        log.error("login-by-id failed", e);
                        return ResponseEntity.status(401).body("Invalid credentials");
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }

    public record UserIdLoginRequest(@NotBlank String userId, @NotBlank String password) {}
}

