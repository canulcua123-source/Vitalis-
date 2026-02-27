package com.clinica.api.module.payment.application.service;

import com.clinica.api.module.appointment.domain.AppointmentStatus;
import com.clinica.api.module.appointment.infrastructure.entity.AppointmentEntity;
import com.clinica.api.module.appointment.infrastructure.repository.AppointmentRepository;
import com.clinica.api.module.notification.application.service.NotificationService;
import com.clinica.api.module.payment.application.dto.ConfirmPaymentRequest;
import com.clinica.api.module.payment.application.dto.CreatePaymentIntentRequest;
import com.clinica.api.module.payment.application.dto.PaymentIntentResponse;
import com.clinica.api.module.payment.infrastructure.entity.PaymentEntity;
import com.clinica.api.module.payment.infrastructure.repository.PaymentRepository;
import com.clinica.api.module.user.infrastructure.entity.UserEntity;
import com.clinica.api.module.user.infrastructure.repository.UserRepository;
import com.clinica.api.shared.exception.domain.ConflictException;
import com.clinica.api.shared.exception.domain.ResourceNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final AppointmentRepository appointmentRepository;
        private final UserRepository userRepository;

        // Inyección del Microservicio Lógico Interno (Event-Driven Simulation)
        private final NotificationService notificationService;

        @Value("${stripe.secret-key}")
        private String stripeSecretKey;

        @Value("${stripe.webhook-secret:}")
        private String stripeWebhookSecret;

        public PaymentIntentResponse createPaymentIntent(String patientEmail, CreatePaymentIntentRequest request) {
                UserEntity patientUser = userRepository.findByEmail(patientEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

                AppointmentEntity appointment = appointmentRepository.findById(request.getAppointmentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada"));

                if (!appointment.getPatient().getUser().getId().equals(patientUser.getId())) {
                        throw new ConflictException("Solo el paciente de esta cita puede intentar pagarla.");
                }

                if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
                        throw new ConflictException(
                                        "Esta cita no está pendiente de pago. Su estado es: "
                                                        + appointment.getStatus().name());
                }

                // Obtener Precio del Doctor
                BigDecimal consultationPrice = appointment.getDoctor().getConsultationPrice();
                if (consultationPrice == null || consultationPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ConflictException("El doctor no tiene un precio válido de consulta configurado.");
                }

                // Stripe acepta centavos, así que multiplicamos por 100
                long amountInCents = consultationPrice.multiply(new BigDecimal(100)).longValue();

                try {
                        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                                        .setAmount(amountInCents)
                                        .setCurrency("mxn")
                                        .putMetadata("appointment_id", appointment.getId().toString())
                                        .putMetadata("patient_email", patientEmail)
                                        .putMetadata("doctor_id", appointment.getDoctor().getId().toString())
                                        .setAutomaticPaymentMethods(
                                                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                                                        .setEnabled(true)
                                                                        .setAllowRedirects(
                                                                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                                                        .build())
                                        .build();

                        PaymentIntent intent = PaymentIntent.create(params);

                        log.info("Ticket de cobro (Intent) generado por Stripe para la Cita {}. Monto: {}",
                                        appointment.getId(), consultationPrice);

                        return PaymentIntentResponse.builder()
                                        .clientSecret(intent.getClientSecret())
                                        .build();

                } catch (StripeException e) {
                        log.error("Fallo Stripe al generar PaymentIntent: {}", e.getMessage());
                        throw new RuntimeException(
                                        "Error en nuestra pasarela de pagos al intentar procesar su solicitud.");
                }
        }

        @Transactional
        public void confirmPayment(String patientEmail, ConfirmPaymentRequest request) {
                UserEntity patientUser = userRepository.findByEmail(patientEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

                AppointmentEntity appointment = appointmentRepository.findById(request.getAppointmentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada"));

                if (!appointment.getPatient().getUser().getId().equals(patientUser.getId())) {
                        throw new ConflictException("Solo el titular de la cita puede procesar el pago");
                }

                if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
                        throw new ConflictException(
                                        "Esta cita no está esperando pago financiero. Su estado es: "
                                                        + appointment.getStatus());
                }

                if (paymentRepository.existsByAppointmentIdAndStatus(appointment.getId(), "completed")) {
                        throw new ConflictException("Esta cita ya tiene un cobro reportado como completado");
                }

                // 1. Crear Recibo Físico (Registro Financiero)
                PaymentEntity newPayment = PaymentEntity.builder()
                                .appointment(appointment)
                                .patient(appointment.getPatient())
                                .doctor(appointment.getDoctor())
                                .amount(request.getAmount())
                                .currency("MXN")
                                .paymentMethod(request.getPaymentMethod())
                                .paymentProvider(request.getPaymentProvider())
                                .providerPaymentId(request.getProviderPaymentId())
                                .status("completed")
                                .paidAt(LocalDateTime.now())
                                .build();

                paymentRepository.save(newPayment);

                // 2. Modificar Estado Cita para que Flutter la pinte de verde
                appointment.setStatus(AppointmentStatus.CONFIRMED);
                appointmentRepository.save(appointment);

                log.info("💳 Pago Recibido de {} comprobado ({})! Cita {} actualizada a CONFIRMED",
                                patientEmail, request.getProviderPaymentId(), appointment.getId());

                // 3. Disparar Evento Asíncrono: Notificación al Doctor
                String doctorMessage = String.format(
                                "El paciente %s acaba de confirmar y pagar su cita para la fecha %s a las %s hrs",
                                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                                appointment.getAppointmentDate(),
                                appointment.getStartTime());

                notificationService.createNotification(
                                appointment.getDoctor().getUser(),
                                "¡Cita Confirmada y Pagada! 💰",
                                doctorMessage,
                                "PAYMENT_SUCCESS");

                // 4. Notificación local para la bandeja de entrada del Paciente
                notificationService.createNotification(
                                patientUser,
                                "Recibo de Pago",
                                "Hemos recibido exitosamente el pago de $" + request.getAmount()
                                                + " por la consulta programada. Conserva este recibo con ID "
                                                + request.getProviderPaymentId(),
                                "PAYMENT_RECEIPT");
        }

        @Transactional
        public void handleStripeWebhook(String payload, String signatureHeader) {
                if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
                        throw new IllegalStateException("Stripe webhook secret no configurado");
                }

                Event event;
                try {
                        event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
                } catch (SignatureVerificationException e) {
                        throw new IllegalArgumentException("Firma de webhook inválida");
                }

                if ("payment_intent.succeeded".equals(event.getType())) {
                        Optional<com.stripe.model.StripeObject> stripeObject = event.getDataObjectDeserializer()
                                        .getObject();
                        if (stripeObject.isEmpty()) {
                                log.warn("Webhook: No se pudo deserializar el objeto de Stripe");
                                return;
                        }
                        if (!(stripeObject.get() instanceof PaymentIntent)) {
                                log.warn("Webhook: El objeto recibido NO es un PaymentIntent. Tipo detectado: {}",
                                                stripeObject.get().getClass().getName());
                                return;
                        }

                        PaymentIntent intent = (PaymentIntent) stripeObject.get();
                        String appointmentId = intent.getMetadata() != null ? intent.getMetadata().get("appointment_id")
                                        : null;

                        if (appointmentId == null || appointmentId.isBlank()) {
                                log.warn("PaymentIntent sin appointment_id en metadata");
                                return;
                        }

                        java.util.UUID apptId = java.util.UUID.fromString(appointmentId);
                        if (paymentRepository.existsByAppointmentIdAndStatus(apptId, "completed")) {
                                log.info("Pago ya procesado para cita {}", apptId);
                                return;
                        }

                        AppointmentEntity appointment = appointmentRepository.findById(apptId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada"));

                        long amountCents = intent.getAmountReceived() != null ? intent.getAmountReceived()
                                        : intent.getAmount();
                        BigDecimal amount = BigDecimal.valueOf(amountCents, 2);
                        String currency = intent.getCurrency() != null ? intent.getCurrency().toUpperCase() : "MXN";
                        String paymentMethod = (intent.getPaymentMethodTypes() != null
                                        && !intent.getPaymentMethodTypes().isEmpty())
                                                        ? intent.getPaymentMethodTypes().get(0)
                                                        : "card";

                        PaymentEntity newPayment = PaymentEntity.builder()
                                        .appointment(appointment)
                                        .patient(appointment.getPatient())
                                        .doctor(appointment.getDoctor())
                                        .amount(amount)
                                        .currency(currency)
                                        .paymentMethod(paymentMethod)
                                        .paymentProvider("Stripe")
                                        .providerPaymentId(intent.getId())
                                        .status("completed")
                                        .paidAt(LocalDateTime.now())
                                        .build();

                        paymentRepository.save(newPayment);

                        appointment.setStatus(AppointmentStatus.CONFIRMED);
                        appointmentRepository.save(appointment);

                        String doctorMessage = String.format(
                                        "El paciente %s acaba de confirmar y pagar su cita para la fecha %s a las %s hrs",
                                        appointment.getPatient().getFirstName() + " "
                                                        + appointment.getPatient().getLastName(),
                                        appointment.getAppointmentDate(),
                                        appointment.getStartTime());

                        notificationService.createNotification(
                                        appointment.getDoctor().getUser(),
                                        "¡Cita Confirmada y Pagada! 💰",
                                        doctorMessage,
                                        "PAYMENT_SUCCESS");

                        notificationService.createNotification(
                                        appointment.getPatient().getUser(),
                                        "Recibo de Pago",
                                        "Hemos recibido exitosamente el pago de $" + amount
                                                        + " por la consulta programada. Conserva este recibo con ID "
                                                        + intent.getId(),
                                        "PAYMENT_RECEIPT");
                }
        }
}
