package com.clinica.api.module.user.infrastructure.repository;

import com.clinica.api.module.user.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    
    // Spring Data JPA proveerá la implementación mágica de esta query 
    // basándose únicamente en la convención de nombres de los métodos.
    Optional<UserEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
