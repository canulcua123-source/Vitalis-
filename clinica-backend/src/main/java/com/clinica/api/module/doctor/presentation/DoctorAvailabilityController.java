package com.clinica.api.module.doctor.presentation;

import com.clinica.api.module.doctor.application.dto.AvailabilityResponse;
import com.clinica.api.module.doctor.application.dto.CreateAvailabilityRequest;
import com.clinica.api.module.doctor.application.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    /**
     * VISTA DEL DOCTOR: Agrega sus horas de trabajo. (Ejemplo: Lunes de 09:00 a
     * 14:00)
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<Void> addAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        availabilityService.addAvailability(userDetails.getUsername(), request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * VISTA DEL DOCTOR: Retorna toda la cuadrícula de horarios que tiene activos.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<List<AvailabilityResponse>> getMyAvailabilities(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(availabilityService.getDoctorWeeklyAvailabilities(userDetails.getUsername()));
    }
}
