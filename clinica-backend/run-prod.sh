#!/usr/bin/env bash
set -euo pipefail
cp .env.supabase .env
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
