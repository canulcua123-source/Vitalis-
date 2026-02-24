package com.clinica.api.module.notification.application.service;

import com.clinica.api.module.notification.application.dto.NotificationResponse;
import com.clinica.api.module.notification.application.dto.RegisterDeviceTokenRequest;
import com.clinica.api.module.notification.infrastructure.entity.DeviceTokenEntity;
import com.clinica.api.module.notification.infrastructure.repository.DeviceTokenRepository;
import com.clinica.api.module.notification.infrastructure.entity.NotificationEntity;
import com.clinica.api.module.notification.infrastructure.repository.NotificationRepository;
import com.clinica.api.module.user.infrastructure.entity.UserEntity;
import com.clinica.api.module.user.infrastructure.repository.UserRepository;
import com.clinica.api.shared.exception.domain.ConflictException;
import com.clinica.api.shared.exception.domain.ResourceNotFoundException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Interfaz de Servicio a Servicio (S2S) para detonar notificaciones internas
     */
    @Transactional
    public void createNotification(UserEntity targetUser, String title, String message, String type) {
        NotificationEntity notification = NotificationEntity.builder()
                .user(targetUser)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notificación '{}' creada y encolada para Push en el usuario {}", title, targetUser.getEmail());

        // 2) Lanzar FCM Push si Firebase está configurado
        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
            log.warn("FCM omitido para {}: Firebase no está inicializado.", targetUser.getEmail());
            return;
        }

        List<DeviceTokenEntity> devices = deviceTokenRepository.findByUserId(targetUser.getId());

        for (DeviceTokenEntity device : devices) {
            try {
                Message fcmMessage = Message.builder()
                        .setToken(device.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(message)
                                .build())
                        .putData("type", type)
                        .putData("datetime", LocalDateTime.now().toString())
                        .build();

                String response = FirebaseMessaging.getInstance().send(fcmMessage);
                log.info("🚀 PUSH Exitoso {}. FCM Response: {}", device.getDeviceInfo(), response);

            } catch (Exception e) {
                log.error("❌ Falló envío push a dispositivo del usuario {}: {}", targetUser.getEmail(), e.getMessage());
            }
        }
    }

    /**
     * Método Auxiliar si solo tenemos el ID
     */
    @Transactional
    public void createNotification(UUID targetUserId, String title, String message, String type) {
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destino no encontrado"));
        createNotification(targetUser, title, message, type);
    }

    /**
     * Devuelve el historial de la 'Campanita' de notificaciones
     */
    public List<NotificationResponse> getMyNotifications(String email) {
        return getMyNotificationsPaged(email, Pageable.unpaged()).getContent();
    }

    public Page<NotificationResponse> getMyNotificationsPaged(String email, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void markAsRead(String email, UUID notificationId) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no existe"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No puedes alterar notificaciones de otros");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void registerDeviceToken(String email, RegisterDeviceTokenRequest request) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        DeviceTokenEntity tokenEntity = deviceTokenRepository.findByFcmToken(request.getFcmToken())
                .map(existing -> {
                    existing.setUser(user);
                    existing.setDeviceInfo(request.getDeviceInfo());
                    return existing;
                })
                .orElseGet(() -> DeviceTokenEntity.builder()
                        .user(user)
                        .fcmToken(request.getFcmToken())
                        .deviceInfo(request.getDeviceInfo())
                        .build());

        deviceTokenRepository.save(tokenEntity);
    }

    @Transactional
    public void removeDeviceToken(String email, String fcmToken) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        DeviceTokenEntity tokenEntity = deviceTokenRepository.findByFcmToken(fcmToken)
                .orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));

        if (!tokenEntity.getUser().getId().equals(user.getId())) {
            throw new ConflictException("No puedes borrar un token que no te pertenece");
        }

        deviceTokenRepository.deleteByFcmToken(fcmToken);
    }

    private NotificationResponse mapToResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
