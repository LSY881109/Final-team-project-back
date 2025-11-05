package com.busanit501.__team_back.repository.maria;

import com.busanit501.__team_back.entity.MariaDB.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<[엔티티 클래스], [PK의 타입]>
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. 아이디로 사용자 정보 조회 (로그인 시 사용)
    Optional<User> findByUserId(String userId);

    // 이메일로 사용자 정보 조회
    Optional<User> findByEmail(String email);

    // 2. 아이디 중복 확인 (회원가입 시 사용)
    boolean existsByUserId(String userId);

    // 3. 이메일 중복 확인 (회원가입 시 사용)
    boolean existsByEmail(String email);
}
