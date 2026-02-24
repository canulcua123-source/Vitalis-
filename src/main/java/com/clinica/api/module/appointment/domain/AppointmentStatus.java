package com.clinica.api.module.appointment.domain;

/**
 * Estados estandarizados matemáticamente en SQL para la Cita.
 */
public enum AppointmentStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
