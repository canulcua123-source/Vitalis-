package com.clinica.api.module.doctor.presentation;

import com.clinica.api.module.doctor.application.dto.AvailableSlotResponse;
import com.clinica.api.module.doctor.application.dto.DoctorResponse;
import com.clinica.api.module.doctor.application.service.DoctorAvailabilityService;
import com.clinica.api.module.doctor.application.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorAvailabilityService availabilityService;

    /**
     * Endpoint Público (o protegido según el negocio) para traer al equipo médico.
     * Soporta Paginación vía '?page=0&size=10&sort=rating,desc'
     */
    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> getDoctors(
            @PageableDefault(size = 20, sort = "rating") Pageable pageable) {
        Page<DoctorResponse> doctors = doctorService.getAllActiveDoctors(pageable);
        return ResponseEntity.ok(doctors);
    }

    /**
     * VISTA PÚBLICA (o protegida según negocio): retorna slots disponibles del doctor en una fecha.
     * Ejemplo: GET /api/v1/doctors/{doctorId}/availability?date=2024-11-20
     */
    @GetMapping("/{doctorId}/availability")
    public ResponseEntity<List<AvailableSlotResponse>> getDoctorAvailabilityForDate(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getAvailableSlotsForDate(doctorId, date));
    }
}
