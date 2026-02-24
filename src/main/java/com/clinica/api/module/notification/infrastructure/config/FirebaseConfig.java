package com.clinica.api.module.notification.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import java.io.FileInputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${app.firebase.required:false}")
    private boolean firebaseRequired;

    @Value("${app.firebase.credentials-path:}")
    private String credentialsPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream credentialStream = null;
        boolean hasCredentials = false;

        if (credentialsPath != null && !credentialsPath.isBlank()) {
            credentialStream = new FileInputStream(credentialsPath);
            hasCredentials = true;
        } else {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (resource.exists()) {
                credentialStream = resource.getInputStream();
                hasCredentials = true;
            }
        }

        if (!hasCredentials || credentialStream == null) {
            String msg = "Credenciales de Firebase no encontradas. Las notificaciones Push estarán deshabilitadas.";
            if (firebaseRequired) {
                throw new IllegalStateException("ERROR CRÍTICO: " + msg);
            }
            log.warn("⚠️ {} (app.firebase.required=false)", msg);
            return null; // El servicio debe manejar el bean nulo de forma segura
        }

        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialStream))
                    .build();

            log.info("✅ Firebase inicializado correctamente para Notificaciones Push");
            return FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            log.error("❌ Error al inicializar Firebase: {}", e.getMessage());
            if (firebaseRequired) {
                throw e;
            }
            return null;
        }
    }
}
