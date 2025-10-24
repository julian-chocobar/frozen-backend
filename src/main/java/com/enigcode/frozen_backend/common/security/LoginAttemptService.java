package com.enigcode.frozen_backend.common.security;

import com.enigcode.frozen_backend.common.SecurityProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final SecurityProperties securityProperties;
    private final Map<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    public LoginAttemptService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public void loginFailed(String username) {
        LoginAttempt attempt = attemptsCache.getOrDefault(username,
                new LoginAttempt(username, securityProperties.getMaxLoginAttempts()));

        attempt.recordFailedAttempt();

        if (attempt.isBlocked()) {
            attempt.setBlockedUntil(LocalDateTime.now()
                    .plusMinutes(securityProperties.getLoginTimeoutMinutes()));
        }

        attemptsCache.put(username, attempt);
    }

    public void loginSuccess(String username) {
        attemptsCache.remove(username);
    }

    public boolean isBlocked(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt == null) {
            return false;
        }

        if (attempt.isBlocked() && attempt.getBlockedUntil().isBefore(LocalDateTime.now())) {
            attemptsCache.remove(username);
            return false;
        }

        return attempt.isBlocked();
    }

    public String getBlockedMessage(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt != null && attempt.isBlocked()) {
            long minutesLeft = java.time.Duration.between(LocalDateTime.now(), attempt.getBlockedUntil()).toMinutes();
            return String.format("Demasiados intentos fallidos. Espere %d minutos.", minutesLeft + 1);
        }
        return "Credenciales incorrectas.";
    }

    public int getRemainingAttempts(String username) {
        LoginAttempt attempt = attemptsCache.get(username);
        if (attempt == null) {
            return securityProperties.getMaxLoginAttempts();
        }
        return attempt.getRemainingAttempts();
    }

    private static class LoginAttempt {
        private int failedAttempts;
        private LocalDateTime blockedUntil;
        private final int maxAttempts;

        public LoginAttempt(String username, int maxAttempts) {
            this.maxAttempts = maxAttempts;
            this.failedAttempts = 0;
        }

        public void recordFailedAttempt() {
            this.failedAttempts++;
        }

        public boolean isBlocked() {
            return failedAttempts >= maxAttempts;
        }

        public int getRemainingAttempts() {
            return Math.max(0, maxAttempts - failedAttempts);
        }

        // Getters y Setters
        public LocalDateTime getBlockedUntil() {
            return blockedUntil;
        }

        public void setBlockedUntil(LocalDateTime blockedUntil) {
            this.blockedUntil = blockedUntil;
        }
    }
}