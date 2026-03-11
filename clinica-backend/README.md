# 🏥 Vitalis Healthcare Backend - Enterprise API

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![API Docs](https://img.shields.io/badge/API-Documentation-red?style=for-the-badge&logo=read-the-docs)](./API_SPECIFICATION.md)

Este es el motor central de una plataforma de gestión clínica de alta gama. No es solo un CRUD; es un sistema robusto diseñado para manejar transacciones financieras reales, notificaciones en tiempo real y escalabilidad empresarial.

---

## 📖 Documentación del Proyecto

Para una inmersión técnica profunda, consulta los pilares de nuestra documentación:

- 🛡️ **[Sistema de Autenticación y Seguridad](./docs/SECURITY_AUTH.md)**: Detalle de JWT, Roles y Rate Limiting.
- 🗄️ **[Arquitectura de Base de Datos](./DATABASE_SCHEMA.md)**: Diagrama ERD y optimizaciones de PostgreSQL.
- 👉 **[Especificación de la API y Guía de Postman](./API_SPECIFICATION.md)**: Especificación técnica y pruebas automatizadas.
- 🧪 **[Manual de Pruebas Postman](./docs/POSTMAN_TESTING.md)**: Guía paso a paso para validación total.

---

## 🚀 Lo que este proyecto demuestra

Este backend es un testimonio de **mejores prácticas de ingeniería de software** y preparación para nivel productivo:

*   **Arquitectura Limpia (Layered Architecture)**: Separación clara de responsabilidades entre Presentación, Aplicación, Dominio e Infraestructura.
*   **Seguridad Bancaria**:
    *   Autenticación basada en **JWT** (JSON Web Tokens).
    *   Control de acceso basado en roles (**RBAC**) para Médicos, Pacientes y Administradores.
    *   Protección contra ataques de fuerza bruta mediante **Rate Limiting** (Bucket4j).
*   **Integraciones Críticas de Terceros**:
    *   **Stripe Webhooks**: Procesamiento de pagos 100% fiable con verificación de firma criptográfica e idempotencia.
    *   **Firebase Admin SDK**: Notificaciones push integradas de forma robusta.
*   **Escalabilidad**: Implementación de **paginación nativa** en todos los listados para asegurar el rendimiento con grandes volúmenes de datos.
*   **DevOps & Docker**: Entorno completamente containerizado y reproducible, listo para CI/CD.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
| :--- | :--- |
| **Lenguaje** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.2 + Spring Security |
| **Persistencia** | Spring Data JPA + Hibernate |
| **Base de Datos** | PostgreSQL 16 |
| **Migraciones** | Flyway (Versionado de BD) |
| **Pagos** | Stripe API (v2025-11-17) |
| **Notificaciones** | Firebase Cloud Messaging (FCM) |
| **Infraestructura** | Docker / Docker Compose |
| **Otros** | Lombok, MapStruct, Jakarta Validation, Bucket4j |

---

## 🔥 Funcionalidades Implementadas

### 🔐 Gestión de Usuarios y Seguridad
*   Registro y Login con roles diferenciados.
*   Filtros de seguridad para denegar acceso no autorizado a rutas médicas sensibles.
*   Limitación de peticiones (Rate Limit) por IP para prevenir abusos.

### 📅 Agenda y Citas
*   Gestión de disponibilidad médica por horarios y días de la semana.
*   Búsqueda de slots disponibles en tiempo real.
*   Agendamiento de citas con bloqueo automático de horarios.

### 💳 Procesamiento de Pagos (Stripe)
*   Creación de `PaymentIntents` seguros.
*   **Webhook Listener**: Confirmación de citas **solo** cuando el pago es verificado por Stripe mediante notificaciones asíncronas seguras.
*   Gestión de reembolsos y estados de transacción.

### 🔔 Notificaciones Centralizadas
*   Historial de notificaciones in-app.
*   Envío de notificaciones Push automáticas al actualizar estados de citas.
*   Mantenimiento de tokens de dispositivos (FCM) por usuario.

---

## 📦 Instalación y Uso

El proyecto está diseñado para arrancar con un solo comando gracias a Docker:

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/Vitalis-Healthcare-Backend.git
   cd Vitalis-Healthcare-Backend
   ```

2. **Configurar variables de entorno**:
   Crea un archivo `.env.local` basado en el ejemplo proporcionado con tus llaves de Stripe y Firebase.

3. **Levantar con Docker**:
   ```bash
   docker-compose up -d --build
   ```

La API estará disponible en `http://localhost:8081` y la base de datos se migrará automáticamente a la última versión disponible.

---

## 📜 Licencia

Este proyecto es de uso personal para portafolio. Todos los derechos reservados.
