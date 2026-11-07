#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -B -ntp -f dispute-service/pom.xml clean verify
mvn -B -ntp -f reporting-service/pom.xml clean verify
(cd frontend && npm ci && npm run typecheck && npm run build && npm test)
./scripts/setup.sh
python3 scripts/smoke-two-services.py
(cd e2e && npm ci && npx playwright install chromium && BASE_URL=http://127.0.0.1:18081 npm test)
python3 scripts/coverage-summary.py
