package com.busanit501.__team_back.repository;

import com.busanit501.__team_back.domain.mongo.FoodImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodImageRepository extends MongoRepository<FoodImage, String> {

    // 특정 사용자의 이미지 조회 (추후 회원 기능 추가시)
    List<FoodImage> findByUserId(String userId);

    // 날짜 범위로 조회
    List<FoodImage> findByUploadedAtBetween(LocalDateTime start, LocalDateTime end);

    // 음식 이름으로 조회
    List<FoodImage> findByFoodName(String foodName);
}