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

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
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

        // 2. Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 3. Refresh Token 생성
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

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 2. 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 3. UserDetails 객체를 생성하여 Authentication 반환
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