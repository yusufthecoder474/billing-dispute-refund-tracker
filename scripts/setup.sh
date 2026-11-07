#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v docker >/dev/null || { echo "Install Docker Engine with Compose first."; exit 1; }
docker info >/dev/null
if [ ! -f .env ]; then cp .env.example .env; fi
docker compose up --build -d --wait --wait-timeout 240
echo "Open http://localhost:8080 (or APP_PORT from .env)."
