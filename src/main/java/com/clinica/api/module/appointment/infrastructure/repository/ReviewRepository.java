package com.clinica.api.module.appointment.infrastructure.repository;

import com.clinica.api.module.appointment.infrastructure.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    boolean existsByAppointmentId(UUID appointmentId);
}
