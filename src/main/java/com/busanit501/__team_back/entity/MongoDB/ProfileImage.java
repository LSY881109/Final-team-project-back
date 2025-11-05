package com.busanit501.__team_back.entity.MongoDB;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "profile_images")
public class ProfileImage {

    @Id
    private String id; // MongoDB의 _id 필드와 매핑

    private Binary imageData; // 이미지 바이너리 데이터
    private String contentType; // 이미지 타입 (e.g., "image/png")
    private String imageUrl; // 소셜 로그인 프로필 이미지 URL (네이버/구글)
}
