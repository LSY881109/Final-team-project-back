package com.busanit501.__team_back.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class TestDiagnosticsController {

    // 1) 리다이렉트/시큐리티 영향 없이 바로 응답 확인
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    // 2) Thymeleaf 없이 정적 HTML 폼으로 우회
    @GetMapping("/signup-static")
    public ResponseEntity<Void> staticForm() {
        // /static/test/test-signup.html 로 이동 (정적 리소스 경로)
        return ResponseEntity.status(302)
                .location(URI.create("/test/test-signup.html"))
                .build();
    }

    // 3) 현재 요청 정보 확인(쿠키/헤더) — 루프 원인 추적에 사용
    @GetMapping(value = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> echo(HttpServletRequest req) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("method", req.getMethod());
        out.put("uri", req.getRequestURI());
        out.put("query", req.getQueryString());
        out.put("remote", req.getRemoteAddr());
        Map<String, String> headers = new LinkedHashMap<>();
        var names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            headers.put(n, req.getHeader(n));
        }
        out.put("headers", headers);
        Cookie[] cookies = req.getCookies();
        out.put("cookies", cookies != null ? Arrays.stream(cookies).map(c -> c.getName()+"="+c.getValue()).toArray(String[]::new) : new String[0]);
        return ResponseEntity.ok(out);
    }
}

