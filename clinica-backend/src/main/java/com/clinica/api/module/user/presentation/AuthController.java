package com.clinica.api.module.user.presentation;

import com.clinica.api.module.user.application.dto.AuthRequest;
import com.clinica.api.module.user.application.dto.AuthResponse;
import com.clinica.api.module.user.application.dto.RegisterRequest;
import com.clinica.api.module.user.application.dto.RegisterDoctorRequest;
import com.clinica.api.module.user.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint Controlador Público para la Autenticación.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de Registro Libre (Pacientes).
     * 
     * @param request JSON DTO inyectado (con @Valid que dispara
     *                GlobalExceptionHandler si falla)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerPatient(
            @Valid @RequestBody RegisterRequest request // El `@Valid` ejecuta en caliente nuestras anotaciones de
                                                        // constraints (@NotBlank, etc)
    ) {
        return new ResponseEntity<>(authService.registerPatient(request), HttpStatus.CREATED); // Estado 201 Created
                                                                                               // Profesional
    }

    /**
     * Endpoint Administrativo de Registro (Doctores).
     */
    @PostMapping("/register/doctor")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AuthResponse> registerDoctor(
            @Valid @RequestBody RegisterDoctorRequest request) {
        return new ResponseEntity<>(authService.registerDoctor(request), HttpStatus.CREATED);
    }

    /**
     * Endpoint de Autenticación.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request)); // Estado 200 OK
    }
}
