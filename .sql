-- ================================
-- EXTENSIONS
-- ================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "btree_gist"; -- Requerido para evitar solapamientos precisos (rangos de tiempo)

-- ================================
-- TRIGGERS & FUNCTIONS (AUDITORÍA MÁGICA)
-- ================================
-- Esta función actualiza automáticamente el campo 'updated_at' cada vez que se modifica una fila
CREATE OR REPLACE FUNCTION update_modified_column()   
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;   
END;
$$ language 'plpgsql';

-- ================================
-- USERS
-- ================================

CREATE TABLE users(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_PATIENT','ROLE_DOCTOR', 'ROLE_ADMIN')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_users_modtime BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- ================================
-- SPECIALTIES
-- ================================

CREATE TABLE specialties(
    id SERIAL PRIMARY KEY,
    name VARCHAR(120) UNIQUE NOT NULL
);

-- ================================
-- PATIENT PROFILE
-- ================================

CREATE TABLE patient_profile(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    birth_date DATE,
    medical_history TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_patient_profile_modtime BEFORE UPDATE ON patient_profile FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- ================================
-- DOCTOR PROFILE
-- ================================

CREATE TABLE doctor_profile(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    specialty_id INT REFERENCES specialties(id),
    experience_years INT,
    consultation_price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    slot_duration_minutes INT DEFAULT 30, -- Duración de cada cita para este doctor
    buffer_time_minutes INT DEFAULT 0,    -- Tiempo de descanso entre pacientes
    location TEXT,
    bio TEXT,
    photo_url TEXT,
    rating NUMERIC(2,1) DEFAULT 0 CHECK (rating >= 0 AND rating <= 5),
    total_reviews INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_doctor_profile_modtime BEFORE UPDATE ON doctor_profile FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- ================================
-- DOCTOR AVAILABILITY
-- ================================

CREATE TABLE doctor_availability(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctor_profile(id) ON DELETE CASCADE,
    day_of_week INT CHECK(day_of_week BETWEEN 0 AND 6), -- 0=Domingo, 6=Sábado
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT check_end_time_after_start_time CHECK (end_time > start_time)
);

-- ================================
-- BLOCKED DATES (Vacaciones, Enfermedad)
-- ================================

CREATE TABLE doctor_blocked_dates(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctor_profile(id) ON DELETE CASCADE,
    blocked_date DATE NOT NULL,
    reason TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ================================
-- APPOINTMENTS (THE CORE ENGINE)
-- ================================

CREATE TABLE appointments(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctor_profile(id) NOT NULL,
    patient_id UUID REFERENCES patient_profile(id) NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(25) DEFAULT 'pending_payment' CHECK (
        status IN ('pending_payment', 'confirmed', 'completed', 'cancelled', 'no_show')
    ),
    notes TEXT,
    cancelled_by UUID REFERENCES users(id), -- Auditoría: Quién la canceló
    cancellation_reason TEXT,               -- Auditoría: Por qué
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT check_appointment_time CHECK (end_time > start_time)
);

CREATE TRIGGER update_appointments_modtime BEFORE UPDATE ON appointments FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- UBER LEVEL PROTECTION: PREVENIR SOLAPAMIENTOS PERFECTOS
-- Usamos btree_gist para asegurar matemáticamente que ninguna cita activa choque 
-- con otra del mismo doctor el mismo día.

-- PostgreSQL no tiene 'timerange' nativamente habilitado, hay que declararlo:
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'timerange') THEN
        CREATE TYPE timerange AS RANGE (subtype = time);
    END IF;
END $$;

ALTER TABLE appointments 
ADD CONSTRAINT prevent_schedule_overlap 
EXCLUDE USING gist (
   doctor_id WITH =, 
   appointment_date WITH =, 
   timerange(start_time, end_time) WITH &&
) WHERE (status NOT IN ('cancelled', 'no_show', 'rejected'));


-- ================================
-- PAYMENTS
-- ================================

CREATE TABLE payments(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id UUID REFERENCES appointments(id) ON DELETE SET NULL,
    patient_id UUID REFERENCES patient_profile(id),
    doctor_id UUID REFERENCES doctor_profile(id),
    amount NUMERIC(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'MXN',
    payment_method VARCHAR(50),
    payment_provider VARCHAR(50), -- Stripe, PayPal, MercadoPago
    provider_payment_id TEXT,
    platform_fee NUMERIC(10,2),
    status VARCHAR(20) DEFAULT 'pending' CHECK (
        status IN ('pending', 'processing', 'completed', 'failed', 'refunded')
    ),
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_payments_modtime BEFORE UPDATE ON payments FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- ================================
-- PAYMENT METHODS (Tarjetas Guardadas)
-- ================================

CREATE TABLE payment_methods(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID REFERENCES patient_profile(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_customer_id TEXT NOT NULL,
    card_last4 VARCHAR(4),
    brand VARCHAR(20),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ================================
-- REFUNDS (Reembolsos)
-- ================================

CREATE TABLE refunds(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID REFERENCES payments(id),
    reason TEXT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'processed' CHECK (status IN ('pending', 'processed', 'failed')),
    refunded_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ================================
-- REVIEWS (Reseñas)
-- ================================

CREATE TABLE reviews(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctor_profile(id) ON DELETE CASCADE,
    patient_id UUID REFERENCES patient_profile(id) ON DELETE SET NULL,
    appointment_id UUID REFERENCES appointments(id), -- Vincular reseña a visita específica
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Evitar que un paciente deje 2 reseñas para la misma cita
CREATE UNIQUE INDEX unique_appointment_review ON reviews(appointment_id);

-- ================================
-- CHAT CONVERSATIONS
-- ================================

CREATE TABLE conversations(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctor_profile(id) ON DELETE CASCADE,
    patient_id UUID REFERENCES patient_profile(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_conversations_modtime BEFORE UPDATE ON conversations FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- Solo 1 chat activo por par Dr-Paciente
CREATE UNIQUE INDEX unique_doctor_patient_conversation ON conversations(doctor_id, patient_id);

CREATE TABLE messages(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMPTZ DEFAULT NOW()
);

-- ================================
-- NOTIFICATIONS
-- ================================

CREATE TABLE notifications(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50), -- ej. 'appointment_reminder', 'payment_success'
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ================================
-- DEVICE TOKENS (Push Notifications)
-- ================================

CREATE TABLE device_tokens(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    device_type VARCHAR(20), -- 'ios', 'android', 'web'
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ================================
-- FILE STORAGE (Expedientes, fotos)
-- ================================

CREATE TABLE files(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    file_url TEXT NOT NULL,
    file_type VARCHAR(50),
    file_name VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ================================
-- INDEXES PERFORMANCE (Consultas optimizadas)
-- ================================

CREATE INDEX idx_appointments_doctor_date ON appointments(doctor_id, appointment_date);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id) WHERE is_read = FALSE;
CREATE INDEX idx_doctor_availability_doctor ON doctor_availability(doctor_id) WHERE is_active = TRUE;