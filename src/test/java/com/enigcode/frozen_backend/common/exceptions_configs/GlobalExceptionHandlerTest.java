package com.enigcode.frozen_backend.common.exceptions_configs;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BlockedUserException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Standalone setup with the controller that throws exceptions and the advice under test
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleResourceNotFound_returns404_and_message() throws Exception {
        mockMvc.perform(get("/test-exceptions/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recurso no encontrado"));
    }

    @Test
    void handleDataIntegrityViolation_returns409_and_friendlyMessage() throws Exception {
        mockMvc.perform(get("/test-exceptions/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("El dato que intentas guardar")));
    }

    @Test
    void handleBadCredentials_returns401_with_fixedMessage() throws Exception {
        mockMvc.perform(get("/test-exceptions/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void handleBlockedUser_returns429_and_authResponse() throws Exception {
        mockMvc.perform(get("/test-exceptions/blocked"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.token").value("BLOCKED"))
                .andExpect(jsonPath("$.message").value("Usuario bloqueado"));
    }

    @Test
    void handleBadRequestException_returns400_and_message() throws Exception {
        mockMvc.perform(get("/test-exceptions/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Solicitud inválida"));
    }

    @Test
    void handleGenericException_returns500_and_genericMessage() throws Exception {
        mockMvc.perform(get("/test-exceptions/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocurrió un error interno del servidor. Consulte los logs para más detalles."));
    }

    // Controller used only for testing the advice
    @RestController
    @RequestMapping("/test-exceptions")
    static class TestController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("Recurso no encontrado");
        }

        @GetMapping("/conflict")
        public void conflict() {
            throw new DataIntegrityViolationException("duplicate key");
        }

        @GetMapping("/bad-credentials")
        public void badCredentials() {
            throw new BadCredentialsException("bad");
        }

        @GetMapping("/blocked")
        public void blocked() {
            throw new BlockedUserException("Usuario bloqueado");
        }

        @GetMapping("/bad-request")
        public void badRequest() {
            throw new BadRequestException("Solicitud inválida");
        }

        @GetMapping("/error")
        public void error() {
            throw new RuntimeException("boom");
        }
    }
}
