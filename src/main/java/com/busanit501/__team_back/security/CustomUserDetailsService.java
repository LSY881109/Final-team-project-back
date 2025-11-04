package com.busanit501.__team_back.security;

import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.repository.maria.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// Spring Security가 로그인 인증을 할 때, 우리 데이터베이스(users 테이블)에서
    // 사용자 정보를 어떻게 찾아와야 하는지 알려주는 안내해줌.
    // UserServiceImpl → Spring Security → ** CustomUserDetailsService **
        // → UserRepository(DB) 순서로 호출
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserId(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    // DB의 User 정보를 UserDetails 객체로 변환
    private UserDetails createUserDetails(User user) {
        // TODO: 추후 User에 Role(권한) 필드를 추가하고, 해당 권한을 부여하도록 수정해야 함
        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getPassword(),
                Collections.emptyList() // 우선은 권한 없음
        );
    }
}