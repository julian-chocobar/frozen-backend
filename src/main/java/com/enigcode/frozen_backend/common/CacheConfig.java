package com.enigcode.frozen_backend.common;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para el módulo de Analytics.
 * TTL de 6 horas para datos históricos que no cambian frecuentemente.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * TTL de 6 horas (21,600 segundos) para datos de analytics.
     * Los datos históricos son inmutables, solo se agregan nuevos datos del día actual.
     * En el peor caso, los datos reflejarán información hasta el día anterior.
     */
    private static final long CACHE_TTL_HOURS = 6;
    private static final long CACHE_TTL_SECONDS = CACHE_TTL_HOURS * 60 * 60;
    private static final int CACHE_MAX_SIZE = 1000;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        CaffeineCache analyticsCache = new CaffeineCache("analytics",
                Caffeine.newBuilder()
                        .expireAfterWrite(CACHE_TTL_SECONDS, TimeUnit.SECONDS)
                        .maximumSize(CACHE_MAX_SIZE)
                        .recordStats() // Habilitar estadísticas para monitoreo
                        .build());
        
        cacheManager.setCaches(Arrays.asList(analyticsCache));
        
        return cacheManager;
    }
}

