#!/bin/bash
echo "1. Login Doctor..."
DOC_RES=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email": "dr.smith.cardiologia@clinica.com", "password": "SecurePassword123!"}')
DOC_TOKEN=$(echo $DOC_RES | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo -e "\n2. Agregar Disponibilidad (Lunes 9 AM - 2 PM)..."
curl -s -X POST http://localhost:8080/api/v1/doctors/availability \
  -H "Authorization: Bearer $DOC_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dayOfWeek": 1,
    "startTime": "09:00:00",
    "endTime": "14:00:00"
  }' | jq

echo -e "\n3. Obtener Disponibilidad del Doctor..."
curl -s -X GET http://localhost:8080/api/v1/doctors/availability/me \
  -H "Authorization: Bearer $DOC_TOKEN" | jq

echo -e "\n4. Login Paciente..."
PAT_RES=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email": "paciente.juan@gmail.com", "password": "SecurePassword123!"}')
PAT_TOKEN=$(echo $PAT_RES | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo -e "\n5. Listar Doctores (El paciente busca un Cardiólogo)..."
ALL_DOCS=$(curl -s -X GET http://localhost:8080/api/v1/doctors -H "Authorization: Bearer $PAT_TOKEN")
echo $ALL_DOCS | jq
DOC_ID=$(echo $ALL_DOCS | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)

echo -e "\n6. Agendar Cita (Lunes 2 de Marzo 2026, 10:00 AM)..."
APT_RES=$(curl -s -X POST http://localhost:8080/api/v1/appointments \
  -H "Authorization: Bearer $PAT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"doctorId\": \"$DOC_ID\",
    \"appointmentDate\": \"2026-03-09\",
    \"startTime\": \"10:00:00\",
    \"notes\": \"Dolor en el pecho fuerte\"
  }")
echo $APT_RES | jq
APT_ID=$(echo $APT_RES | grep -o '"appointmentId":"[^"]*' | cut -d'"' -f4)

echo -e "\n7. Doctor Consulta su Agenda Diaria (2 de Marzo)..."
curl -s -X GET "http://localhost:8080/api/v1/appointments/doctor/daily?date=2026-03-09" -H "Authorization: Bearer $DOC_TOKEN" | jq

echo -e "\n8. Generar Intento de Pago (Stripe)..."
curl -s -X POST http://localhost:8080/api/v1/payments/intent \
  -H "Authorization: Bearer $PAT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"appointmentId\": \"$APT_ID\"
  }" | jq

echo -e "\n9. Notificar Pago Falso (Lo que haría la App tras Stripe)..."
curl -s -X POST http://localhost:8080/api/v1/payments/confirm -w "\nHTTP Status: %{http_code}\n" \
  -H "Authorization: Bearer $PAT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"appointmentId\": \"$APT_ID\",
    \"amount\": 1200.00,
    \"paymentMethod\": \"CARD\",
    \"paymentProvider\": \"STRIPE\",
    \"providerPaymentId\": \"pi_test_5678\"
  }"

echo -e "\n10. Paciente consulta su Historial..."
curl -s -X GET http://localhost:8080/api/v1/appointments/me -H "Authorization: Bearer $PAT_TOKEN" | jq

echo -e "\n11. Doctor revisa sus Notificaciones (Debe tener aviso de cobro)..."
curl -s -X GET http://localhost:8080/api/v1/notifications/me -H "Authorization: Bearer $DOC_TOKEN" | jq

echo -e "\n12. Marcando la Cita como 'COMPLETED' y Dejando Reseña..."
curl -s -X PATCH http://localhost:8080/api/v1/appointments/$APT_ID/status \
  -H "Authorization: Bearer $DOC_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED"}'

curl -s -X POST http://localhost:8080/api/v1/reviews \
  -H "Authorization: Bearer $PAT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"appointmentId\": \"$APT_ID\",
    \"rating\": 5,
    \"comment\": \"Excelente doctor, me salvó la vida.\"
  }" | jq

