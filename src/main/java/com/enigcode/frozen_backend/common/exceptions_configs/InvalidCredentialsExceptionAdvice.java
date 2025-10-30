package com.enigcode.frozen_backend.common.exceptions_configs;

import com.enigcode.frozen_backend.users.DTO.AuthResponseDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;

/**
 * Handles InvalidCredentialsException if present on the classpath, without
 * creating
 * a hard compile-time dependency in the main GlobalExceptionHandler.
 */
@ControllerAdvice
@ConditionalOnClass(name = "com.enigcode.frozen_backend.common.exceptions_configs.exceptions.InvalidCredentialsException")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InvalidCredentialsExceptionAdvice implements HandlerExceptionResolver {

    private static final String TARGET_CLASS = "com.enigcode.frozen_backend.common.exceptions_configs.exceptions.InvalidCredentialsException";

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        if (!isTargetException(ex)) {
            return null; // Not handled here; let other resolvers/advice manage it
        }

        String message = ex.getMessage();
        Integer remaining = tryGetRemainingAttempts(ex);
        if (remaining != null) {
            message = String.format("%s. Intentos restantes: %d", message, remaining);
        }

        try {
            AuthResponseDTO body = AuthResponseDTO.builder()
                    .token("ERROR")
                    .username("")
                    .roles(null)
                    .message(message)
                    .build();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), body);
            response.getWriter().flush();
        } catch (Exception ignored) {
        }
        return new ModelAndView();
    }

    private boolean isTargetException(Exception ex) {
        return ex != null && TARGET_CLASS.equals(ex.getClass().getName());
    }

    private Integer tryGetRemainingAttempts(Exception ex) {
        try {
            Method m = ex.getClass().getMethod("getRemainingAttempts");
            Object val = m.invoke(ex);
            if (val instanceof Integer i)
                return i;
        } catch (Exception ignored) {
        }
        return null;
    }
}
