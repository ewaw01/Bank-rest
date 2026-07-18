package com.example.bank_rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequestDto(
        @NotNull(message = "From card id is required")
        Long fromCardId,

        @NotNull(message = "To card id is required")
        Long toCardId,

        @Positive(message = "Amount must be positive")
        Long amount
) {

}
