package com.busanit501.__team_back.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleOAuth2(OAuth2AuthenticationException ex, HttpServletRequest request) {
        log.error("OAuth2 error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        Map<String, Object> body = baseBody(HttpStatus.UNAUTHORIZED, request, ex);
        body.put("oauth2_error", ex.getError() != null ? ex.getError().toString() : null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex, HttpServletRequest request) {
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

