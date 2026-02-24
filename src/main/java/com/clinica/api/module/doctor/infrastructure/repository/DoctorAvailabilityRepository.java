package com.clinica.api.module.doctor.infrastructure.repository;

import com.clinica.api.module.doctor.infrastructure.entity.DoctorAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailabilityEntity, UUID> {

    // Obtener los horarios de un doctor específico en un día de la semana
    // particular (Ej: Lunes = 1)
    // Solo trae los que estén marcados como activos
    @Query("SELECT da FROM DoctorAvailabilityEntity da WHERE da.doctor.id = :doctorId AND da.dayOfWeek = :dayOfWeek AND da.isActive = true ORDER BY da.startTime ASC")
    List<DoctorAvailabilityEntity> findActiveAvailabilityByDoctorAndDay(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") Integer dayOfWeek);

    @Query("SELECT COUNT(da) > 0 FROM DoctorAvailabilityEntity da WHERE da.doctor.id = :doctorId AND da.dayOfWeek = :dayOfWeek AND da.isActive = true AND (da.startTime < :proposedEnd AND da.endTime > :proposedStart)")
    boolean hasOverlappingAvailability(
            @Param("doctorId") UUID doctorId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("proposedStart") java.time.LocalTime proposedStart,
            @Param("proposedEnd") java.time.LocalTime proposedEnd);
}
