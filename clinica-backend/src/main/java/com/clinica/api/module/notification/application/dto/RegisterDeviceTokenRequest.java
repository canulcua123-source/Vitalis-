package com.clinica.api.module.notification.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceTokenRequest {

    @NotBlank(message = "El token FCM es obligatorio")
    private String fcmToken;

    private String deviceInfo; // Ej: "iPhone 15 Pro", "Samsung S23"
}
