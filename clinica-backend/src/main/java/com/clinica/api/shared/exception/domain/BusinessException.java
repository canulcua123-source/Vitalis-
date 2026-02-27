package com.clinica.api.shared.exception.domain;

/**
 * Excepción base para todas las reglas de negocio en la capa de Aplicación/Dominio.
 * Otras excepciones más específicas deben heredar de esta.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
