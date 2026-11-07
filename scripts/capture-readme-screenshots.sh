#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE_URL="${1:-http://127.0.0.1:18081}"
cd "$ROOT/e2e"
export BASE_URL
printf 'Capturing screenshots from %s; creates one synthetic dispute.\n' "$BASE_URL"
if [ ! -d node_modules ]; then npm ci; fi
npx playwright install chromium
npx playwright test --config playwright.screenshots.config.ts
printf 'Saved screenshots to %s/docs/screenshots\n' "$ROOT"
