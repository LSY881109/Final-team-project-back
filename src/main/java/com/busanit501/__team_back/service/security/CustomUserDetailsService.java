package com.busanit501.__team_back.service.security;

import com.busanit501.__team_back.domain.user.entity.User;
import com.busanit501.__team_back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security가 사용자 인증 정보를 로드할 때 사용하는 커스텀 서비스
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * username (사용자 ID)을 기반으로 DB에서 UserDetails (사용자 정보)를 로드합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername 호출됨: {}", username);

        // 1. UserRepository를 사용하여 DB에서 사용자 정보를 조회합니다.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: {}", username);
                    // 사용자가 없으면 Spring Security 전용 예외를 발생시킵니다.
                    return new UsernameNotFoundException("사용자 ID를 찾을 수 없습니다: " + username);
                });

        log.info("사용자 정보 로드 성공: {}", user.getUsername());

        // 2. User 엔티티 자체가 UserDetails 인터페이스를 구현하고 있다고 가정하고 반환합니다.
        //    (User 엔티티에 UserDetails 구현이 아직 안 되어 있다면, 나중에 수정해야 합니다.)
        return user;
    }
}