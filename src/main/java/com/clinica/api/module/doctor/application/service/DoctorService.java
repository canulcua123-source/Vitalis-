package com.clinica.api.module.doctor.application.service;

import com.clinica.api.module.doctor.application.dto.DoctorResponse;
import com.clinica.api.module.doctor.infrastructure.entity.DoctorProfileEntity;
import com.clinica.api.module.doctor.infrastructure.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.clinica.api.module.doctor.application.mapper.DoctorMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorProfileRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    /**
     * Retorna el listado paginado de Doctores ACTIVOS.
     * Ideal para un Scroll Infinito en tu App Móvil (Flutter).
     */
    public Page<DoctorResponse> getAllActiveDoctors(Pageable pageable) {
        log.info("Buscando doctores activos página {} de tamaño {}", pageable.getPageNumber(), pageable.getPageSize());

        return doctorRepository.findByUser_IsActiveTrue(pageable)
                .map(doctorMapper::toResponse);
    }
}
