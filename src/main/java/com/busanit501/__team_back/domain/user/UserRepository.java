package com.busanit501.__team_back.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * APIUser 엔티티에 대한 데이터 접근 계층 (Repository)
 */
public interface UserRepository extends JpaRepository<APIUser, String> {

    // 🚩 JPA 오류 방지를 위해 엔티티의 실제 필드(mid)를 사용하도록 메서드 수정
    Optional<APIUser> findByMid(String mid);

    // findByUsername 메서드는 삭제됨
}