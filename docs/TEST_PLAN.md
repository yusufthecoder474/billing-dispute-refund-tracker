# Verification plan

Planning targets were at least 20 backend unit cases, 15 PostgreSQL integration cases, 6 frontend component tests and 5 end-to-end scenarios. Coverage target is at least 75% backend LINE coverage, not instruction coverage. These targets are not passing-test claims; observed counts are in TEST_RESULTS.md.

| Layer | Cases and purpose | Command from repository root |
|---|---|---|
| Unit | Refund bounds, rejection zero, roles, assignments, stale versions, allowed state, SLA boundaries, Monday validation, CSV prefixes/escaping. | `mvn -f backend/pom.xml test` |
| Integration | Real PostgreSQL migrations, persistence, scoped lists, ownership, roles, workflow, duplicate decisions, competing managers, forced audit failure rollback, session login/logout, CSRF, SQL fixture, Swagger. | `mvn -f backend/pom.xml verify` |
| Frontend | Login, login failure, description validation, money payload, empty queue, loading/network error, pagination and stale-action feedback. | `cd frontend` then `npm ci` and `npm test` |
| Browser | Customer submission, assigned support review, manager approval/audit/CSV, access control and two manager sessions. | `cd e2e` then `npm ci`, `npx playwright install chromium`, `npm test` |

Failsafe runs *IT tests during verify. The default integration route starts `postgres:16.4-alpine` using Testcontainers and requires a working Docker daemon. It does not silently skip tests when Docker is unavailable.

An explicit alternative accepts `TEST_DB_URL`, `TEST_DB_USER`, `TEST_DB_PASSWORD` for a disposable real PostgreSQL database. This route executes the same integration assertions but bypasses container provisioning; label it PostgreSQL external mode, not Testcontainers passed. Never point it at a valuable database: tests truncate disputes, decisions and audit events and inject a temporary rollback-test trigger. The `test` profile seeds accounts.

Run `mvn -f backend/pom.xml clean verify` to generate fresh JaCoCo XML/HTML at backend/target/site/jacoco. The top-level LINE counter in jacoco.xml is authoritative for line coverage. Both unit and integration runs contribute to the same session file. The target is reported, not enforced as a gate that masks other test results.

Concurrency sample: two managers start from version 3, synchronize on a barrier, issue independent MockMvc requests backed by real transactions, then assert statuses [200,409], exactly one decision, one final audit and the final state. The browser/API suite repeats the race using independent HTTP sessions. This proves the tested two-request invariant, not throughput or large-scale concurrency.

Rollback test installs a temporary PostgreSQL trigger that fails final audit insertion, then asserts decision absence and unchanged pending state. Cleanup drops the trigger even when assertions fail. No integration test substitutes an in-memory database or mocked repository.
