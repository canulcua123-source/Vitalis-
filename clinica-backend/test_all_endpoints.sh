#!/usr/bin/env bash
set -euo pipefail

# ======================
# CONFIG
# ======================
BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL_PATIENT="paciente@test.com"
PASS_PATIENT="123456"
EMAIL_DOCTOR="doctor@test.com"
PASS_DOCTOR="Doctor123!"

FCM_TOKEN="fcm_token_de_prueba"
DEVICE_INFO="iPhone 15 Pro"

# Fecha de prueba (hoy)
DATE="2026-02-24"
MAX_DATE_TRIES=14

# ======================
# HELPERS (requiere python3)
# ======================
json_get() {
  python3 -c 'import json,sys; data=json.load(sys.stdin); path=sys.argv[1].split("."); cur=data;
for p in path:
    if isinstance(cur, list):
        cur=cur[int(p)]
    else:
        cur=cur[p]
print(cur)' "$1"
}

# ======================
# REGISTROS
# ======================
echo "== Register patient =="
curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"Juan",
    "lastName":"Perez",
    "email":"'"$EMAIL_PATIENT"'",
    "password":"'"$PASS_PATIENT"'"
  }' || true

echo "== Register doctor =="
curl -s -X POST "$BASE_URL/api/v1/auth/register/doctor" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"Ana",
    "lastName":"Lopez",
    "email":"'"$EMAIL_DOCTOR"'",
    "password":"'"$PASS_DOCTOR"'",
    "specialtyName":"Cardiologia",
    "experienceYears":5,
    "consultationPrice":500
  }' || true

# ======================
# LOGIN
# ======================
echo "== Login patient =="
PATIENT_LOGIN=""
for i in 1 2 3; do
  PATIENT_LOGIN=$(curl -sS -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"'"$EMAIL_PATIENT"'","password":"'"$PASS_PATIENT"'"}')
  if echo "$PATIENT_LOGIN" | grep -q "\"token\""; then
    break
  fi
  echo "Login patient retry $i (posible rate limit): $PATIENT_LOGIN"
  sleep 2
done
if [ -z "$PATIENT_LOGIN" ] || ! echo "$PATIENT_LOGIN" | grep -q "\"token\""; then
  echo "ERROR: Login patient no devolvió token"
  exit 1
fi
echo "Login patient response: $PATIENT_LOGIN"
TOKEN_PATIENT=$(printf "%s" "$PATIENT_LOGIN" | json_get token)
echo "TOKEN_PATIENT ok"

echo "== Login doctor =="
DOCTOR_LOGIN=$(curl -sS -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"'"$EMAIL_DOCTOR"'","password":"'"$PASS_DOCTOR"'"}')
echo "Login doctor response: $DOCTOR_LOGIN"
if echo "$DOCTOR_LOGIN" | grep -q "\"token\""; then
  TOKEN_DOCTOR=$(printf "%s" "$DOCTOR_LOGIN" | json_get token)
  echo "TOKEN_DOCTOR ok"
else
  TOKEN_DOCTOR=""
  echo "WARN: No se pudo autenticar doctor. Se omitirán pasos de doctor."
fi

# ======================
# LIST DOCTORS (requires auth)
# ======================
echo "== Get doctors =="
DOCTORS=$(curl -s -X GET "$BASE_URL/api/v1/doctors?page=0&size=50&sort=rating,desc" \
  -H "Authorization: Bearer $TOKEN_PATIENT")
