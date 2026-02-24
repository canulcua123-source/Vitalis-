package com.clinica.api.module.payment.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentIntentResponse {

    // Este es el ticket de cobro de un solo uso que devolverá Stripe
    private String clientSecret;

    private String stripeAccountId;
}
