package com.enigcode.frozen_backend.common.exceptions_configs.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

