package com.example.bank_rest.dto;

public record UserSearchFilter(
        Long id,
        String email,
        String username,
        Integer pageNum,
        Integer pageSize
) {
}
