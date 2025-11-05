package com.busanit501.__team_back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectSignupAliasController {

    // 사용자가 실수로 /test/test-signup 로 접근한 경우 올바른 경로로 리다이렉트
    @GetMapping({"/test/test-signup", "/test-signup"})
    public String alias() {
        return "redirect:/test/signup";
    }
}

