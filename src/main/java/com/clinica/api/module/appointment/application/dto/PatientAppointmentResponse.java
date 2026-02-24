package com.clinica.api.module.appointment.application.dto;

import com.clinica.api.module.appointment.domain.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class PatientAppointmentResponse {
    private UUID appointmentId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
    private String notes;

    private UUID doctorId;
    private String doctorFirstName;
    private String doctorLastName;
    private String specialty;
}
