package com.clinica.api.module.appointment.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre el enum AppointmentStatus (MAYÚSCULAS en Java)
 * y el formato snake_case en minúsculas que la base de datos espera.
 * Esto es necesario porque el CHECK constraint de PostgreSQL valida
 * que el status sea en minúsculas (ej: 'pending_payment', 'confirmed').
 */
@Converter(autoApply = false)
public class AppointmentStatusConverter implements AttributeConverter<AppointmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AppointmentStatus status) {
        if (status == null)
            return null;
        return status.name().toLowerCase();
    }

    @Override
    public AppointmentStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null)
            return null;
        return AppointmentStatus.valueOf(dbValue.toUpperCase());
    }
}
