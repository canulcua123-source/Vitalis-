# 🧪 Guía de Pruebas con Postman - Vitalis API

Esta guía proporciona los pasos exactos para validar el funcionamiento del sistema Vitalis utilizando Postman, incluyendo automatización de tokens y validación de estados.

---

## 🏁 Paso 1: Configuración del Entorno

1. **Base URL**: Configura una variable de entorno `{{baseUrl}}` con el valor `http://localhost:8081/api/v1`.
2. **Global Auth**: En la pestaña de **Authorization** de tu colección, selecciona `Bearer Token` y usa la variable `{{access_token}}`.

---

## 🧪 Flujo de Pruebas Paso a Paso

### 1️⃣ Registro y Login (Público)
- **Endpoint**: `POST {{baseUrl}}/auth/login`
- **Body**:
```json
{
  "email": "juan.perez@example.com",
  "password": "password123"
}
```
- **Script de Automatización (Pestaña "Tests")**:
Este script captura el token automáticamente para que no tengas que copiarlo y pegarlo manualmente en cada petición:
```javascript
var jsonData = pm.response.json();
pm.environment.set("access_token", jsonData.token);
pm.test("Token guardado correctamente", function () {
    pm.expect(pm.environment.get("access_token")).to.not.be.null;
});
```

### 2️⃣ Consultar Doctores y Disponibilidad
- **Listar Doctores**: `GET {{baseUrl}}/doctors`
  - Verifica que el campo `specialty` y `consultationPrice` sean correctos.
- **Ver Slots**: `GET {{baseUrl}}/doctors/{id}/availability?date=2024-12-25`
  - Comprueba que los slots marcados como `isAvailable: true` coincidan con el horario laboral del doctor.

### 3️⃣ Agendamiento de Cita (Requiere Auth)
- **Endpoint**: `POST {{baseUrl}}/appointments`
- **Body**:
```json
{
  "doctorId": "id-del-doctor",
  "appointmentDate": "2024-12-25",
  "startTime": "09:00:00"
}
```
- **Resultado Esperado (201 Created)**: El objeto devuelto tendrá el estado `PENDING_PAYMENT`.

### 4️⃣ Simulación de Pago (Stripe)
- **Crear Intento**: `POST {{baseUrl}}/payments/intent`
  - Body: `{ "appointmentId": "id-de-la-cita-anterior" }`
  - Captura el `clientSecret`. En un entorno real, este secreto se usa para completar el pago en el móvil/web.

### 5️⃣ Verificación de Notificaciones
- **Endpoint**: `GET {{baseUrl}}/notifications/me`
- **Acción**: Verifica que tras agendar la cita, se haya generado una entrada en el historial de notificaciones push.

---

## 🛠️ Solución de Problemas Comunes

### Error: `401 Unauthorized`
- **Causa**: El token JWT ha expirado o no se está enviando en el header.
- **Solución**: Ejecuta de nuevo el request de **Login** para refrescar la variable `{{access_token}}`.

### Error: `403 Forbidden`
- **Causa**: Estas intentando acceder con un rol equivocado (ej. un paciente intentando entrar a la agenda interna de un doctor).
- **Solución**: Asegúrate de estar usando las credenciales correctas para cada rol.

### Error: `429 Too Many Requests`
- **Causa**: Has excedido el límite de peticiones configurado en el `RateLimitFilter`.
- **Solución**: Espera un minuto o deshabilita el rate limit en el archivo `.env.local` para pruebas intensivas.

---

## 💡 Tips de Experto para el Reclutador
- **Monitorización en Vivo**: Deja abierta la terminal donde corre el backend. Verás cómo los **Webhooks de Stripe** llegan en tiempo real y cambian el estado de la cita en la base de datos sin necesidad de refrescar manualmente.
- **Pruebas de Estrés**: Intenta enviar 10 peticiones seguidas de login para ver el **Rate Limiter** en acción.

---

**Desarrollado por:** Jesus Guadalupe Canul Cua - Vitalis Project 🚀
