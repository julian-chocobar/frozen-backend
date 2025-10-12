package com.enigcode.frozen_backend.common.exceptions_configs.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un recurso (ej. Material, Movement)
 * no puede ser encontrado por su ID.
 * Capturada globalmente para devolver un HTTP 404 NOT FOUND.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
