package com.clinica.api.module.payment.presentation;

import com.clinica.api.module.payment.application.dto.ConfirmPaymentRequest;
import com.clinica.api.module.payment.application.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<com.clinica.api.module.payment.application.dto.PaymentIntentResponse> createIntent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody com.clinica.api.module.payment.application.dto.CreatePaymentIntentRequest request) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(userDetails.getUsername(), request));
    }

    /**
     * Endpoint Protegido: El paciente manda su comprobante de pago de Stripe /
     * PayPal / MercadoPago
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Void> confirmPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        paymentService.confirmPayment(userDetails.getUsername(), request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
