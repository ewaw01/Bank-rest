package com.example.bank_rest.controller;

import com.example.bank_rest.dto.LoginRequestDto;
import com.example.bank_rest.dto.RegisterRequestDto;
import com.example.bank_rest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        log.info("Registration request: {}", request);
        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully registered!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        log.info("Login request: {}", request);
        return ResponseEntity.ok().body("Successfully logged in!\nYour token: " + authService.login(request));
    }
}
