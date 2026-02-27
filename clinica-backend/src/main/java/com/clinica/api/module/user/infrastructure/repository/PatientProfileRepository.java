package com.clinica.api.module.user.infrastructure.repository;

import com.clinica.api.module.user.infrastructure.entity.PatientProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PatientProfileRepository extends JpaRepository<PatientProfileEntity, UUID> {
    Optional<PatientProfileEntity> findByUserId(UUID userId);
}
