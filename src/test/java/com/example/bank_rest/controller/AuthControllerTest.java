package com.example.bank_rest.controller;

import com.example.bank_rest.dto.LoginRequestDto;
import com.example.bank_rest.dto.RegisterRequestDto;
import com.example.bank_rest.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register - успешная регистрация")
    void register_Success() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto("test@test.com", "password", "testuser");

        doNothing().when(authService).register(any(RegisterRequestDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("Successfully registered!"));
    }

    @Test
    @DisplayName("POST /api/auth/login - успешный логин")
    void login_Success() throws Exception {
        LoginRequestDto request = new LoginRequestDto("test@test.com", "password");

        when(authService.login(any(LoginRequestDto.class))).thenReturn("jwtToken");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Successfully logged in!\nYour token: jwtToken"));
    }
}
