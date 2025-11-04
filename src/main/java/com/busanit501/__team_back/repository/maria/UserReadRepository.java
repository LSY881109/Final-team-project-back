package com.busanit501.__team_back.repository.maria;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.busanit501.__team_back.entity.MariaDB.User;

// 기존 UserRepository를 수정하지 않기 위해 같은 엔티티를 읽는 보조 리포지토리를 추가
public interface UserReadRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

