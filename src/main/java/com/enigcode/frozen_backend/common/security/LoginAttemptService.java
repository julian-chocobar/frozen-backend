package com.enigcode.frozen_backend.common.security;

import com.enigcode.frozen_backend.common.SecurityProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final long CACHE_CLEANUP_INTERVAL_MS = 30 * 60 * 1000L; // 30 minutos

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

    /**
     * Limpieza periódica del cache de intentos de login expirados.
     * Previene el crecimiento indefinido del cache en memoria.
     */
    @Scheduled(fixedRate = CACHE_CLEANUP_INTERVAL_MS)
    public void cleanupExpiredAttempts() {
        if (attemptsCache.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int initialSize = attemptsCache.size();
        
        attemptsCache.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            // Remover intentos expirados (bloqueos que ya pasaron)
            if (attempt.isBlocked() && attempt.getBlockedUntil() != null 
                && attempt.getBlockedUntil().isBefore(now)) {
                return true;
            }
            return false;
        });

        int removed = initialSize - attemptsCache.size();
        if (removed > 0) {
            // Usar System.out.println para evitar dependencia de logger
            System.out.println("Limpieza de cache de intentos de login: " + removed + " entradas expiradas removidas, " + attemptsCache.size() + " restantes");
        }
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