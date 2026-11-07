#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v docker >/dev/null || { echo 'Docker required' >&2; exit 1; }
docker info >/dev/null
if [ ! -f .env ]; then cp .env.example .env; fi
if ! grep -Eq '^INTERNAL_API_KEY=.{24,}$' .env; then
  sed -i '/^INTERNAL_API_KEY=/d' .env
  echo "INTERNAL_API_KEY=$(openssl rand -hex 32)" >> .env
fi
docker compose up --remove-orphans --build -d --wait --wait-timeout 300
printf 'Open http://localhost:18081\n'
