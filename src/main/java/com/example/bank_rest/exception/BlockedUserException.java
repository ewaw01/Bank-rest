package com.example.bank_rest.exception;

public class BlockedUserException extends RuntimeException {
    public BlockedUserException(String message) {
        super(message);
    }
}
