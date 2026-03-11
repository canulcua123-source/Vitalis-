# 🏥 Documentación API - Vitalis Healthcare Core

API REST profesional para la gestión de clínicas médicas, agendamiento de citas y procesamiento de pagos seguros.

**Base URL:** `http://localhost:8081/api/v1/`

---

## 📚 Índice

1. [Autenticación](#autenticación)
2. [Gestión de Doctores](#gestión-de-doctores)
3. [Citas Médicas](#citas-médicas)
4. [Pagos y Facturación](#pagos-y-facturación)
5. [Notificaciones PUSH](#notificaciones-push)
6. [Reseñas y Feedback](#reseñas-y-feedback)

---

## Autenticación

Vitalis utiliza **JWT (JSON Web Tokens)** para la seguridad de todas sus operaciones transaccionales.

### Registro de Pacientes
#### Crear una nueva cuenta de paciente
```
POST /auth/register
```
**Cuerpo (JSON):**
```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@example.com",
  "password": "password123",
  "phone": "+529991234567",
  "birthDate": "1990-05-15"
}
```
**Respuesta (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1Ni...",
  "email": "juan.perez@example.com",
  "role": "ROLE_PATIENT"
}
```

---

### Iniciar Sesión
#### Obtener token de acceso
```
POST /auth/login
```
**Cuerpo:**
```json
{
  "email": "juan.perez@example.com",
  "password": "password123"
}
```
**Respuesta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1Ni...",
  "email": "juan.perez@example.com",
  "role": "ROLE_PATIENT"
}
```
**Postman Test:**
```javascript
var jsonData = pm.response.json();
pm.environment.set("access_token", jsonData.token);
pm.test("Status code is 200", () => pm.response.to.have.status(200));
```

---

## Gestión de Doctores

### Listar Equipo Médico
#### Obtener todos los doctores activos
```
GET /doctors
```
**Autenticación:** Autenticado
**Parámetros:** `page`, `size`, `sort`
**Respuesta (200 OK):**
```json
{
  "content": [
    {
      "id": "507f1f77bcf86cd799439011",
      "firstName": "Erik",
      "lastName": "García",
      "specialty": "Cardiología",
      "consultationPrice": 850.00,
      "rating": 4.9
    }
  ],
  "totalElements": 45,
  "totalPages": 5
}
```

---

### Consultar Disponibilidad
#### Obtener slots libres para una fecha específica
```
GET /doctors/{doctorId}/availability?date=2024-12-25
```
**Respuesta:**
```json
[
  { "startTime": "09:00:00", "endTime": "09:30:00", "isAvailable": true },
  { "startTime": "09:30:00", "endTime": "10:00:00", "isAvailable": false }
]
```

---

## Citas Médicas

### Agendar Cita
#### Reservar un espacio médico
```
POST /appointments
```
**Autenticación:** ROLE_PATIENT
**Cuerpo:**
```json
{
  "doctorId": "507f1f77bcf86cd799439011",
  "appointmentDate": "2024-12-25",
  "startTime": "09:00:00",
  "notes": "Consulta general"
}
```
**Respuesta (201 Created):**
```json
{
  "appointmentId": "uuid-123",
  "status": "PENDING_PAYMENT",
  "doctorName": "Erik García",
  "startTime": "09:00:00"
}
```

---

### Historial del Paciente
#### Consultar citas propias
```
GET /appointments/me
```
**Autenticación:** ROLE_PATIENT
**Respuesta:** Lista paginada del historial médico del usuario autenticado.

---

## Pagos y Facturación

### Crear Intento de Pago
#### Iniciar transacción con Stripe
```
POST /payments/intent
```
**Autenticación:** ROLE_PATIENT
**Cuerpo:**
```json
{ "appointmentId": "uuid-cita" }
```
**Respuesta (200 OK):**
```json
{
  "clientSecret": "pi_3P...",
  "stripeAccountId": "acct_..."
}
```

---

### Webhook de Stripe
#### Procesar eventos de pago exitosos (Automático)
```
POST /payments/webhook
```
**Header Rekuerido:** `Stripe-Signature`
**Descripción:** Endpoint consumido solo por Stripe para notificar el éxito de una transacción de forma asíncrona y segura.

---

## Notificaciones PUSH

### Registrar Dispositivo
#### Vincular token de Firebase Cloud Messaging
```
POST /notifications/device-token
```
**Autenticación:** Autenticado
**Cuerpo:**
```json
{
  "fcmToken": "fcm-token-12345",
  "deviceInfo": "iPhone 15 Pro"
}
```

---

## Reseñas y Feedback

### Calificar Consulta
#### Enviar feedback después de la cita
```
POST /reviews
```
**Autenticación:** ROLE_PATIENT
**Cuerpo:**
```json
{
  "appointmentId": "uuid-cita",
  "rating": 5,
  "comment": "Excelente atención médico."
}
```

---

## 🛡️ Sistema de Permisos Detallado

A continuación se detalla el nivel de acceso requerido para cada operación en la API:

| Módulo | Endpoint | GET | POST | PATCH/PUT | DELETE | Rol Mínimo |
| :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| **Auth** | `/auth/login` | - | ✅ | - | - | Público |
| **Auth** | `/auth/register` | - | ✅ | - | - | Público |
| **Auth** | `/auth/register/doctor` | - | ✅ | - | - | **ADMIN** |
| **Doctores** | `/doctors` | ✅ | - | - | - | Autenticado |
| **Doctores** | `/doctors/{id}/availability` | ✅ | - | - | - | Autenticado |
| **Agenda** | `/doctors/availability` | - | ✅ | - | - | **DOCTOR** |
| **Agenda** | `/doctors/availability/me` | ✅ | - | - | - | **DOCTOR** |
| **Citas** | `/appointments` | - | ✅ | - | - | **PATIENT** |
| **Citas** | `/appointments/me` | ✅ | - | - | - | **PATIENT** |
| **Citas** | `/appointments/doctor/daily` | ✅ | - | - | - | **DOCTOR** |
| **Citas** | `/appointments/{id}/status` | - | - | ✅ | - | **PATIENT / DOCTOR** |
| **Pagos** | `/payments/intent` | - | ✅ | - | - | **PATIENT** |
| **Pagos** | `/payments/confirm`| - | ✅ | - | - | **PATIENT** |
| **Pagos** | `/payments/webhook` | - | ✅ | - | - | Público (Stripe) |
| **Notif.** | `/notifications/me` | ✅ | - | - | - | Autenticado |
| **Notif.** | `/notifications/device-token` | - | ✅ | - | ✅ | Autenticado |
| **Reviews**| `/reviews` | - | ✅ | - | - | **PATIENT** |

---

## 🚀 Notas de Implementación (Para Reclutadores)

- **JWT Stateless**: Implementación de seguridad sin estado para alta disponibilidad.
- **Paginación**: Todos los endpoints de listas usan `Pageable` de Spring para evitar saturación de memoria.
- **Validación DTO**: Uso intensivo de `@Valid` y `@NotNull` para asegurar la integridad de los datos antes de persistir.
- **Control de Tráfico**: Integración de Rate Limiting para proteger endpoints sensibles contra ataques de denegación de servicio.

---

**Desarrollado por:** Jesus Guadalupe Canul Cua - Portafolio Profesional 🚀
