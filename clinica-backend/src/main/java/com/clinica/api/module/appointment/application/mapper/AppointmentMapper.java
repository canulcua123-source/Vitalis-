package com.clinica.api.module.appointment.application.mapper;

import com.clinica.api.module.appointment.application.dto.DoctorDailyAppointmentResponse;
import com.clinica.api.module.appointment.application.dto.PatientAppointmentResponse;
import com.clinica.api.module.appointment.infrastructure.entity.AppointmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "appointmentId", source = "id")
    @Mapping(target = "date", source = "appointmentDate")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientFirstName", source = "patient.firstName")
    @Mapping(target = "patientLastName", source = "patient.lastName")
    @Mapping(target = "patientNotes", source = "notes")
    DoctorDailyAppointmentResponse toDoctorDailyResponse(AppointmentEntity entity);

    @Mapping(target = "appointmentId", source = "id")
    @Mapping(target = "date", source = "appointmentDate")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorFirstName", source = "doctor.firstName")
    @Mapping(target = "doctorLastName", source = "doctor.lastName")
    @Mapping(target = "specialty", source = "doctor.specialty.name", defaultValue = "Medicina General")
    PatientAppointmentResponse toPatientResponse(AppointmentEntity entity);
}
