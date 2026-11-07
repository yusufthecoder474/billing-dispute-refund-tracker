## Unreleased — reporting-service extraction (candidate; not yet run end to end)

- Split `backend/` into `dispute-service/` and an independent `reporting-service/` Java 21 Maven module.
- Moved weekly aggregation/CSV from direct JDBC to authenticated paginated REST export.
- Preserved manager-facing routes; introduced explicit 503/502 upstream errors and bounded retries.
- Added targeted Java tests, Compose smoke, two-Dockerfile deployment and two-module CI.
- Recorded trade-offs and test/evidence requirements in `docs/MICROSERVICES.md`.
- **No new test/coverage/startup/CI measurements claimed.**

# Changelog

## 1.0.0 — 2026-11-07
Initial reference implementation: customer disputes, assigned support queue, manager decisions, UTC SLA summary, append-only application audit timeline and weekly CSV reporting. Includes automated checks, container packaging and learning documentation. See docs/TEST_RESULTS.md for execution evidence; this entry is not evidence of a production release.
