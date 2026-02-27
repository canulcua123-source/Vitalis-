package com.clinica.api.module.doctor.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private UUID id;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isActive;
}
