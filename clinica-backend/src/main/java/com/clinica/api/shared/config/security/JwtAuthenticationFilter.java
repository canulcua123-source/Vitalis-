package com.clinica.api.shared.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Filtro que intercepta CADA petición HTTP para comprobar si el usuario está enviando un Token válido.
 * Si es válido, mete al usuario en el Contexto de Seguridad (SecurityContextHolder).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver; // Importante para enviar errores al GlobalExceptionHandler

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        try {
            // 1. Validar que la cabecera existe y empieza con 'Bearer '
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extraer Token y comprobar Email (subject)
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            // 3. Si tiene email pero NO está ya autenticado en el sistema
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Buscamos al usuario en base de datos
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Comprobamos si el token no está expirado ni manipulado matemáticamente
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Armamos el objeto de Sesión en Memoria (Stateless)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 4. Metemos al usuario validado en el Contexto. 
                    // Ya está autenticado para el resto de la petición.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // Si el token falló, está expirado, es corrupto... le pasamos el error a nuestro
            // GlobalExceptionHandler para que envase un error 401 bonito JSON en vez del StackTrace.
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
