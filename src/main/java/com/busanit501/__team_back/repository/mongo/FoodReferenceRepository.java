package com.busanit501.__team_back.repository.mongo;

import com.busanit501.__team_back.entity.MongoDB.FoodReference;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

//관리자가 입력한 표준 영양 정보(`FoodReference`)를 관리할 리포지토리
public interface FoodReferenceRepository extends MongoRepository<FoodReference, String> {

    // 음식 이름으로 영양 정보를 조회하는 메소드
    // AI가 음식 이름을 알려주면, 이 메소드를 호출하여 해당 음식의 영양 정보를 찾습니다.
    Optional<FoodReference> findByFoodName(String foodName);

}

//findByFoodName: AI 분석 결과로 나온 음식 이름(예: "파스타")을 이용해
// DB에서 해당 음식의 칼로리, 단백질 등의 정보를 찾아오기 위해 사용

//Optional<T>을 사용하여 조회 결과가 없을 경우(DB에 등록되지 않은 음식인 경우)를 안전하게 처리