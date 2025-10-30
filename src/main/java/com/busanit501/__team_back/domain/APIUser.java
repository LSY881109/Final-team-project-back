package com.busanit501.__team_back.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

//작업 순서7
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 사용 시 protected 기본 생성자 권장
@ToString
public class APIUser implements UserDetails { // 🚩 UserDetails 인터페이스 구현

    @Id // Primary Key 설정
    private String mid; // 사용자 ID (이것이 Spring Security의 username 역할을 합니다)

    private String mpw; // 사용자 비밀번호 (이것이 Spring Security의 password 역할을 합니다)

    // 몽고디비 추가
    // 몽고디비에 저장된 프로필 이미지의 File ID
    private String profileImg;

    // JWT에서 권한 정보를 가져올 때 사용할 필드 (예시로 ROLE_USER 권한만 부여)
    @Transient // DB에 저장하지 않는 필드임을 명시
    private final List<GrantedAuthority> authorities =
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));


    // 비밀번호 변경 메서드
    public void changePw(String mpw) {
        this.mpw = mpw;
    }
    public void changeProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    // =======================================================
    // 🚩 UserDetails 인터페이스 구현 메서드 (필수) 🚩
    // =======================================================

    // 1. 권한 목록을 반환합니다.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // 2. 사용자의 패스워드를 반환합니다.
    @Override
    public String getPassword() {
        return mpw; // 엔티티의 비밀번호 필드를 반환
    }

    // 3. 사용자의 이름을 반환합니다. (ID 역할을 수행)
    @Override
    public String getUsername() {
        return mid; // 엔티티의 사용자 ID 필드를 반환
    }

    // 4. 계정 만료 여부 (true: 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 5. 계정 잠김 여부 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 6. 패스워드 만료 여부 (true: 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 7. 계정 활성화 여부 (true: 활성화됨)
    @Override
    public boolean isEnabled() {
        return true;
    }
}