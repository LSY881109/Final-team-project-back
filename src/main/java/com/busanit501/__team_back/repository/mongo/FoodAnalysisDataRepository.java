package com.busanit501.__team_back.repository.mongo;

import com.busanit501.__team_back.entity.MongoDB.FoodAnalysisData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
//AI 학습용 원본 이미지(entity -> FoodAnalysisData.java)
public interface FoodAnalysisDataRepository extends MongoRepository<FoodAnalysisData, String> {

    // 특정 음식 카테고리에 해당하는 모든 이미지 데이터를 찾는 메소드 (추후 데이터 관리용)
    List<FoodAnalysisData> findByFoodCategory(String foodCategory);
}