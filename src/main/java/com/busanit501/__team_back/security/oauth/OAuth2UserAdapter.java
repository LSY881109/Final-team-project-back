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

        // (1) 이미 소셜 연동이 있더라도 URL은 매 로그인마다 갱신
        if (link.isPresent()) {
            User linkedUser = link.get().getUser();
            syncProfileImage(linkedUser, pictureUrl);
            return;
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email is required for social sign-in.");
        }

        // (2) 기존 유저 조회 또는 생성
        User user = userReadRepo.findByEmail(email).orElseGet(() -> {
            String baseUserId = (email.contains("@") ? email.substring(0, email.indexOf("@")) : (provider + '_' + providerId));
            String candidate = baseUserId;
            int suffix = 0;
            while (userRepository.existsByUserId(candidate)) {
                suffix++;
                candidate = baseUserId + '_' + suffix;
            }

            // 최초 가입일 경우: 프로필 문서 먼저 만들고 FK 세팅(있을 때만)
            String profileImageId = null;
            if (pictureUrl != null && !pictureUrl.isBlank()) {
                try {
                    ProfileImage profileImage = new ProfileImage();
                    profileImage.setImageUrl(pictureUrl);
                    ProfileImage saved = profileImageRepository.save(profileImage);
                    profileImageId = saved.getId();
                    log.info("소셜 로그인 프로필 이미지 URL 저장 완료: {} (provider: {})", pictureUrl, provider);
                } catch (Exception e) {
                    log.warn("프로필 이미지 URL 저장 실패: {}", e.getMessage());
                }
            }

            User u = User.builder()
                    .userId(candidate)
                    .password(encoder.encode(PasswordGenerator.random64()))
                    .email(email)
                    .profileImageId(profileImageId) // Mongo 문서 PK(FK)
                    .build();
            return userRepository.save(u);
        });

        // (3) 기존 유저로 로그인한 경우에도 URL 동기화
        syncProfileImage(user, pictureUrl);

        OAuth2Account account = new OAuth2Account();
        account.setProvider(provider);
        account.setProviderId(providerId);
        account.setUser(user);
        oauthRepo.save(account);
    }

    /**
     * FK(users.profile_image_id)는 그대로 두고,
     * Mongo 문서의 imageUrl만 최신으로 맞춘다.
     * - FK가 있으면 해당 문서의 URL만 비교/갱신
     * - FK가 없거나 깨졌으면 새 문서 생성 후 FK 1회 세팅
     */
    private void syncProfileImage(User user, String newUrl) {
        if (newUrl == null || newUrl.isBlank()) return;

        String mongoId = user.getProfileImageId();

        if (mongoId != null && !mongoId.isBlank()) {
            profileImageRepository.findById(mongoId).ifPresentOrElse(doc -> {
                if (!newUrl.equals(doc.getImageUrl())) {
                    doc.setImageUrl(newUrl);
                    profileImageRepository.save(doc);
                    log.info("Mongo URL 갱신 완료: uid={}, mongoId={}", user.getId(), mongoId);
                }
            }, () -> {
                // 참조는 있는데 문서가 없으면(깨짐): 새로 만들고 FK 교체(한 번만)
                ProfileImage created = new ProfileImage();
                created.setImageUrl(newUrl);
                created = profileImageRepository.save(created);
                // setter가 없으니 복사-세이브(원본 구조 최소 변경)
                User updated = User.builder()
                        .id(user.getId())
                        .userId(user.getUserId())
                        .password(user.getPassword())
                        .email(user.getEmail())
                        .profileImageId(created.getId())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build();
                userRepository.save(updated);
                log.warn("깨진 FK 복구: {} -> {}", mongoId, created.getId());
            });
        } else {
            // FK가 비어있으면 새 문서 생성 후 FK 1회 세팅
            ProfileImage created = new ProfileImage();
            created.setImageUrl(newUrl);
            created = profileImageRepository.save(created);

            User updated = User.builder()
                    .id(user.getId())
                    .userId(user.getUserId())
                    .password(user.getPassword())
                    .email(user.getEmail())
                    .profileImageId(created.getId())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
            userRepository.save(updated);

            log.info("Mongo 최초 생성 & Maria FK 세팅: uid={}, mongoId={}", user.getId(), created.getId());
        }
    }
}

