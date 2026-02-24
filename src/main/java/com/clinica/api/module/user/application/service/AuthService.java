package com.clinica.api.module.user.application.service;

import com.clinica.api.module.doctor.infrastructure.entity.DoctorProfileEntity;
import com.clinica.api.module.doctor.infrastructure.entity.SpecialtyEntity;
import com.clinica.api.module.doctor.infrastructure.repository.DoctorProfileRepository;
import com.clinica.api.module.doctor.infrastructure.repository.SpecialtyRepository;
import com.clinica.api.module.user.application.dto.AuthRequest;
import com.clinica.api.module.user.application.dto.AuthResponse;
import com.clinica.api.module.user.application.dto.RegisterDoctorRequest;
import com.clinica.api.module.user.application.dto.RegisterRequest;
import com.clinica.api.module.user.domain.Role;
import com.clinica.api.module.user.infrastructure.entity.PatientProfileEntity;
import com.clinica.api.module.user.infrastructure.entity.UserEntity;
import com.clinica.api.module.user.infrastructure.repository.PatientProfileRepository;
import com.clinica.api.module.user.infrastructure.repository.UserRepository;
import com.clinica.api.shared.config.security.JwtService;
import com.clinica.api.shared.exception.domain.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio Central de Autenticación.
 * Aquí ocurre el proceso CEREBRAL de Registros y Logins.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final UserRepository userRepository;
        private final PatientProfileRepository patientProfileRepository;
        private final DoctorProfileRepository doctorProfileRepository;
        private final SpecialtyRepository specialtyRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        /**
         * REGISTRO DE PACIENTE
         */
        @Transactional
        public AuthResponse registerPatient(RegisterRequest request) {

                log.info("Iniciando registro para el paciente: {}", request.getEmail());

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new ConflictException("Ya existe un usuario con este correo electrónico.");
                }

                var user = UserEntity.builder()
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.ROLE_PATIENT)
                                .isActive(true)
                                .build();

                var savedUser = userRepository.save(user);

                var patientProfile = PatientProfileEntity.builder()
                                .user(savedUser)
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .build();

                patientProfileRepository.save(patientProfile);

                var jwtToken = jwtService.generateToken(savedUser);
                log.info("Paciente {} registrado exitosamente", user.getEmail());

                return AuthResponse.builder()
                                .token(jwtToken)
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .build();
        }

        /**
         * ALTA DE DOCTORES
         * Operación Transaccional, creará el Identity User y el Perfil del Médico.
         */
        @Transactional
        public AuthResponse registerDoctor(RegisterDoctorRequest request) {

                log.info("Iniciando alta institucional del Médico: {}", request.getEmail());

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new ConflictException("El correo ya está en uso por otro empleado o paciente.");
                }

                // Si mandan una especialidad que no existe, la damos de alta de inmediato
                // (Opciones flexibles)
                SpecialtyEntity specialty = specialtyRepository.findByName(request.getSpecialtyName().trim())
                                .orElseGet(() -> specialtyRepository.save(SpecialtyEntity.builder()
                                                .name(request.getSpecialtyName().trim()).build()));

                var user = UserEntity.builder()
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.ROLE_DOCTOR) // Otorga poderes de DOCTOR
                                .isActive(true)
                                .build();

                var savedUser = userRepository.save(user);

                var doctorProfile = DoctorProfileEntity.builder()
                                .user(savedUser)
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .specialty(specialty)
                                .experienceYears(request.getExperienceYears())
                                .consultationPrice(
                                                request.getConsultationPrice() != null ? request.getConsultationPrice()
                                                                : BigDecimal.ZERO)
                                .build();

                doctorProfileRepository.save(doctorProfile);

                // JWT del Doctor recién dado de alta
                var jwtToken = jwtService.generateToken(savedUser);
                log.info("Doctor {} ({}) dado de alta exitosamente en plataforma", user.getEmail(),
                                specialty.getName());

                return AuthResponse.builder()
                                .token(jwtToken)
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .build();
        }

        /**
         * INICIO DE SESIÓN
         */
        public AuthResponse authenticate(AuthRequest request) {
                log.info("Intentando Login para email: {}", request.getEmail());

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                var user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow();

                var jwtToken = jwtService.generateToken(user);

                return AuthResponse.builder()
                                .token(jwtToken)
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .build();
        }
}
