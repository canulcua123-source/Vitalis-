package com.clinica.api.module.notification.infrastructure.repository;

import com.clinica.api.module.notification.infrastructure.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    @Query("SELECT n FROM NotificationEntity n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    org.springframework.data.domain.Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.user.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") UUID userId);
}
