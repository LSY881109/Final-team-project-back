package com.busanit501.__team_back.security.oauth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.repository.maria.UserRepository;
import com.busanit501.__team_back.repository.maria.UserReadRepository;
import com.busanit501.__team_back.entity.MariaDB.OAuth2Account;
import com.busanit501.__team_back.repository.maria.OAuth2AccountRepository;

import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2UserAdapter {

    private final OAuth2AccountRepository oauthRepo;
    private final UserRepository userRepository;     // existsByUserId / existsByEmail 등 기존 메서드 활용
    private final UserReadRepository userReadRepo;   // findByEmail 전용(기존 인터페이스 수정 회피)
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public OAuth2UserAdapter(OAuth2AccountRepository oauthRepo,
                             UserRepository userRepository,
                             UserReadRepository userReadRepo) {
        this.oauthRepo = oauthRepo;
        this.userRepository = userRepository;
        this.userReadRepo = userReadRepo;
    }

    @Transactional
    public void upsertLinkAndEnsureUser(Map<String, Object> mapped) {
        String provider = (String) mapped.get("provider");
        String providerId = (String) mapped.get("providerId");
        String email = (String) mapped.get("email");
        String name = (String) mapped.getOrDefault("name", "User");

        Optional<OAuth2Account> link = oauthRepo.findByProviderAndProviderId(provider, providerId);
        if (link.isPresent()) return;

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email is required for social sign-in.");
        }

        // 기존 유저 조회 (기존 UserRepository 수정 피하려고 별도 ReadRepo 사용)
        User user = userReadRepo.findByEmail(email).orElseGet(() -> {
            // userId 필요: 이메일 prefix 또는 provider 기반으로 유니크 생성
            String baseUserId = (email.contains("@") ? email.substring(0, email.indexOf("@")) : (provider + '_' + providerId));
            String candidate = baseUserId;
            int suffix = 0;
            while (userRepository.existsByUserId(candidate)) {
                suffix++;
                candidate = baseUserId + '_' + suffix;
            }

            User u = User.builder()
                    .userId(candidate)
                    .password(encoder.encode(PasswordGenerator.random64())) // 소셜 계정 임시 난수 비번
                    .email(email)
                    .profileImageId(null)
                    .build();
            return userRepository.save(u);
        });

        OAuth2Account account = new OAuth2Account();
        account.setProvider(provider);
        account.setProviderId(providerId);
        account.setUser(user);
        oauthRepo.save(account);
    }
}
