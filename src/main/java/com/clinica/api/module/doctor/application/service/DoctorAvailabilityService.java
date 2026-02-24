package com.clinica.api.module.doctor.application.service;

import com.clinica.api.module.doctor.application.dto.AvailableSlotResponse;
import com.clinica.api.module.doctor.infrastructure.entity.DoctorAvailabilityEntity;
import com.clinica.api.module.doctor.infrastructure.entity.DoctorProfileEntity;
import com.clinica.api.module.doctor.infrastructure.repository.DoctorAvailabilityRepository;
import com.clinica.api.module.doctor.infrastructure.repository.DoctorProfileRepository;
import com.clinica.api.module.appointment.infrastructure.entity.AppointmentEntity;
import com.clinica.api.module.appointment.infrastructure.repository.AppointmentRepository;
import com.clinica.api.shared.exception.domain.ResourceNotFoundException;
import com.clinica.api.module.user.infrastructure.entity.UserEntity;
import com.clinica.api.module.user.infrastructure.repository.UserRepository;
import com.clinica.api.shared.exception.domain.ConflictException;
import com.clinica.api.module.doctor.application.dto.CreateAvailabilityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SERVICIO EXPERTO EN AGENDA.
 * Este servicio contiene el Algoritmo Central que calcula cuántas citas de X
 * minutos
 * caben en un rango de horario, respetando los "buffers" y bloqueos de la base
 * de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAvailabilityService {

        private final DoctorProfileRepository doctorProfileRepository;
        private final DoctorAvailabilityRepository availabilityRepository;
        private final UserRepository userRepository;
        private final AppointmentRepository appointmentRepository;

        /**
         * Algoritmo súper complejo que genera el listado visual que verá el paciente en
         * la App Móvil.
         * Toma el esquema general de SQL (09:00 - 14:00) y lo fragmenta dinámicamente
         * según la
         * duración de cita definida por el doctor (ej. cada 30 min genera: 09:00,
         * 09:30, 10:00).
         */
        public List<AvailableSlotResponse> getAvailableSlotsForDate(UUID doctorId, LocalDate targetDate) {

                // 1. Validar que el doctor exista
                DoctorProfileEntity doctor = doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "El perfil del doctor solicitado no existe."));

                // LocalDate.getDayOfWeek() retorna Lunes = 1, Domingo = 7.
                // En nuestro diseño SQL hicimos que Domingo = 0 y Sábado = 6. Ajustamos
                // matemáticamente:
                int sqlDayOfWeek = targetDate.getDayOfWeek().getValue() == 7 ? 0 : targetDate.getDayOfWeek().getValue();

                // 2. Extraer Horarios de Rutina del día
                List<DoctorAvailabilityEntity> dayRoutines = availabilityRepository
                                .findActiveAvailabilityByDoctorAndDay(doctorId, sqlDayOfWeek);

                if (dayRoutines.isEmpty()) {
                        log.info("El doctor {} no labora el día {}", doctorId, targetDate);
                        return new ArrayList<>(); // Vacío, Flutter App muestra "No hay citas disponibles".
                }

                // TODO FUTURO: Buscar en "doctor_blocked_dates" si este día está de vacaciones
                // y retornar ArrayList vacío.

                List<AvailableSlotResponse> finalSlots = new ArrayList<>();

                // Extraemos las manías del doctor
                int slotDuration = doctor.getSlotDurationMinutes();
                int bufferTime = doctor.getBufferTimeMinutes() == null ? 0 : doctor.getBufferTimeMinutes();

                // 3. El Motor de Fragmentación (El Bucle que construye el Calendario)
                for (DoctorAvailabilityEntity routine : dayRoutines) {

                        LocalTime currentStartTime = routine.getStartTime();
                        LocalTime absoluteEndTime = routine.getEndTime();

                        // Mientra la hora actual + la duración + buffer sea menor o igual a su hora de
                        // salida...
                        while (currentStartTime.plusMinutes(slotDuration).isBefore(absoluteEndTime) ||
                                        currentStartTime.plusMinutes(slotDuration).equals(absoluteEndTime)) {

                                LocalTime currentEndTime = currentStartTime.plusMinutes(slotDuration);

                                // TODO FUTURO: Hacer aquí Select de la tabla Appointments() para ver si ESTE
                                // Mismo Slot
                                // ya está tomado. Si sí -> isAvailable = false.

                                finalSlots.add(AvailableSlotResponse.builder()
                                                .startTime(currentStartTime)
                                                .endTime(currentEndTime)
                                                .isAvailable(true)
                                                .build());

                                // El siguiente bloque inicia cuando termina este + el buffer
                                currentStartTime = currentEndTime.plusMinutes(bufferTime);
                        }
                }

                // 4. Marcar slots ocupados por citas ya registradas (no canceladas)
                List<AppointmentEntity> activeAppointments = appointmentRepository
                                .findActiveAppointmentsForDoctorAndDate(doctorId, targetDate);

                if (!activeAppointments.isEmpty()) {
                        for (AvailableSlotResponse slot : finalSlots) {
                                boolean occupied = activeAppointments.stream()
                                                .anyMatch(appt -> appt.getStartTime().isBefore(slot.getEndTime())
                                                                && appt.getEndTime().isAfter(slot.getStartTime()));
                                if (occupied) {
                                        slot.setAvailable(false);
                                }
                        }
                }

                log.info("Generados {} slots dinámicos para el Doctor {} en la fecha {}", finalSlots.size(), doctorId,
                                targetDate);
                return finalSlots;
        }

        /**
         * Da de alta un nuevo bloque de horarios (Rango) para el doctor en su semana.
         */
        public void addAvailability(String doctorEmail, CreateAvailabilityRequest request) {
                if (request.getStartTime().isAfter(request.getEndTime())
                                || request.getStartTime().equals(request.getEndTime())) {
                        throw new ConflictException("La hora de finalización debe ser posterior a la de inicio.");
                }

                UserEntity user = userRepository.findByEmail(doctorEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

                DoctorProfileEntity doctor = doctorProfileRepository
                                .findByUser_IsActiveTrue(org.springframework.data.domain.Pageable.unpaged())
                                .stream().filter(d -> d.getUser().getId().equals(user.getId())).findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Perfil de doctor no encontrado."));

                boolean isOverlapping = availabilityRepository.hasOverlappingAvailability(
                                doctor.getId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

                if (isOverlapping) {
                        throw new ConflictException(
                                        "El horario a ingresar se empalma con un bloque previamente registrado para este día.");
                }

                DoctorAvailabilityEntity newRountine = DoctorAvailabilityEntity.builder()
                                .doctor(doctor)
                                .dayOfWeek(request.getDayOfWeek())
                                .startTime(request.getStartTime())
                                .endTime(request.getEndTime())
                                .isActive(true)
                                .build();

                availabilityRepository.save(newRountine);
                log.info("Bloque de disponibilidad añadido exitosamente para doctor {}: Día {} de {} a {}",
                                doctorEmail, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());
        }

        /**
         * Muestra toda la semana de trabajo del doctor al propio doctor.
         */
        public List<com.clinica.api.module.doctor.application.dto.AvailabilityResponse> getDoctorWeeklyAvailabilities(
                        String doctorEmail) {
                UserEntity user = userRepository.findByEmail(doctorEmail).orElseThrow();

                DoctorProfileEntity doctor = doctorProfileRepository
                                .findByUser_IsActiveTrue(org.springframework.data.domain.Pageable.unpaged())
                                .stream().filter(d -> d.getUser().getId().equals(user.getId())).findFirst()
                                .orElseThrow();

                return availabilityRepository.findAll().stream()
                                .filter(a -> a.getDoctor().getId().equals(doctor.getId()) && a.isActive())
                                .map(a -> com.clinica.api.module.doctor.application.dto.AvailabilityResponse.builder()
                                                .id(a.getId())
                                                .dayOfWeek(a.getDayOfWeek())
                                                .startTime(a.getStartTime())
                                                .endTime(a.getEndTime())
                                                .isActive(a.isActive())
                                                .build())
                                .toList();
        }
}
