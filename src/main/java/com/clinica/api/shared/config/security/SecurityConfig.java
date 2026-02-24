package com.clinica.api.shared.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Escudo principal de Spring. Aquí definimos qué rutas son públicas y cuáles
 * no,
 * además de inyectar nuestro filtro JWT.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Permite usar `@PreAuthorize("hasRole('ROLE_DOCTOR')")` en los endpoints
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // Desactivar CSRF (No es útil si usamos JWT y
                                                                       // CORS en móviles)
                                .authorizeHttpRequests(auth -> auth
                                                // Rutas públicas de Autenticación
                                                .requestMatchers("/api/v1/auth/**").permitAll()

                                                // Webhook público para Stripe
                                                .requestMatchers("/api/v1/payments/webhook").permitAll()

                                                // Rutas públicas opcionales (Ej: Swagger, Documentación, Health)
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()

                                                // Todas las demás rutas exigen un Token Válido
                                                .anyRequest().authenticated())
                                .sessionManagement(sess -> sess
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Nunca guardar
                                                                                                        // cookies, todo
                                                                                                        // por Token
                                )
                                .authenticationProvider(authenticationProvider)
                                // Ejecutar el filtro JWT ANTES que el filtro por defecto de Spring
                                // (UsernamePassword)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
