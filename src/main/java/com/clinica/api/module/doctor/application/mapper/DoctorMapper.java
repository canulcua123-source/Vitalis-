package com.clinica.api.module.doctor.application.mapper;

import com.clinica.api.module.doctor.application.dto.DoctorResponse;
import com.clinica.api.module.doctor.infrastructure.entity.DoctorProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "specialty", source = "specialty.name", defaultValue = "Sin Especialidad")
    DoctorResponse toResponse(DoctorProfileEntity entity);
}
