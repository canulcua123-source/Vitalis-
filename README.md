# 🏥 Vitalis - Health Management System (Full Stack)

![Flutter](https://img.shields.io/badge/Flutter-%2302569B.svg?style=for-the-badge&logo=Flutter&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Architecture-Clean-blue?style=for-the-badge)
![Security-Grade](https://img.shields.io/badge/Security-Enterprise-red?style=for-the-badge)

Vitalis is a comprehensive, enterprise-grade medical ecosystem designed for both patients and doctors. This repository demonstrates professional software engineering standards, featuring a high-performance **Flutter Mobile App** and a robust **Spring Boot REST API**.

---

## 🏗️ Advanced Engineering Architecture

Vitalis is built following **Clean Architecture** and **SOLID** principles, ensuring a complete separation of concerns and high testability.

### Layered Structure (Full Stack)
- **Frontend (Flutter)**: Implements the **BLoC (Business Logic Component)** pattern for predictable state management and unidirectional data flow.
- **Backend (Spring Boot)**: Architected with a modular service-oriented approach, using **Flyway** for database migrations and **JPA/Hibernate** for persistent data integrity.
- **Domain-Driven Design**: Core business logic is encapsulated in pure Domain layers, decoupling it from infrastructure and UI frameworks.

### Dependency Injection
- Used across the stack (BlocProviders/GetIt in Front, @Autowired/Constructor injection in Back) to decouple implementation from interfaces, allowing for seamless integration and simplified unit testing with Mocks.

---

## 🔐 Security & Data Protection

Healthcare data requires the highest level of protection. Vitalis implements multi-layered security:

- **Hardware-Backed Encryption**: Sensitive session tokens and **JWTs** are stored using **`flutter_secure_storage`**, leveraging OS-level security (iOS Keychain and Android Keystore) with **AES-256** encryption.
- **Secure Data in Transit**: All API communications are performed over **HTTPS**, ensuring data integrity and confidentiality between the mobile client and the Spring Boot server.
- **Backend Hardening**: 
  - Implementation of **Spring Security** for fine-grained authorization.
  - Rate limiting using **Bucket4j** to prevent brute-force attacks.
  - Strict input validation and sanitization using JSR-303 (Bean Validation).
- **Sensitive Data Obfuscation**: Passwords are hashed using BCrypt, and sensitive info is never exposed in logs or plain text.

---

## 📈 Scalability & Performance

- **Performance Optimization**:
  - **Smart Image Compression**: Client-side multi-stage compression reduces bandwidth usage by ~80% before transmission.
  - **Memory Management**: Optimized widget tree depth and strategic use of `const` constructors to minimize UI rebuilds.
  - **Database Efficiency**: High-performance PostgreSQL queries indexed for low-latency response.
- **Scalability**:
  - **Modular Feature Design**: Independent modules for Auth, Patient, and Doctor, prepared for microservice extraction if needed.
  - **Infrastructure**: Designed for cloud-native deployment with support for environment-based configurations (.env).

---

## 🎨 UX Engineering (2026 Standards)

Visual excellence is a functional requirement:
- **Micro-interactions**: Subtle haptic and visual feedback for every user action.
- **State-Aware UI**: Comprehensive handling of `Loading`, `Shimmer`, `Error`, and `Empty` states.
- **Interactive Geodata**: OpenStreetMap implementation with custom markers and Reverse Geocoding.
- **Industrial Design**: Modern medical aesthetic with refined typography and consistent iconography.

---

## 🛠️ Tech Stack & DevOps

- **Mobile**: Flutter, BLoC, Dio, OpenStreetMap, Flutter Secure Storage.
- **API**: Java 21, Spring Boot, Spring Security, Flyway, Maven, Bucket4j.
- **Database**: PostgreSQL (Supabase).
- **DevOps**: Docker & Docker Compose, Environment separation (Dotenv), Strict Static Analysis (Flutter Lints).

---
*Vitalis represents a commitment to software engineering excellence in the healthcare technology space.*
