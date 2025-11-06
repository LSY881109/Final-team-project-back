package com.busanit501.__team_back.service;

import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.entity.MongoDB.ProfileImage;
import com.busanit501.__team_back.repository.maria.UserRepository;
import com.busanit501.__team_back.repository.mongo.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProfileImageSyncService {

    private final ProfileImageRepository profileImageRepository;
    private final UserRepository userRepository;

    /** 소셜 로그인 직후 호출: Mongo URL만 최신화 (Maria FK는 보통 그대로) */
    public void syncOnLogin(User user, String provider, String newUrl) {
        if (newUrl == null || newUrl.isBlank()) {
            log.debug("프로필 URL 없음 → 동기화 생략");
            return;
        }

        try {
            String mongoId = user.getProfileImageId();

            if (mongoId != null && !mongoId.isBlank()) {
                // 1) 기존 문서 찾기
                profileImageRepository.findById(mongoId).ifPresentOrElse(doc -> {
                    if (!newUrl.equals(doc.getImageUrl())) {
                        doc.setImageUrl(newUrl);
                        // 필요하다면 업데이트 시각 필드 하나 추가해서 찍어도 됨
                        profileImageRepository.save(doc);
                        log.info("Mongo URL 갱신 완료: uid={}, mongoId={}", user.getId(), mongoId);
                    }
                }, () -> {
                    // 참조는 있는데 문서가 없을 때: 새 문서 생성 후 FK 갈아끼우기(1회 복구)
                    ProfileImage created = new ProfileImage();
                    created.setImageUrl(newUrl);
                    // imageData/contentType은 건드리지 않음(원본 최소 변경)
                    created = profileImageRepository.save(created);

                    // 선택 1) 전용 업데이트 쿼리(권장)
                    userRepository.updateProfileImageId(user.getId(), created.getId());

                    // 선택 2) 엔티티 복사-세이브(Setter 없으면 빌더로 재구성) — 원본 변경 최소 원칙에선 1) 추천
                    log.warn("깨진 참조 복구: 기존 mongoId={} 미발견 → 새 {}로 교체", mongoId, created.getId());
                });

            } else {
                // 2) FK가 비어있다면: 새 문서 만들고 FK 1회 세팅
                ProfileImage created = new ProfileImage();
                created.setImageUrl(newUrl);
                created = profileImageRepository.save(created);
                userRepository.updateProfileImageId(user.getId(), created.getId());
                log.info("Mongo 최초 생성 & Maria FK 세팅: uid={}, mongoId={}", user.getId(), created.getId());
            }

        } catch (Exception e) {
            // 로그인 진행 막지 않기
            log.warn("프로필 동기화 오류(무시하고 로그인 진행): uid={}, msg={}", user.getId(), e.getMessage());
        }
    }
}
