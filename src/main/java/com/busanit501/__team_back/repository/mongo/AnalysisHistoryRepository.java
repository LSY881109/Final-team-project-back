package com.busanit501.__team_back.repository.mongo;

import com.busanit501.__team_back.entity.MongoDB.AnalysisHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

//사용자의 분석 기록(AnalysisHistory)을 관리 <- 마이페이지에 사용됨.
public interface AnalysisHistoryRepository extends MongoRepository<AnalysisHistory, String> {

    // 분석 기록을 최신순으로 페이징하여 조회하는 메소드
    // 마이페이지에서 '더보기'와 같은 무한 스크롤 기능을 구현할 때 필수적입니다.
    Page<AnalysisHistory> findByUserIdOrderByAnalysisDateDesc(Long userId,
                                                              Pageable pageable);
}
// *   Spring Data의 `Pageable` 객체를 파라미터로 넘겨주면,
//  자동으로 `limit`과 `offset` (혹은 `skip`) 쿼리를 적용하여 페이징을 처리.

// *   `findByUserIdOrderByAnalysisDateDesc`:
// "특정 `userId`에 대해, `analysisDate`를 기준으로 내림차순(최신순)으로 정렬하여 찾아라"
// 라는 의미의 쿼리 메소드