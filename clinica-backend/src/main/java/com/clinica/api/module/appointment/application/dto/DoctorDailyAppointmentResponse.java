package com.clinica.api.module.appointment.application.dto;

import com.clinica.api.module.appointment.domain.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class DoctorDailyAppointmentResponse {
    private UUID appointmentId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;

    // Solo al doctor le interesa saber QUÍEN es el paciente
    private UUID patientId;
    private String patientFirstName;
    private String patientLastName;
    private String patientNotes;
}
