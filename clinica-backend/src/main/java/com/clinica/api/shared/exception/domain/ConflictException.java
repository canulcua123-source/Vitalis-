package com.clinica.api.shared.exception.domain;

/**
 * Usada para conflictos de estado.
 * Ejemplo 1: El paciente intenta cancelar una cita que ya está completada.
 * Ejemplo 2: Superposición de horarios (Overlapping appointments).
 * El GlobalExceptionHandler la traducirá a un HTTP 409 Conflict.
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
