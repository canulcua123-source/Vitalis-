package com.clinica.api.module.appointment.application.service;

import com.clinica.api.module.appointment.application.dto.CreateReviewRequest;
import com.clinica.api.module.appointment.domain.AppointmentStatus;
import com.clinica.api.module.appointment.infrastructure.entity.AppointmentEntity;
import com.clinica.api.module.appointment.infrastructure.entity.ReviewEntity;
import com.clinica.api.module.appointment.infrastructure.repository.AppointmentRepository;
import com.clinica.api.module.appointment.infrastructure.repository.ReviewRepository;
import com.clinica.api.module.doctor.infrastructure.entity.DoctorProfileEntity;
import com.clinica.api.module.doctor.infrastructure.repository.DoctorProfileRepository;
import com.clinica.api.module.user.infrastructure.entity.PatientProfileEntity;
import com.clinica.api.module.user.infrastructure.entity.UserEntity;
import com.clinica.api.module.user.infrastructure.repository.PatientProfileRepository;
import com.clinica.api.module.user.infrastructure.repository.UserRepository;
import com.clinica.api.shared.exception.domain.ConflictException;
import com.clinica.api.shared.exception.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Transactional
    public void submitReview(String patientEmail, CreateReviewRequest request) {

        UserEntity user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        AppointmentEntity appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no existe"));

        if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
            throw new ConflictException("Solo el paciente que agendó puede dejar reseña");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ConflictException("Solo se pueden reseñar citas que ya han finalizado");
        }

        if (reviewRepository.existsByAppointmentId(appointment.getId())) {
            throw new ConflictException("Ya has dejado una calificación para esta cita. ¡No puedes hacer trampa!");
        }

        DoctorProfileEntity doctor = appointment.getDoctor();

        // 1. Crear el Entidad de Reseña
        ReviewEntity newReview = ReviewEntity.builder()
                .appointment(appointment)
                .doctor(doctor)
                .patient(appointment.getPatient())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(newReview);

        // 2. MATEMÁTICAS PURAS - Promediar el rating global del doctor.
        int existingTotalReviews = doctor.getTotalReviews() != null ? doctor.getTotalReviews() : 0;
        BigDecimal existingRating = doctor.getRating() != null ? doctor.getRating() : BigDecimal.ZERO;

        BigDecimal totalSum = existingRating.multiply(new BigDecimal(existingTotalReviews));
        totalSum = totalSum.add(new BigDecimal(request.getRating()));

        int newTotalReviews = existingTotalReviews + 1;

        // El Promedio con 1 decimal exacto. (Ejem: 4.8)
        BigDecimal newAverageRating = totalSum.divide(new BigDecimal(newTotalReviews), 1, RoundingMode.HALF_UP);

        doctor.setTotalReviews(newTotalReviews);
        doctor.setRating(newAverageRating);

        // 3. Guardar en Base de Datos
        doctorProfileRepository.save(doctor);
        log.info("Calificación guardada exitosamente. Promedio del Doctor ({}) subió a {}", doctor.getId(),
                newAverageRating);
    }
}
