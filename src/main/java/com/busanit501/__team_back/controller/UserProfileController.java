package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.repository.maria.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserProfileController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}/image")
    public ResponseEntity<?> profileImage(@PathVariable String userId) {
        return userRepository.findByUserId(userId)
            .map(u -> {
                String pid = getProfileImageId(u);
                if (pid == null || pid.isBlank()) return ResponseEntity.notFound().build();
                return ResponseEntity.status(302).header("Location", "/images/profile/" + pid).build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    private String getProfileImageId(Object user) {
        try {
            var f = user.getClass().getDeclaredField("profileImageId");
            f.setAccessible(true);
            Object v = f.get(user);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

