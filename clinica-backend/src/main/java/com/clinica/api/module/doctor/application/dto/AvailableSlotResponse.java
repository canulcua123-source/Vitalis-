package com.clinica.api.module.doctor.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotResponse {
    
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Indica si otra consulta ya ocupa este campo en particular (Evitar Overlapping)
    @Builder.Default
    private boolean isAvailable = true; 
}
