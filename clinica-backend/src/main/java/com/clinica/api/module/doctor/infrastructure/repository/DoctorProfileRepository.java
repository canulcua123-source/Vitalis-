package com.clinica.api.module.doctor.infrastructure.repository;

import com.clinica.api.module.doctor.infrastructure.entity.DoctorProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfileEntity, UUID> {

    /**
     * Trae mágicamente a todos los doctores a través del estado de su usuario.
     */
    Page<DoctorProfileEntity> findByUser_IsActiveTrue(Pageable pageable);
}
