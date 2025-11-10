package com.busanit501.__team_back.repository.mongo;

import com.busanit501.__team_back.entity.MongoDB.FoodReference;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

//관리자가 입력한 표준 영양 정보(`FoodReference`)를 관리할 리포지토리
public interface FoodReferenceRepository extends MongoRepository<FoodReference, String> {

    /**
     * 음식 이름으로 FoodReference 문서를 조회합니다.
     * @param foodName 조회할 음식의 이름
     * @return Optional<FoodReference> 객체
     */
    Optional<FoodReference> findByFoodName(String foodName);
    //Optional은 "결과가 있을 수도, 없을 수도 있다"는 것을 의미
}

//findByFoodName: AI 분석 결과로 나온 음식 이름(예: "파스타")을 이용해
// DB에서 해당 음식의 칼로리, 단백질 등의 정보를 찾아오기 위해 사용

//Optional<T>을 사용하여 조회 결과가 없을 경우(DB에 등록되지 않은 음식인 경우)를 안전하게 처리