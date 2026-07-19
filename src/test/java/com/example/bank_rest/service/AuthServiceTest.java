package com.example.bank_rest.service;

import com.example.bank_rest.dto.LoginRequestDto;
import com.example.bank_rest.dto.RegisterRequestDto;
import com.example.bank_rest.entity.Role;
import com.example.bank_rest.entity.UserEntity;
import com.example.bank_rest.exception.UserAlreadyExistException;
import com.example.bank_rest.repository.UserRepository;
import com.example.bank_rest.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("регистрация - успешно, пользователь не существует")
    void register_Success() {
        RegisterRequestDto request = new RegisterRequestDto("test@test.com", "password", "testuser");

        Mockito.when(userRepository.existsByEmail(request.email())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(new UserEntity());

        assertDoesNotThrow(() -> authService.register(request));

        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(request.email());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(UserEntity.class));
    }

    @Test
    @DisplayName("регистрация - пользователь уже существует, ошибка")
    void register_UserAlreadyExists_ThrowsException() {
        RegisterRequestDto request = new RegisterRequestDto("test@test.com", "password", "testuser");

        Mockito.when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(UserAlreadyExistException.class, () -> authService.register(request));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(UserEntity.class));
    }

    @Test
    @DisplayName("логин - успешно, возвращает токен")
    void login_Success() {
        LoginRequestDto request = new LoginRequestDto("test@test.com", "password");
        UserEntity user = new UserEntity();
        user.setEmail("test@test.com");
        user.setRole(Role.ROLE_USER);

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        Mockito.when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        Mockito.when(jwtUtil.generateToken(user.getEmail(), user.getRole())).thenReturn("jwtToken");

        String token = authService.login(request);

        assertNotNull(token);
        assertEquals("jwtToken", token);

        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(request.email());
        Mockito.verify(jwtUtil, Mockito.times(1)).generateToken(user.getEmail(), user.getRole());
    }

    @Test
    @DisplayName("логин - пользователь не найден, ошибка")
    void login_UserNotFound_ThrowsException() {
        LoginRequestDto request = new LoginRequestDto("test@test.com", "password");

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        Mockito.when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authService.login(request));

        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(request.email());
        Mockito.verify(jwtUtil, Mockito.never()).generateToken(Mockito.anyString(), Mockito.any(Role.class));
    }
}
