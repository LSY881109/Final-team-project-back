package com.busanit501.__team_back.entity.MariaDB;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@ToString(exclude = {"password"}) // ToString 생성 시 password 필드는 제외 (보안)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    // MongoDB의 프로필 이미지 원본을 불러올 주소값
    private String profileImageId;

    // 회원가입한 시간
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 회원정보변경한 시간
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
