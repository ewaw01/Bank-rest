package com.example.bank_rest.dto;

public record CardSearchFilter(
        Long id,
        String numberCard,
        Long balance,
        Integer pageSize,
        Integer pageNum
) {
}
