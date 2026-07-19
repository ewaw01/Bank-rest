package com.example.bank_rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateCardBalanceRequestDto(
        @NotNull(message = "Card ID is required")
        Long cardId,

        @PositiveOrZero(message = "Balance must be positive or zero")
        Long newBalance
) {
}
