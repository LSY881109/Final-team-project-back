package com.busanit501.__team_back.domain.user;

import com.busanit501.__team_back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 Entity 관련 데이터베이스 접근 계층
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // [수정사항 적용]: Spring Data JPA의 Query Method - username 중복 검사
    boolean existsByUsername(String username);

    // username으로 사용자 정보를 조회하는 메서드 (로그인 시 사용 예정)
    Optional<User> findByUsername(String username);
}