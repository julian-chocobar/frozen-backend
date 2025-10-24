package com.enigcode.frozen_backend.common.exceptions_configs.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class BlockedUserException extends RuntimeException {
    public BlockedUserException(String message) {
        super(message);
    }
}