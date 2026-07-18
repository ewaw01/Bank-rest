package com.example.bank_rest.exception;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String message,
        String detailMessage,
        LocalDateTime time
) {
}
