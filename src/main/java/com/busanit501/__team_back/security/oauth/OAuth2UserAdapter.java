package com.busanit501.__team_back.security.oauth;

import com.busanit501.__team_back.entity.MongoDB.ProfileImage;
import com.busanit501.__team_back.repository.mongo.ProfileImageRepository;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class OAuth2UserAdapter {

    private final OAuth2AccountRepository oauthRepo;
    private final UserRepository userRepository;     // existsByUserId / existsByEmail 등 기존 메서드 활용
    private final UserReadRepository userReadRepo;   // findByEmail 전용(기존 인터페이스 수정 회피)
    private final ProfileImageRepository profileImageRepository; // 프로필 이미지 저장용
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public OAuth2UserAdapter(OAuth2AccountRepository oauthRepo,
                             UserRepository userRepository,
                             UserReadRepository userReadRepo,
                             ProfileImageRepository profileImageRepository) {
        this.oauthRepo = oauthRepo;
        this.userRepository = userRepository;
        this.userReadRepo = userReadRepo;
        this.profileImageRepository = profileImageRepository;
    }

    @Transactional
    public void upsertLinkAndEnsureUser(Map<String, Object> mapped) {
        String provider = (String) mapped.get("provider");
        String providerId = (String) mapped.get("providerId");
        String email = (String) mapped.get("email");
        String name = (String) mapped.getOrDefault("name", "User");
        String pictureUrl = (String) mapped.get("picture"); // 네이버/구글 프로필 이미지 URL

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

            // 프로필 이미지 URL이 있으면 MongoDB에 저장
            String profileImageId = null;
            if (pictureUrl != null && !pictureUrl.isBlank()) {
                try {
                    ProfileImage profileImage = new ProfileImage();
                    profileImage.setImageUrl(pictureUrl); // URL만 저장
                    ProfileImage saved = profileImageRepository.save(profileImage);
                    profileImageId = saved.getId();
                    log.info("소셜 로그인 프로필 이미지 URL 저장 완료: {} (provider: {})", pictureUrl, provider);
                } catch (Exception e) {
                    log.warn("프로필 이미지 URL 저장 실패: {}", e.getMessage());
                    // 실패해도 계속 진행 (null로 저장)
                }
            }

            User u = User.builder()
                    .userId(candidate)
                    .password(encoder.encode(PasswordGenerator.random64())) // 소셜 계정 임시 난수 비번
                    .email(email)
                    .profileImageId(profileImageId) // URL이 저장된 이미지 ID 설정
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
