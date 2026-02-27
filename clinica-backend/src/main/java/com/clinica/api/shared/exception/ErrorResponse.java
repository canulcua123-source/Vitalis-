package com.clinica.api.shared.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa una respuesta de error estandarizada basada en la RFC 7807 (Problem Details).
 * Esto asegura que los clientes móviles (Flutter) siempre reciban la misma estructura
 * sin importar de dónde provenga el error (Seguridad, Validación, Base de Datos, Negocio).
 */
@Getter
@Builder
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ValidationError> validationErrors;

    @Getter
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
    }
}
