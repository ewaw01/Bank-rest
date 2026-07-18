package com.example.bank_rest.dto;

import com.example.bank_rest.entity.CardStatus;

import java.time.LocalDate;

public record Card(
        Long id,
        String maskedNumber,
        String owner,
        LocalDate expiryDate,
        CardStatus status,
        Long balance,
        Long userId
) {
}
