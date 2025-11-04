package com.busanit501.__team_back.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 토큰 정보 추출 및 유효성 검증을 담당하는 클래스
 */
@Component
@Log4j2
public class JwtTokenProvider {

    private final Key key;

    private static final String AUTHORITIES_KEY = "auth";
    private final String ACCESS_TOKEN_EXPIRE_TIME;
    private final String REFRESH_TOKEN_EXPIRE_TIME;

    // application.properties에서 secret key와 만료 시간을 로드합니다.
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expire-time}") String accessTokenExpireTime,
            @Value("${jwt.refresh-token-expire-time}") String refreshTokenExpireTime
    ) {
        // Base64로 인코딩된 시크릿 키를 디코딩하여 Key 객체로 만듭니다.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.ACCESS_TOKEN_EXPIRE_TIME = accessTokenExpireTime;
        this.REFRESH_TOKEN_EXPIRE_TIME = refreshTokenExpireTime;
    }

    /**
     * Authentication(인증 정보) 객체를 이용하여 Access Token과 Refresh Token을 생성합니다.
     */
    public TokenInfo generateToken(Authentication authentication) {

        // 1. 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 토큰 만료 시간 설정
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + Long.parseLong(ACCESS_TOKEN_EXPIRE_TIME));
        Date refreshTokenExpiresIn = new Date(now + Long.parseLong(REFRESH_TOKEN_EXPIRE_TIME));

        // 2. [수정] Access Token 생성 시, authorities가 비어있지 않을 때만 claim에 추가
        io.jsonwebtoken.JwtBuilder accessTokenBuilder = Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256);

        if (StringUtils.hasText(authorities)) {
            accessTokenBuilder.claim(AUTHORITIES_KEY, authorities);
        }
        String accessToken = accessTokenBuilder.compact();

        // 3. Refresh Token 생성 (Refresh Token에는 보통 권한 정보를 넣지 않음)
        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 4. 생성된 토큰 정보를 TokenInfo DTO에 담아 반환
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(Long.parseLong(ACCESS_TOKEN_EXPIRE_TIME))
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * JWT 토큰에서 인증 정보를 추출합니다.
     */
    public Authentication getAuthentication(String accessToken) {
        // 1. 토큰 복호화 및 클레임 추출
        Claims claims = parseClaims(accessToken);

        // [수정] 권한 정보가 없거나 비어있는 경우를 안전하게 처리
        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);
        Collection<? extends GrantedAuthority> authorities;

        if (authoritiesClaim == null || !StringUtils.hasText(authoritiesClaim.toString())) {
            authorities = Collections.emptyList(); // 권한이 없으면 빈 목록으로 설정
        } else {
            authorities = Arrays.stream(authoritiesClaim.toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // 3. [수정함] UserDetails 객체를 생성하여 Authentication 반환
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰의 유효성을 검사합니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }

    /**
     * 토큰 복호화 및 만료된 토큰에서도 클레임을 추출하는 메서드
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}