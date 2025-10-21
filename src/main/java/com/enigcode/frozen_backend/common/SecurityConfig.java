package com.enigcode.frozen_backend.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Especifica los orígenes permitidos (solo localhost:3000 en desarrollo)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // Desarrollo local
                "http://127.0.0.1:3000", // Alternativa a localhost
                "https://frozen-frontend-kappa.vercel.app" // Producción
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Cabeceras permitidas
        configuration.setAllowedHeaders(Arrays.asList(
                "authorization",
                "content-type",
                "x-auth-token",
                "x-requested-with"));

        // Cabeceras expuestas
        configuration.setExposedHeaders(Arrays.asList(
                "x-auth-token",
                "x-total-count" // Útil para paginación
        ));

        // Permitir credenciales (necesario si usas cookies/sesión)
        configuration.setAllowCredentials(true);

        // Tiempo que la respuesta de pre-vuelo puede ser cacheada
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}