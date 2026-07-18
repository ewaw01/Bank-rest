package com.example.bank_rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Email is required")@Email String email,
        @NotBlank(message = "Password is required") String password
) {
}
