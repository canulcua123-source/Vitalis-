#!/usr/bin/env bash
set -euo pipefail

# Carga variables locales si existen
if [ -f .env.local ]; then
  export $(grep -v '^#' .env.local | xargs)
fi

DB_NAME=${DB_NAME:-clinica_db}
DB_USER=${DB_USER:-clinica_admin}
DB_PASSWORD=${DB_PASSWORD:-super_secret_password}

ADMIN_EMAIL=${ADMIN_EMAIL:-admin@clinica.com}
ADMIN_PASS=${ADMIN_PASS:-Admin123!}
DOCTOR_EMAIL=${DOCTOR_EMAIL:-doctor@test.com}
DOCTOR_PASS=${DOCTOR_PASS:-Doctor123!}
DOCTOR_FIRST=${DOCTOR_FIRST:-Ana}
DOCTOR_LAST=${DOCTOR_LAST:-Lopez}
DOCTOR_SPECIALTY=${DOCTOR_SPECIALTY:-Cardiologia}
DOCTOR_PRICE=${DOCTOR_PRICE:-500}
AVAIL_DAY=${AVAIL_DAY:-2}
AVAIL_START=${AVAIL_START:-09:00}
AVAIL_END=${AVAIL_END:-13:00}

# Inserta admin y doctor con bcrypt usando pgcrypto
SQL=$(cat <<SQL
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Admin user
INSERT INTO users (email, password, role, is_active)
VALUES (
  '${ADMIN_EMAIL}',
  crypt('${ADMIN_PASS}', gen_salt('bf')),
  'ROLE_ADMIN',
  TRUE
)
ON CONFLICT (email) DO NOTHING;

-- Doctor user
INSERT INTO users (email, password, role, is_active)
VALUES (
  '${DOCTOR_EMAIL}',
  crypt('${DOCTOR_PASS}', gen_salt('bf')),
  'ROLE_DOCTOR',
  TRUE
)
ON CONFLICT (email) DO NOTHING;

-- Specialty
INSERT INTO specialties (name)
VALUES ('${DOCTOR_SPECIALTY}')
ON CONFLICT (name) DO NOTHING;

-- Doctor profile
INSERT INTO doctor_profile (user_id, specialty_id, first_name, last_name, consultation_price)
SELECT u.id, s.id, '${DOCTOR_FIRST}', '${DOCTOR_LAST}', ${DOCTOR_PRICE}
FROM users u
JOIN specialties s ON s.name='${DOCTOR_SPECIALTY}'
WHERE u.email='${DOCTOR_EMAIL}'
ON CONFLICT (user_id) DO NOTHING;

-- Availability (solo si no existe un bloque igual)
INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time, is_active)
SELECT d.id, ${AVAIL_DAY}, '${AVAIL_START}'::time, '${AVAIL_END}'::time, TRUE
FROM doctor_profile d
JOIN users u ON u.id = d.user_id
WHERE u.email='${DOCTOR_EMAIL}'
  AND NOT EXISTS (
    SELECT 1 FROM doctor_availability da
    WHERE da.doctor_id = d.id
      AND da.day_of_week = ${AVAIL_DAY}
      AND da.start_time = '${AVAIL_START}'::time
      AND da.end_time = '${AVAIL_END}'::time
  );
SQL
)

# Ejecutar dentro del contenedor postgres
if ! docker ps --format '{{.Names}}' | grep -q '^clinica-api-db$'; then
  echo "ERROR: contenedor clinica-api-db no está corriendo"
  exit 1
fi

docker exec -e PGPASSWORD="$DB_PASSWORD" -i clinica-api-db \
  psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 <<EOSQL
$SQL
EOSQL

echo "Listo. Admin: $ADMIN_EMAIL / $ADMIN_PASS | Doctor: $DOCTOR_EMAIL / $DOCTOR_PASS"
