package com.example.bank_rest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CardRequestDto(
        @NotNull(message = "Owner id is required param") Long ownerId,
        @NotNull(message = "Expiry date is required param") LocalDate expiryDate
) {
}
