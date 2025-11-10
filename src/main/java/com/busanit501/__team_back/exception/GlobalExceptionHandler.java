package com.busanit501.__team_back.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    /**
     * 정적 리소스 파일(favicon, apple-touch-icon 등)이 없을 때 발생하는 예외 처리
     * 이 파일들은 없어도 기능에 영향이 없으므로 조용히 404로 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        // 정적 리소스 파일 요청인 경우에만 조용히 처리
        if (path.contains("favicon") || path.contains("apple-touch-icon") || 
            path.contains("robots.txt") || path.contains("sitemap.xml")) {
            // 로그를 남기지 않고 조용히 404 반환
            return ResponseEntity.notFound().build();
        }
        // 다른 리소스는 로그 남기고 처리
        log.warn("Resource not found at {}: {}", path, ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleOAuth2(OAuth2AuthenticationException ex, HttpServletRequest request) {
        log.error("OAuth2 error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        Map<String, Object> body = baseBody(HttpStatus.UNAUTHORIZED, request, ex);
        body.put("oauth2_error", ex.getError() != null ? ex.getError().toString() : null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex, HttpServletRequest request) {
        // 정적 리소스 관련 예외는 이미 위에서 처리했으므로 여기서는 로그만 남김
        String path = request.getRequestURI();
        if (path.contains("favicon") || path.contains("apple-touch-icon") || 
            path.contains("robots.txt") || path.contains("sitemap.xml")) {
            // 정적 리소스는 조용히 처리 (로그 남기지 않음)
            return ResponseEntity.notFound().build();
        }
        
        log.error("Unhandled error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(baseBody(status, request, ex));
    }

    private Map<String, Object> baseBody(HttpStatus status, HttpServletRequest request, Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("exception", ex.getClass().getName());
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        return body;
    }
}

