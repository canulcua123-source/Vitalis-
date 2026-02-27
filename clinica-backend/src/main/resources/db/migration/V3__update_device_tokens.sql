-- V3__update_device_tokens.sql

ALTER TABLE device_tokens 
RENAME COLUMN token TO fcm_token;

ALTER TABLE device_tokens 
RENAME COLUMN device_type TO device_info;

ALTER TABLE device_tokens 
ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMPTZ DEFAULT NOW();
