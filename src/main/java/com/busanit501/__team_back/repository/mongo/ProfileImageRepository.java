package com.busanit501.__team_back.repository.mongo;

import com.busanit501.__team_back.entity.MongoDB.ProfileImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// MongoRepository<[도큐먼트 클래스], [PK의 타입]>
public interface ProfileImageRepository extends MongoRepository<ProfileImage, String> {

    // 기본 CRUD (save, findById, deleteById 등)는 이미 내장되어 있습니다.
    // 특별히 필요한 쿼리 메소드가 있다면 여기에 추가할 수 있습니다.
    // 예를 들어, 특정 contentType의 이미지만 찾는 경우:
    // List<ProfileImage> findByContentType(String contentType);

    // 소셜 로그인 프로필 이미지 URL로 단일 문서 조회
    Optional<ProfileImage> findTopByImageUrl(String imageUrl);
}
