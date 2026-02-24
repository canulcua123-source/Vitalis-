package com.clinica.api.module.doctor.infrastructure.repository;

import com.clinica.api.module.doctor.infrastructure.entity.SpecialtyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<SpecialtyEntity, Integer> {
    Optional<SpecialtyEntity> findByName(String name);
}
