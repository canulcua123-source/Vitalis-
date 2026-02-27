package com.clinica.api.shared.exception.domain;

/**
 * Usada cuando un recurso (User, Appointment, MedicalRecord) no se encuentra en la base de datos.
 * El GlobalExceptionHandler la traducirá automáticamente a un HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
