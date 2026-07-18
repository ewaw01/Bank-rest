package com.example.bank_rest.dto;

import com.example.bank_rest.entity.Role;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record User(
    @NotNull(message = "Id is required param") Long id,
    String email,
    Role role,
    String username,
    Boolean isBlocked,
    List<Card> cards
) {
}
