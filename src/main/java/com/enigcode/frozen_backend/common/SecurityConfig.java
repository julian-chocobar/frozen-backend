package com.enigcode.frozen_backend.common;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                        AuthenticationProvider authenticationProvider) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(c -> c.configurationSource(corsConfigurationSource()))
                                .sessionManagement(sm -> sm
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false))
                                .securityContext(sc -> sc.requireExplicitSave(false))
                                .authenticationProvider(authenticationProvider)
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                                .requestCache(rc -> rc.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/auth/login",
                                                                "/auth/logout",
                                                                "/auth/validate",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/test/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .anyRequest().authenticated())
                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        response.setStatus(200);
                                                }));
                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
                DaoAuthenticationProvider p = new DaoAuthenticationProvider(userDetailsService);
                p.setPasswordEncoder(passwordEncoder());
                return p;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SessionRegistry sessionRegistry() {
                return new SessionRegistryImpl();
        }

        /**
         * Configuración unificada de CORS
         * Incluye soporte específico para SSE (Server-Sent Events)
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Orígenes permitidos - usar patrones para más flexibilidad
                configuration.setAllowedOriginPatterns(List.of(
                                "${RED_LOCAL}",
                                "http://localhost:3000",
                                "http://localhost:3001",
                                "http://127.0.0.1:3000",
                                "https://*.vercel.app",
                                "https://*.netlify.app",
                                "https://frozen-frontend-kappa.vercel.app"));

                // Métodos HTTP permitidos
                configuration.setAllowedMethods(List.of(
                                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

                // Headers permitidos - importante para SSE
                configuration.setAllowedHeaders(List.of(
                                "Authorization",
                                "Cache-Control",
                                "Content-Type",
                                "Accept",
                                "X-Requested-With",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers",
                                "Origin"));

                // Headers expuestos - necesarios para SSE y autenticación
                configuration.setExposedHeaders(List.of(
                                "Set-Cookie",
                                "Authorization",
                                "Access-Control-Allow-Origin",
                                "Access-Control-Allow-Credentials",
                                "Cache-Control",
                                "Content-Type",
                                "X-Accel-Buffering",
                                "Connection"));

                // Permitir credenciales (necesario para SSE y autenticación basada en cookies)
                configuration.setAllowCredentials(true);

                // Tiempo de cache para preflight requests
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}