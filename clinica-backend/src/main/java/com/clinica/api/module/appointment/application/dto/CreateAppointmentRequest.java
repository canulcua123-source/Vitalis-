package com.clinica.api.module.appointment.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class CreateAppointmentRequest {

    @NotNull(message = "El ID del doctor es obligatorio")
    private UUID doctorId;

    @NotNull(message = "La fecha de la cita es obligatoria")
    @FutureOrPresent(message = "No puedes reservar citas en días pasados")
    private LocalDate appointmentDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    private String notes; // Opcional
}
