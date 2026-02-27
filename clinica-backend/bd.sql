-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.appointments (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  doctor_id uuid NOT NULL,
  patient_id uuid NOT NULL,
  appointment_date date NOT NULL,
  start_time time without time zone NOT NULL,
  end_time time without time zone NOT NULL,
  status character varying DEFAULT 'pending_payment'::character varying CHECK (status::text = ANY (ARRAY['pending_payment'::character varying, 'confirmed'::character varying, 'completed'::character varying, 'cancelled'::character varying, 'no_show'::character varying]::text[])),
  notes text,
  cancelled_by uuid,
  cancellation_reason text,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT appointments_pkey PRIMARY KEY (id),
  CONSTRAINT appointments_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor_profile(id),
  CONSTRAINT appointments_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patient_profile(id),
  CONSTRAINT appointments_cancelled_by_fkey FOREIGN KEY (cancelled_by) REFERENCES public.users(id)
);
CREATE TABLE public.conversations (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  doctor_id uuid,
  patient_id uuid,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT conversations_pkey PRIMARY KEY (id),
  CONSTRAINT conversations_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor_profile(id),
  CONSTRAINT conversations_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patient_profile(id)
);
CREATE TABLE public.device_tokens (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  user_id uuid,
  fcm_token text NOT NULL UNIQUE,
  device_info character varying,
  created_at timestamp with time zone DEFAULT now(),
  last_used_at timestamp with time zone DEFAULT now(),
  CONSTRAINT device_tokens_pkey PRIMARY KEY (id),
  CONSTRAINT device_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.doctor_availability (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  doctor_id uuid,
  day_of_week integer CHECK (day_of_week >= 0 AND day_of_week <= 6),
  start_time time without time zone NOT NULL,
  end_time time without time zone NOT NULL,
  is_active boolean DEFAULT true,
  CONSTRAINT doctor_availability_pkey PRIMARY KEY (id),
  CONSTRAINT doctor_availability_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor_profile(id)
);
CREATE TABLE public.doctor_blocked_dates (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  doctor_id uuid,
  blocked_date date NOT NULL,
  reason text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT doctor_blocked_dates_pkey PRIMARY KEY (id),
  CONSTRAINT doctor_blocked_dates_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor_profile(id)
);
CREATE TABLE public.doctor_profile (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  user_id uuid UNIQUE,
  specialty_id integer,
  experience_years integer,
  consultation_price numeric NOT NULL DEFAULT 0.00,
  slot_duration_minutes integer DEFAULT 30,
  buffer_time_minutes integer DEFAULT 0,
  location text,
  bio text,
  photo_url text,
  rating numeric DEFAULT 0 CHECK (rating >= 0::numeric AND rating <= 5::numeric),
  total_reviews integer DEFAULT 0,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  first_name character varying NOT NULL DEFAULT 'Dr.'::character varying,
  last_name character varying NOT NULL DEFAULT 'Medical'::character varying,
  CONSTRAINT doctor_profile_pkey PRIMARY KEY (id),
  CONSTRAINT doctor_profile_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id),
  CONSTRAINT doctor_profile_specialty_id_fkey FOREIGN KEY (specialty_id) REFERENCES public.specialties(id)
);
CREATE TABLE public.files (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  user_id uuid,
  file_url text NOT NULL,
  file_type character varying,
  file_name character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT files_pkey PRIMARY KEY (id),
  CONSTRAINT files_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.flyway_schema_history (
  installed_rank integer NOT NULL,
  version character varying,
  description character varying NOT NULL,
  type character varying NOT NULL,
  script character varying NOT NULL,
  checksum integer,
  installed_by character varying NOT NULL,
  installed_on timestamp without time zone NOT NULL DEFAULT now(),
  execution_time integer NOT NULL,
  success boolean NOT NULL,
  CONSTRAINT flyway_schema_history_pkey PRIMARY KEY (installed_rank)
);
CREATE TABLE public.messages (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  conversation_id uuid,
  sender_id uuid,
  message text NOT NULL,
  is_read boolean DEFAULT false,
  sent_at timestamp with time zone DEFAULT now(),
  CONSTRAINT messages_pkey PRIMARY KEY (id),
  CONSTRAINT messages_conversation_id_fkey FOREIGN KEY (conversation_id) REFERENCES public.conversations(id),
  CONSTRAINT messages_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id)
);
CREATE TABLE public.notifications (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  user_id uuid,
  title character varying NOT NULL,
  message text NOT NULL,
  type character varying,
  is_read boolean DEFAULT false,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT notifications_pkey PRIMARY KEY (id),
  CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.patient_profile (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  user_id uuid UNIQUE,
  first_name character varying NOT NULL,
  last_name character varying NOT NULL,
  phone character varying,
  birth_date date,
  medical_history text,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT patient_profile_pkey PRIMARY KEY (id),
  CONSTRAINT patient_profile_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.payment_methods (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  patient_id uuid,
  provider character varying NOT NULL,
  provider_customer_id text NOT NULL,
  card_last4 character varying,
  brand character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT payment_methods_pkey PRIMARY KEY (id),
  CONSTRAINT payment_methods_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patient_profile(id)
);
CREATE TABLE public.payments (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  appointment_id uuid,
  patient_id uuid,
  doctor_id uuid,
  amount numeric NOT NULL,
  currency character varying DEFAULT 'MXN'::character varying,
  payment_method character varying,
  payment_provider character varying,
  provider_payment_id text,
  platform_fee numeric,
  status character varying DEFAULT 'pending'::character varying CHECK (status::text = ANY (ARRAY['pending'::character varying, 'processing'::character varying, 'completed'::character varying, 'failed'::character varying, 'refunded'::character varying]::text[])),
  paid_at timestamp with time zone,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT payments_pkey PRIMARY KEY (id),
  CONSTRAINT payments_appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES public.appointments(id),
  CONSTRAINT payments_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patient_profile(id),
  CONSTRAINT payments_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor_profile(id)
);
CREATE TABLE public.refunds (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  payment_id uuid,
  reason text NOT NULL,
  amount numeric NOT NULL,
  status character varying DEFAULT 'processed'::character varying CHECK (status::text = ANY (ARRAY['pending'::character varying, 'processed'::character varying, 'failed'::character varying]::text[])),
  refunded_at timestamp with time zone DEFAULT now(),
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT refunds_pkey PRIMARY KEY (id),
  CONSTRAINT refunds_payment_id_fkey FOREIGN KEY (payment_id) REFERENCES public.payments(id)
);
CREATE TABLE public.reviews (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  doctor_id uuid,
  patient_id uuid,
  appointment_id uuid,
  rating integer NOT NULL CHECK (rating >= 1 AND rating <= 5),
  comment text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT reviews_pkey PRIMARY KEY (id),
  CONSTRAINT reviews_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor_profile(id),
  CONSTRAINT reviews_patient_id_fkey FOREIGN KEY (patient_id) REFERENCES public.patient_profile(id),
  CONSTRAINT reviews_appointment_id_fkey FOREIGN KEY (appointment_id) REFERENCES public.appointments(id)
);
CREATE TABLE public.specialties (
  id integer NOT NULL DEFAULT nextval('specialties_id_seq'::regclass),
  name character varying NOT NULL UNIQUE,
  CONSTRAINT specialties_pkey PRIMARY KEY (id)
);
CREATE TABLE public.users (
  id uuid NOT NULL DEFAULT uuid_generate_v4(),
  email character varying NOT NULL UNIQUE,
  password character varying NOT NULL,
  role character varying NOT NULL CHECK (role::text = ANY (ARRAY['ROLE_PATIENT'::character varying, 'ROLE_DOCTOR'::character varying, 'ROLE_ADMIN'::character varying]::text[])),
  is_active boolean DEFAULT true,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT users_pkey PRIMARY KEY (id)
);