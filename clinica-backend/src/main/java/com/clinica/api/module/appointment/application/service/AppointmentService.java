package com.clinica.api.module.appointment.application.service;

import com.clinica.api.module.appointment.application.dto.AppointmentResponse;
import com.clinica.api.module.appointment.application.dto.CreateAppointmentRequest;
import com.clinica.api.module.appointment.domain.AppointmentStatus;
import com.clinica.api.module.appointment.infrastructure.entity.AppointmentEntity;
import com.clinica.api.module.appointment.infrastructure.repository.AppointmentRepository;
import com.clinica.api.module.doctor.infrastructure.entity.DoctorProfileEntity;
import com.clinica.api.module.doctor.infrastructure.repository.DoctorProfileRepository;
import com.clinica.api.module.user.infrastructure.entity.PatientProfileEntity;
import com.clinica.api.module.user.infrastructure.entity.UserEntity;
import com.clinica.api.module.user.infrastructure.repository.PatientProfileRepository;
import com.clinica.api.module.user.infrastructure.repository.UserRepository;
import com.clinica.api.shared.exception.domain.ConflictException;
import com.clinica.api.shared.exception.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.clinica.api.module.appointment.application.dto.DoctorDailyAppointmentResponse;
import com.clinica.api.module.appointment.application.dto.PatientAppointmentResponse;
import com.clinica.api.module.appointment.application.mapper.AppointmentMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * SERVICIO EXPERTO EN CITAS MÉDICAS.
 * Controla la Transaccionalidad (ACID) y previene cruces de horarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

        private final AppointmentRepository appointmentRepository;
        private final DoctorProfileRepository doctorRepository;
        private final PatientProfileRepository patientProfileRepository;
        private final UserRepository userRepository;
        private final AppointmentMapper appointmentMapper;

        /**
         * Motor de Creación de Consultas
         */
        @Transactional
        public AppointmentResponse scheduleAppointment(String patientEmail, CreateAppointmentRequest request) {

                log.info("Iniciando solicitud de reserva para paciente {} y doctor {}", patientEmail,
                                request.getDoctorId());

                // 1. Extraer a los Actores
                UserEntity userLogged = userRepository.findByEmail(patientEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado activo en la sesión."));

                PatientProfileEntity patientProfile = patientProfileRepository.findByUserId(userLogged.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Perfil de Paciente no encontrado, complete el registro primero."));

                DoctorProfileEntity doctor = doctorRepository.findById(request.getDoctorId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "El médico solicitado no existe o fue removido."));

                // 2. Lógica Comercial (Lógica Pura)
                LocalTime requestedStart = request.getStartTime();
                LocalTime calculatedEnd = requestedStart.plusMinutes(doctor.getSlotDurationMinutes());

                // 3. UBER-LEVEL VALIDATION: ¿Alguien nos ganó la cita por 1 milisegundo?
                boolean isOccupied = appointmentRepository.hasOverlappingAppointment(
                                doctor.getId(),
                                request.getAppointmentDate(),
                                requestedStart,
                                calculatedEnd);

                if (isOccupied) {
                        // Este Exception será capturado por nuestro GlobalExceptionHandler y retornará
                        // un JSON {"status": 409, "error": "Conflict", "message": "Oops..."} a Flutter.
                        throw new ConflictException(
                                        "Oops! Otro paciente acaba de reservar esta hora o existe un solapamiento en el médico.");
                }

                // 4. Inserción al Sistema (Database Commit)
                AppointmentEntity newAppointment = AppointmentEntity.builder()
                                .doctor(doctor)
                                .patient(patientProfile) // Necesita relacionarse bien
                                .appointmentDate(request.getAppointmentDate())
                                .startTime(requestedStart)
                                .endTime(calculatedEnd)
                                .status(AppointmentStatus.PENDING_PAYMENT) // O "CONFIRMED" según tu regla de oro
                                .notes(request.getNotes())
                                .build();

                var savedAppointment = appointmentRepository.save(newAppointment);

                log.info("Cita {} reservada exitosamente en BDD.", savedAppointment.getId());

                // 5. Devolver Resultado Seguro (No expone Entidades DDBB)
                return AppointmentResponse.builder()
                                .appointmentId(savedAppointment.getId())
                                .doctorName(doctor.getUser() != null ? doctor.getUser().getEmail() : "Doctor") // Idealmente
                                                                                                               // doctor.getFirstName()
                                .appointmentDate(savedAppointment.getAppointmentDate())
                                .startTime(savedAppointment.getStartTime())
                                .endTime(savedAppointment.getEndTime())
                                .status(savedAppointment.getStatus().name())
                                .notes(savedAppointment.getNotes())
                                .build();
        }

        /**
         * VISTA DEL DOCTOR: Mis citas de Hoy.
         */
        @Transactional(readOnly = true)
        public List<DoctorDailyAppointmentResponse> getDailyAppointmentsForDoctor(String doctorEmail,
                        LocalDate targetDate) {
                return getDailyAppointmentsForDoctorPaged(doctorEmail, targetDate, Pageable.unpaged()).getContent();
        }

        /**
         * VISTA DEL DOCTOR: Mis citas de un día (paginado).
         */
        @Transactional(readOnly = true)
        public Page<DoctorDailyAppointmentResponse> getDailyAppointmentsForDoctorPaged(String doctorEmail,
                        LocalDate targetDate, Pageable pageable) {

                UserEntity userLogged = userRepository.findByEmail(doctorEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Credenciales Inválidas."));

                DoctorProfileEntity doctor = doctorRepository
                                .findByUser_IsActiveTrue(org.springframework.data.domain.Pageable.unpaged())
                                .stream()
                                .filter(d -> d.getUser().getId().equals(userLogged.getId()))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Perfil de Médico no encontrado con las credenciales indicadas."));

                return appointmentRepository.findDailyAppointmentsForDoctor(doctor.getId(), targetDate, pageable)
                                .map(appointmentMapper::toDoctorDailyResponse);
        }

        /**
         * VISTA DEL PACIENTE: Mi Historial.
         */
        @Transactional(readOnly = true)
        public List<PatientAppointmentResponse> getPatientHistorial(String patientEmail) {
                return getPatientHistorialPaged(patientEmail, Pageable.unpaged()).getContent();
        }

        /**
         * VISTA DEL PACIENTE: Mi Historial paginado.
         */
        @Transactional(readOnly = true)
        public Page<PatientAppointmentResponse> getPatientHistorialPaged(String patientEmail, Pageable pageable) {
                UserEntity userLogged = userRepository.findByEmail(patientEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Credenciales Inválidas."));

                PatientProfileEntity patient = patientProfileRepository.findByUserId(userLogged.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Perfil de Paciente no encontrado o no terminado de registrar."));

                return appointmentRepository.findAllAppointmentsByPatient(patient.getId(), pageable)
                                .map(appointmentMapper::toPatientResponse);
        }

        /**
         * ACCIÓN DE GESTIÓN: Cambiar Estado a Completado, No-Show o Cancelado.
         */
        @Transactional
        public void updateAppointmentStatus(String emailActor, java.util.UUID appointmentId,
                        AppointmentStatus newStatus) {
                UserEntity userLogged = userRepository.findByEmail(emailActor).orElseThrow();
                AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada."));

                boolean isOwnerDoctor = appointment.getDoctor().getUser().getId().equals(userLogged.getId());
                boolean isOwnerPatient = appointment.getPatient().getUser().getId().equals(userLogged.getId());

                if (!isOwnerDoctor && !isOwnerPatient) {
                        throw new ConflictException("Prohibido. No puedes modificar una consulta que no te pertenece.");
                }

                appointment.setStatus(newStatus);

                if (newStatus == AppointmentStatus.CANCELLED || newStatus == AppointmentStatus.NO_SHOW) {
                        appointment.setCancelledBy(userLogged);
                        appointment.setCancellationReason(
                                        "Estado alterado manualmente desde la aplicación a: " + newStatus.name());
                }

                appointmentRepository.save(appointment);
                log.info("Cita {} actualizada al estado {}", appointmentId, newStatus);
        }
}
