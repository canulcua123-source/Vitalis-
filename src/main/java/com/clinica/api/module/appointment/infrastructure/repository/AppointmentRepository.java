package com.clinica.api.module.appointment.infrastructure.repository;

import com.clinica.api.module.appointment.infrastructure.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    /**
     * MAGIA JPA - El motor de Software antes de la Base de Datos.
     * Esta consulta devuelve VERDADERO si para un doctor específico, en una fecha
     * específica,
     * hay ya una cita ACTIVA (no cancelada) cuyo intervalo de tiempo choca (solapa)
     * con una hora propuesta.
     */
    @Query("SELECT COUNT(a) > 0 FROM AppointmentEntity a " +
            "WHERE a.doctor.id = :doctorId AND a.appointmentDate = :targetDate " +
            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "AND (a.startTime < :proposedEnd AND a.endTime > :proposedStart)")
    boolean hasOverlappingAppointment(
            @Param("doctorId") UUID doctorId,
            @Param("targetDate") LocalDate targetDate,
            @Param("proposedStart") LocalTime proposedStart,
            @Param("proposedEnd") LocalTime proposedEnd);

    /**
     * Devuelve las citas paginadas de un Doctor en un Día en específico.
     */
    @Query("SELECT a FROM AppointmentEntity a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :targetDate ORDER BY a.startTime ASC")
    org.springframework.data.domain.Page<AppointmentEntity> findDailyAppointmentsForDoctor(
            @Param("doctorId") UUID doctorId,
            @Param("targetDate") LocalDate targetDate,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Devuelve el historial paginado de citas de un Paciente.
     */
    @Query("SELECT a FROM AppointmentEntity a WHERE a.patient.id = :patientId ORDER BY a.appointmentDate DESC, a.startTime DESC")
    org.springframework.data.domain.Page<AppointmentEntity> findAllAppointmentsByPatient(
            @Param("patientId") UUID patientId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Citas activas de un doctor en un día (excluye canceladas y no-show)
     */
    @Query("SELECT a FROM AppointmentEntity a " +
            "WHERE a.doctor.id = :doctorId AND a.appointmentDate = :targetDate " +
            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    java.util.List<AppointmentEntity> findActiveAppointmentsForDoctorAndDate(
            @Param("doctorId") UUID doctorId,
            @Param("targetDate") LocalDate targetDate);
}
