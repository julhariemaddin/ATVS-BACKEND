package com.example.acquitance;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/me")
    public Authentication getMe(Authentication authentication) {
        return authentication;
    }
}
