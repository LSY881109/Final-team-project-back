package com.busanit501.__team_back.repository.maria;

import com.busanit501.__team_back.entity.MariaDB.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserCommandRepository extends JpaRepository<User, Long> {
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET profile_image_id = :pid WHERE id = :id", nativeQuery = true)
    int updateProfileImageId(@Param("id") Long id, @Param("pid") String profileImageId);
}

