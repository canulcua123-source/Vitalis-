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
- Used across the stack to decouple implementation from interfaces, allowing for seamless integration and simplified unit testing with Mocks.

---

## 🔐 Deep Security Layer

Healthcare data requires the highest level of protection. Vitalis implements multi-layered security:

- **Token Management**: Secure handling of **JWT (JSON Web Tokens)** with encrypted local storage (AES-256) on the device.
- **Communication Security**:
  - **TLS 1.3** for all API requests.
  - **Interceptor-based Sanitization**: Cleaning and validating all outgoing requests and incoming responses.
- **Sensitive Data Obfuscation**: Critical information is never stored in plain text and is obfuscated during transmission.
- **Backend Hardening**: Rate limiting, CORS protection, and strict input validation using Spring Security.

---

## 📈 Scalability & Performance

- **Performance Optimization**:
  - **Smart Image Compression**: Client-side multi-stage compression reduces bandwidth usage by ~80% without visible quality loss.
  - **Lazy Loading & Infinite Scrolling**: Optimized memory usage for medicine and doctor listings.
  - **Database Efficiency**: High-performance PostgreSQL queries indexed for low-latency response.
- **Scalability**:
  - **Modular Feature Design**: Independent modules for Auth, Patient, and Doctor, prepared for microservice extraction if needed.
  - **Supabase Integration**: Leveraging high-availability cloud infrastructure for data persistence.

---

## 🎨 UX Engineering (2026 Standards)

Visual excellence is a functional requirement:
- **Micro-interactions**: Subtle haptic and visual feedback for every user action.
- **State-Aware UI**: Comprehensive handling of `Loading`, `Shimmer`, `Error`, and `Empty` states.
- **Interactive Geodata**: OpenStreetMap implementation with custom markers and Reverse Geocoding.
- **Premium Design**: Modern aesthetic with glassmorphism elements, soft shadows, and refined typography.

---

## 🚀 Features & Modules

### 📱 Frontend (Flutter)
- **Modern Medical Design**: Professional color palette and intuitive layouts.
- **Role Selection**: Dynamic toggle for Patient and Doctor accounts with custom animations.
- **Profile Management**: Profile photo selection with automatic compression and multipart upload.
- **Office Location**: Interactive map selector using OpenStreetMap.

### ⚙️ Backend (Spring Boot)
- **Secure Auth**: JWT-based security and password hashing.
- **Data Integrity**: Automated JPA/Hibernate mappings and Flyway version control.
- **File Handling**: Multipart storage for profile pictures.

---

## 🛠️ Tech Stack & DevOps

- **Mobile**: Flutter, BLoC, Dio, OpenStreetMap, Image Picker.
- **API**: Java 17+, Spring Boot, Spring Security, Flyway, Maven.
- **Database**: PostgreSQL (Supabase).
- **DevOps**: Docker & Docker Compose, Environment separation (Dev/Prod), Strict Git linting.

---
*Vitalis represents a commitment to software engineering excellence in the healthcare technology space.*
