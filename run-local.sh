#!/usr/bin/env bash
set -euo pipefail
cp .env.local .env
export $(grep -v '^#' .env | xargs)
docker-compose down -v
docker-compose up -d
./mvnw spring-boot:run
