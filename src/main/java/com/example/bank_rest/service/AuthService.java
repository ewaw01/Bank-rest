package com.example.bank_rest.service;

import com.example.bank_rest.dto.LoginRequestDto;
import com.example.bank_rest.dto.RegisterRequestDto;
import com.example.bank_rest.entity.Role;
import com.example.bank_rest.entity.UserEntity;
import com.example.bank_rest.exception.UserAlreadyExistException;
import com.example.bank_rest.repository.UserRepository;
import com.example.bank_rest.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequestDto request) {
        log.info("Register request: {}", request);
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistException("User with email " + request.email() + " already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);
        user.setUsername(request.username());
        user.setIsBlocked(false);

        userRepository.save(user);
    }

    public String login(LoginRequestDto request) {
        log.info("Login request: {}", request);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserEntity user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new NoSuchElementException("User with email " + request.email() + " not found")
        );

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return token;
    }
}
