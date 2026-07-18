package com.example.bank_rest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        log.error("handleException:\n" + "Exception: " + e + "; Message: " + e.getMessage());

        return ResponseEntity.internalServerError().body(
                new ErrorResponseDto(
                        "Something went wrong",
                        e.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(exception = {
            IllegalStateException.class,
            BlockedUserException.class,
            UserAlreadyExistException.class,
    })
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException e) {
        log.error("IllegalStateException: {}", e.getMessage());

        return ResponseEntity.badRequest().body(
                new ErrorResponseDto(
                        "Bad request",
                        e.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponseDto> handleSecurity(SecurityException e) {
        log.error("SecurityException: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponseDto(
                        "Access denied",
                        e.getMessage(),
                        LocalDateTime.now()
                )
        );
    }
}
