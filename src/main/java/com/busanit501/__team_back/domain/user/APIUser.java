package com.busanit501.__team_back.domain.user;

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

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class APIUser implements UserDetails {

    @Id
    private String mid;

    private String mpw;

    private String profileImg;

    @Transient
    private final List<GrantedAuthority> authorities =
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    public void changePw(String mpw) {
        this.mpw = mpw;
    }
    public void changeProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    // 🚩 UserDetails 구현 메서드: mid 필드를 사용자 ID로 사용
    @Override
    public String getUsername() {
        return mid;
    }

    @Override
    public String getPassword() {
        return mpw;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}