# Buscar el doctor que coincide con EMAIL_DOCTOR; si no existe, toma el primero
DOCTOR_MATCH="false"
DOCTOR_ID=$(printf "%s" "$DOCTORS" | python3 -c 'import json,sys,os
data=json.load(sys.stdin)
email=os.environ.get("EMAIL_DOCTOR","").lower()
content=data.get("content", data)
match=next((d for d in content if str(d.get("email","")).lower()==email), None)
target=match or (content[0] if content else None)
print("" if target is None else target.get("id",""))')
DOCTOR_SELECTED_EMAIL=$(printf "%s" "$DOCTORS" | python3 -c 'import json,sys,os
data=json.load(sys.stdin)
email=os.environ.get("EMAIL_DOCTOR","").lower()
content=data.get("content", data)
match=next((d for d in content if str(d.get("email","")).lower()==email), None)
target=match or (content[0] if content else None)
print("" if target is None else target.get("email",""))')
if [ -n "$DOCTOR_SELECTED_EMAIL" ] && [ "$(echo "$DOCTOR_SELECTED_EMAIL" | tr '[:upper:]' '[:lower:]')" = "$(echo "$EMAIL_DOCTOR" | tr '[:upper:]' '[:lower:]')" ]; then
  DOCTOR_MATCH="true"
fi
if [ -z "$DOCTOR_ID" ]; then
  echo "ERROR: No se pudo obtener DOCTOR_ID"
  exit 1
fi
echo "DOCTOR_ID=$DOCTOR_ID (match=$DOCTOR_MATCH, email=$DOCTOR_SELECTED_EMAIL)"

# ======================
# AVAILABILITY
# ======================
echo "== Add availability (doctor) =="
if [ -n "$TOKEN_DOCTOR" ]; then
  curl -s -X POST "$BASE_URL/api/v1/doctors/availability" \
    -H "Authorization: Bearer $TOKEN_DOCTOR" \
    -H "Content-Type: application/json" \
    -d '{
      "dayOfWeek":2,
      "startTime":"09:00",
      "endTime":"13:00"
    }' || true
else
  echo "SKIP: sin TOKEN_DOCTOR"
fi

echo "== Get availability (doctor) =="
if [ -n "$TOKEN_DOCTOR" ]; then
  curl -s -X GET "$BASE_URL/api/v1/doctors/availability/me" \
    -H "Authorization: Bearer $TOKEN_DOCTOR"
else
  echo "SKIP: sin TOKEN_DOCTOR"
fi

echo "== Get slots by date (private) =="
SLOT_START=""
DATE_FOUND="$DATE"
tries=0
while [ $tries -lt $MAX_DATE_TRIES ]; do
  SLOTS_JSON=$(curl -s -X GET "$BASE_URL/api/v1/doctors/$DOCTOR_ID/availability?date=$DATE_FOUND" \
    -H "Authorization: Bearer $TOKEN_PATIENT")
  echo "Slots $DATE_FOUND: $SLOTS_JSON"

  SLOT_START=$(printf "%s" "$SLOTS_JSON" | python3 -c 'import json,sys; data=json.load(sys.stdin); slot=next((s for s in data if s.get("available") is True), None);
print("" if slot is None else slot["startTime"])')

  if [ -n "$SLOT_START" ]; then
    echo "Usando slot startTime=$SLOT_START en fecha $DATE_FOUND"
    break
  fi

  DATE_FOUND=$(python3 -c 'import sys,datetime as d; dt=d.date.fromisoformat(sys.argv[1]); print((dt+d.timedelta(days=1)).isoformat())' "$DATE_FOUND")
  tries=$((tries+1))
done

if [ -z "$SLOT_START" ]; then
  echo "ERROR: No hay slots disponibles en los próximos $MAX_DATE_TRIES días desde $DATE"
  exit 1
fi

# ======================
# APPOINTMENT
# ======================
echo "== Create appointment =="
APPT=$(curl -s -X POST "$BASE_URL/api/v1/appointments" \
  -H "Authorization: Bearer $TOKEN_PATIENT" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId":"'"$DOCTOR_ID"'",
    "appointmentDate":"'"$DATE_FOUND"'",
    "startTime":"'"$SLOT_START"'",
    "notes":"Primera consulta"
  }')
if [ -z "$APPT" ]; then
  echo "ERROR: Create appointment devolvió respuesta vacía"
  exit 1
fi
echo "Create appointment response: $APPT"
APPOINTMENT_ID=$(printf "%s" "$APPT" | json_get appointmentId 2>/dev/null || true)
if [ -z "$APPOINTMENT_ID" ]; then
  echo "ERROR: No se pudo obtener appointmentId"
  exit 1
fi
echo "APPOINTMENT_ID=$APPOINTMENT_ID"

echo "== Patient appointments (paged) =="
curl -s -X GET "$BASE_URL/api/v1/appointments/me?page=0&size=10&sort=appointmentDate,desc" \
  -H "Authorization: Bearer $TOKEN_PATIENT"

echo "== Doctor daily appointments (paged) =="
if [ "$DOCTOR_MATCH" = "true" ] && [ -n "$TOKEN_DOCTOR" ]; then
  curl -s -X GET "$BASE_URL/api/v1/appointments/doctor/daily?date=$DATE_FOUND&page=0&size=10&sort=startTime,asc" \
    -H "Authorization: Bearer $TOKEN_DOCTOR"
else
  echo "SKIP: login doctor no coincide o sin token ($DOCTOR_SELECTED_EMAIL)"
fi

# ======================
# PAYMENTS
# ======================
echo "== Create payment intent =="
curl -s -X POST "$BASE_URL/api/v1/payments/intent" \
  -H "Authorization: Bearer $TOKEN_PATIENT" \
  -H "Content-Type: application/json" \
  -d '{"appointmentId":"'"$APPOINTMENT_ID"'"}'

echo "== Confirm payment =="
curl -s -X POST "$BASE_URL/api/v1/payments/confirm" \
  -H "Authorization: Bearer $TOKEN_PATIENT" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId":"'"$APPOINTMENT_ID"'",
    "amount":500,
    "paymentMethod":"TDC",
    "paymentProvider":"Stripe",
    "providerPaymentId":"pi_fake_123"
  }'

# ======================
# STATUS UPDATE (POST-PAGO)
# ======================
echo "== Update appointment status to COMPLETED =="
curl -s -X PATCH "$BASE_URL/api/v1/appointments/$APPOINTMENT_ID/status" \
  -H "Authorization: Bearer $TOKEN_PATIENT" \
  -H "Content-Type: application/json" \
  -d '{"status":"completed"}'

# ======================
# REVIEW
# ======================
echo "== Submit review (appointment must be COMPLETED to work) =="
curl -s -X POST "$BASE_URL/api/v1/reviews" \
  -H "Authorization: Bearer $TOKEN_PATIENT" \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId":"'"$APPOINTMENT_ID"'",
    "rating":5,
    "comment":"Excelente atencion"
  }' || true

# ======================
# NOTIFICATIONS
# ======================
echo "== Get notifications (paged) =="
NOTIFS=$(curl -s -X GET "$BASE_URL/api/v1/notifications/me?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer $TOKEN_PATIENT")
echo "$NOTIFS"

echo "== Register device token =="
curl -s -X POST "$BASE_URL/api/v1/notifications/device-token" \
  -H "Authorization: Bearer $TOKEN_PATIENT" \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken":"'"$FCM_TOKEN"'",
    "deviceInfo":"'"$DEVICE_INFO"'"
  }'

echo "== Remove device token =="
curl -s -X DELETE "$BASE_URL/api/v1/notifications/device-token?fcmToken=$FCM_TOKEN" \
  -H "Authorization: Bearer $TOKEN_PATIENT"

echo "== DONE =="
