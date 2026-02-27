package com.clinica.api.module.payment.infrastructure.repository;

import com.clinica.api.module.payment.infrastructure.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    boolean existsByAppointmentIdAndStatus(UUID appointmentId, String status);
}
