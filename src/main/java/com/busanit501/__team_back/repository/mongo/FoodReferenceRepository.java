package com.busanit501.__team_back.repository.mongo;

import com.busanit501.__team_back.domain.mongo.FoodReference;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FoodReferenceRepository extends MongoRepository<FoodReference, String> {

    /**
     * 음식 이름으로 FoodReference 문서를 조회합니다.
     * @param foodName 조회할 음식의 이름
     * @return Optional<FoodReference> 객체
     */
    Optional<FoodReference> findByFoodName(String foodName);
    //Optional은 "결과가 있을 수도, 없을 수도 있다"는 것을 의미
}
