package com.clinica.api.module.doctor.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAvailabilityRequest {

    @NotNull(message = "El día de la semana es obligatorio.")
    @Min(value = 0, message = "El día debe ser entre 0 (Domingo) y 6 (Sábado).")
    @Max(value = 6, message = "El día debe ser entre 0 (Domingo) y 6 (Sábado).")
    private Integer dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria.")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria.")
    private LocalTime endTime;
}
