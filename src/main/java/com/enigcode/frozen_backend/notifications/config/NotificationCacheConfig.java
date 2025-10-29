package com.enigcode.frozen_backend.notifications.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuración de cache para notificaciones
 * Optimizado para ventanas de polling específicas (09:00-10:00 y 17:00-18:00)
 */
@Configuration
@EnableCaching
public class NotificationCacheConfig {

    /**
     * Cache manager para notificaciones con TTL optimizado para ventanas de polling
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // Configurar caches específicos para notificaciones
        cacheManager.setAllowNullValues(false);
        cacheManager.setCacheNames(Arrays.asList(
                "notifications",        // Cache de notificaciones por usuario
                "notificationStats",    // Cache de estadísticas
                "pollingWindows"        // Cache de configuración de ventanas
        ));

        return cacheManager;
    }
}