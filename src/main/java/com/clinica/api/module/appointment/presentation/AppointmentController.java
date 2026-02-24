package com.clinica.api.module.appointment.presentation;

import com.clinica.api.module.appointment.application.dto.AppointmentResponse;
import com.clinica.api.module.appointment.application.dto.CreateAppointmentRequest;
import com.clinica.api.module.appointment.application.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import com.clinica.api.module.appointment.application.dto.DoctorDailyAppointmentResponse;
import com.clinica.api.module.appointment.application.dto.PatientAppointmentResponse;
import com.clinica.api.module.appointment.domain.AppointmentStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Endpoint Protegido: Solo un paciente autenticado (con Token) puede agendar.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<AppointmentResponse> scheduleAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.scheduleAppointment(userDetails.getUsername(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * VISTA DEL PACIENTE: Obtiene todo su historial de citas.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public ResponseEntity<Page<PatientAppointmentResponse>> getMyHistoricalAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getPatientHistorialPaged(userDetails.getUsername(), pageable));
    }

    /**
     * VISTA DEL DOCTOR: Obtiene sus citas de un día específico.
     * Ejemplo: GET /api/v1/appointments/doctor/daily?date=2024-11-20
     */
    @GetMapping("/doctor/daily")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<Page<DoctorDailyAppointmentResponse>> getDailyAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getDailyAppointmentsForDoctorPaged(userDetails.getUsername(), date, pageable));
    }

    /**
     * ACCIÓN COMÚN: Actualizar estado. (Cancelar, Confirmar, Finalizar).
     */
    @PatchMapping("/{appointmentId}/status")
    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_PATIENT')")
    public ResponseEntity<Void> updateAppointmentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID appointmentId,
            @RequestBody Map<String, String> body) {
        // En un caso real usaríamos un pequeño RequestDTO y validaciones enums.
        AppointmentStatus newStatus = AppointmentStatus.valueOf(body.get("status").toUpperCase());
        appointmentService.updateAppointmentStatus(userDetails.getUsername(), appointmentId, newStatus);

        return ResponseEntity.noContent().build();
    }
}
