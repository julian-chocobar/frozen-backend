package com.enigcode.frozen_backend.common.exceptions_configs;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * Maneja las excepciones de tipo BadRequestException (errores de negocio).
     * Devuelve HTTP 400 Bad Request.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {

        Map<String, Object> response = createErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);}

    /**
     * Maneja las fallas de @Valid y devuelve HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Mapea los errores de campo (ej: "name": "Se debe ingresar un nombre...")
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> response = createErrorResponse(
                "Error de validación de campos. Por favor, revisa los detalles.",
                HttpStatus.BAD_REQUEST);
        response.put("details", errors); // Agrega los errores específicos por campo

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja la violación de restricciones de base de datos (Ej: clave UNIQUE).
     * Devuelve HTTP 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        // Mensaje genérico, puedes refinarlo para buscar la causa raíz específica del error de DB.
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
                ex.getMessage(), // Usa el mensaje que se pasó al lanzar la excepción
                HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Manejador general para cualquier otra excepción no prevista.
     * Devuelve HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception ex) {

        // ¡Importante! Loggea la excepción completa aquí para debug
        // log.error("Error Interno del Servidor: ", ex);

        Map<String, Object> response = createErrorResponse(
                "Ocurrió un error interno del servidor. Consulte los logs para más detalles.",
                HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
