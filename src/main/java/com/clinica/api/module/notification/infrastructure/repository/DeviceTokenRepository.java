package com.clinica.api.module.notification.infrastructure.repository;

import com.clinica.api.module.notification.infrastructure.entity.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceTokenEntity, UUID> {

    // Obtiene TODOS los celulares actuales donde el usuario tiene la app iniciada
    List<DeviceTokenEntity> findByUserId(UUID userId);

    // Sirve para no meter datos basura duplicados cuando el app reenvia su token
    Optional<DeviceTokenEntity> findByFcmToken(String fcmToken);

    // Bórralo cuando el usuario haga Log Out
    @Modifying
    @Query("DELETE FROM DeviceTokenEntity d WHERE d.fcmToken = :token")
    void deleteByFcmToken(@Param("token") String fcmToken);
}
