package com.busanit501.__team_back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestSignupController {
    @GetMapping("/test/signup")
    public String form() {
        return "test-signup";
    }
}

