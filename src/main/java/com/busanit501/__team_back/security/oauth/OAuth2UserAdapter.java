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

import java.util.List;
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
    public String upsertLinkAndEnsureUser(Map<String, Object> mapped) {
        String provider = (String) mapped.get("provider");
        String providerId = (String) mapped.get("providerId");
        String email = (String) mapped.get("email");
        String pictureUrl = (String) mapped.get("picture"); // 네이버/구글 프로필 이미지 URL

        Optional<OAuth2Account> link = oauthRepo.findByProviderAndProviderId(provider, providerId);

        // (1) 이미 소셜 연동이 있더라도 URL은 매 로그인마다 갱신
        if (link.isPresent()) {
            User linkedUser = link.get().getUser();
            syncProfileImage(linkedUser, pictureUrl);
            return linkedUser.getUserId(); // userId 반환
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

            log.info("🔍 OAuth2 신규 사용자 생성 - email: {}, provider: {}, 생성될 userId: {}", email, provider, candidate);

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
            User savedUser = userRepository.save(u);
            log.info("🔍 OAuth2 신규 사용자 저장 완료 - DB userId: {}", savedUser.getUserId());
            return savedUser;
        });
        
        if (user != null) {
            log.info("🔍 OAuth2 기존 사용자 조회 - email: {}, provider: {}, DB userId: {}", email, provider, user.getUserId());
        }

        // (3) 기존 유저로 로그인한 경우에도 URL 동기화
        syncProfileImage(user, pictureUrl);

        // (4) 해당 User가 이미 같은 provider로 OAuth2Account를 가지고 있는지 확인
        List<OAuth2Account> existingAccounts = oauthRepo.findByUser(user);
        boolean alreadyLinked = existingAccounts.stream()
                .anyMatch(acc -> provider.equals(acc.getProvider()));
        
        if (!alreadyLinked) {
            // 같은 provider로 연결된 계정이 없을 때만 새로 저장
            OAuth2Account account = new OAuth2Account();
            account.setProvider(provider);
            account.setProviderId(providerId);
            account.setUser(user);
            OAuth2Account savedAccount = oauthRepo.save(account);
            
            // 🔍 로깅: users 테이블과 oauth2_account 테이블의 관계 확인
            log.info("✅ OAuth2 계정 연결 완료:");
            log.info("   users.id (PK, Long): {}", user.getId());
            log.info("   users.user_id (String): {}", user.getUserId());
            log.info("   oauth2_account.id (PK): {}", savedAccount.getId());
            log.info("   oauth2_account.user_id (FK → users.id): {}", user.getId());
            log.info("   provider: {}, providerId: {}", provider, providerId);
            log.info("   ✅ oauth2_account.user_id는 users.id (Long)를 참조해야 합니다!");
        } else {
            log.info("이미 연결된 OAuth2 계정: userId={}, provider={}", user.getUserId(), provider);
        }
        
        // userId 반환 (JWT의 sub로 사용)
        String finalUserId = user.getUserId();
        log.info("🔍 OAuth2 최종 반환 userId: {} (DB에 저장된 값)", finalUserId);
        return finalUserId;
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

