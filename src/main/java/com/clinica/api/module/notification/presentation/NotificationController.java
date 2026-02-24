package com.clinica.api.module.notification.presentation;

import com.clinica.api.module.notification.application.dto.NotificationResponse;
import com.clinica.api.module.notification.application.dto.RegisterDeviceTokenRequest;
import com.clinica.api.module.notification.application.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Ambos doctores y pacientes la necesitan
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyNotificationsPaged(userDetails.getUsername(), pageable));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/device-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerDeviceToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterDeviceTokenRequest request) {
        notificationService.registerDeviceToken(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/device-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeDeviceToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fcmToken) {
        notificationService.removeDeviceToken(userDetails.getUsername(), fcmToken);
        return ResponseEntity.noContent().build();
    }
}
