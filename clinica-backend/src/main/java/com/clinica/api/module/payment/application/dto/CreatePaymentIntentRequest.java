package com.clinica.api.module.payment.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentRequest {

    @NotNull(message = "El ID de la cita es requerido")
    private UUID appointmentId;

    // Podríamos pedir la moneda aquí, pero idealmente la moneda y el precio
    // se calculan del lado del servidor leyendo el doctor de la base de datos para
    // evitar hacking.
}
