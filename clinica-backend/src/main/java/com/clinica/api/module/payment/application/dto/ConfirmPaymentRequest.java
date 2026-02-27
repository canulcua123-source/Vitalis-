package com.clinica.api.module.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ConfirmPaymentRequest {

    @NotNull(message = "El ID de la cita no puede estar vacío")
    private UUID appointmentId;

    @NotNull(message = "El monto pagado es obligatorio")
    private BigDecimal amount;

    @NotBlank(message = "El método de pago es requerido (Ej. TDD, TDC)")
    private String paymentMethod;

    @NotBlank(message = "El proveedor de pago es requerido (Ej. Stripe, MercadoPago)")
    private String paymentProvider;

    @NotBlank(message = "El ID de transacción devuelto por el banco es estrictamente necesario para validación de impuestos")
    private String providerPaymentId;
}
