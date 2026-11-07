#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -B -f backend/pom.xml clean verify
(cd frontend && npm ci && npm run typecheck && npm run build && npm test)
docker compose up --build -d --wait --wait-timeout 240
(cd e2e && npm ci && npx playwright install chromium && npm test)
echo "Checks passed. Coverage: backend/target/site/jacoco/index.html"
