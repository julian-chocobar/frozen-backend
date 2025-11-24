package com.enigcode.frozen_backend.common.exceptions_configs;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BlockedUserException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.users.DTO.AuthResponseDTO;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Helper para crear una estructura de respuesta de error consistente
    private Map<String, Object> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    /**
     * Maneja errores de autorización (acceso denegado) y devuelve HTTP 403.
     */
    @ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
    public ResponseEntity<Object> handleAccessDenied(Exception ex) {
        Map<String, Object> response = createErrorResponse(
                "Acceso denegado",
                HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja las excepciones de tipo BadRequestException (errores de negocio).
     * Devuelve HTTP 400 Bad Request.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {

        Map<String, Object> response = createErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja las fallas de @Valid y devuelve HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Mapea los errores de campo (ej: "name": "Se debe ingresar un nombre...")
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> response = createErrorResponse(
                "Error de validación de campos. Por favor, revisa los detalles.",
                HttpStatus.BAD_REQUEST);
        response.put("details", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja la violación de restricciones de base de datos (Ej: clave UNIQUE).
     * Devuelve HTTP 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        String friendlyMessage = "El dato que intentas guardar ya existe o viola una restricción de la base de datos (Ej: código/nombre duplicado).";

        Map<String, Object> response = createErrorResponse(
                friendlyMessage,
                HttpStatus.CONFLICT);

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Maneja la excepción personalizada cuando un recurso no se encuentra.
     * Devuelve HTTP 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {

        Map<String, Object> response = createErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja errores de credenciales inválidas
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> response = createErrorResponse(
                "Usuario o contraseña incorrectos",
                HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja errores de usuario deshabilitado
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabledUser(DisabledException ex) {
        Map<String, Object> response = createErrorResponse(
                "Usuario deshabilitado",
                HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja otros errores de autenticación
     */
    @Order(Ordered.HIGHEST_PRECEDENCE) // Añadir esta anotación
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex) {
        String message = "Error de autenticación";
        if (ex instanceof BadCredentialsException) {
            message = "Usuario o contraseña incorrectos";
        } else if (ex instanceof DisabledException) {
            message = "Usuario deshabilitado";
        }

        Map<String, Object> response = createErrorResponse(
                message,
                HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Manejar usuario bloqueado por rate limiting
    @ExceptionHandler(BlockedUserException.class)
    public ResponseEntity<AuthResponseDTO> handleBlockedUser(BlockedUserException ex) {
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("BLOCKED")
                .username("")
                .roles(null)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    /**
     * Maneja errores de SSE cuando el cliente se desconecta
     * No intenta responder porque el stream ya está cerrado
     */
    @ExceptionHandler(org.springframework.web.context.request.async.AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(
            org.springframework.web.context.request.async.AsyncRequestNotUsableException ex) {
        // Cliente desconectado - esto es normal en SSE
        // No logueamos ni intentamos responder porque el stream está cerrado
    }

    // Nota: El manejo específico de InvalidCredentialsException se movió a un
    // advice condicional
    // para evitar errores de carga de clase durante los tests cuando dicha clase no
    // está disponible

    /**
     * Manejador general para cualquier otra excepción no prevista.
     * Devuelve HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception ex) {
        // Log detallado para diagnóstico en tests
        try {
            org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
                    .error("Unhandled exception: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);
        } catch (Throwable ignored) {
            // Evitar que un fallo de logging afecte la respuesta
        }

        Map<String, Object> response = createErrorResponse(
                "Ocurrió un error interno del servidor. Consulte los logs para más detalles.",
                HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
