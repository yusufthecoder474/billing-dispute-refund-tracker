# Build the project in ten checkpoints

The completed repository is the reference. To learn by rebuilding, create a separate Git directory and work through these checkpoints in order. Each prompt below is self-contained about its task and names its prerequisite implementation. Copy complete reference files when needed; never replace an implemented method with a stub. Use the delivered file manifest to find supporting types.

Before stage 1, install JDK 21 (including javac), Maven 3.9+, Node 22.12+, Git and Docker with Compose. Run `git init` in the new folder. Before stage 2, copy .env.example to .env (PowerShell `Copy-Item`, Linux `cp`). Commands below run from the new repository root unless a `cd` is shown; open a fresh root terminal for each stage. Stage 3 onward native startup uses the profile/password commands in DEPLOYMENT.md.

## 1. Initialize repository and Spring Boot project

Objective: Create a compiling Java 21 baseline and document the six-feature scope.

Files: backend/pom.xml; backend/src/main/java/dev/billing/BillingApplication.java; backend/src/main/java/dev/billing/config/AppConfig.java; backend/src/main/resources/application.yml; .gitignore; .editorconfig; README.md.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Create a Maven Spring Boot 3.5.7 Java 21 application under backend, package dev.billing. Include Web, Data JPA, Security, Validation, PostgreSQL runtime, Flyway core/PostgreSQL, springdoc 2.8.14, JUnit/Spring Boot Test, Spring Security Test, Testcontainers PostgreSQL/JUnit and JaCoCo 0.8.13. Use the reference pom versions. Create BillingApplication and UTC Clock configuration. Configure environment-based datasource and Hibernate validate, never update. At this stage verify compilation, not database startup. Produce complete files and a README explaining the six-feature scope.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
mvn -f backend/pom.xml -DskipTests package
```

Expected result: Java source compiles and a Spring Boot JAR is produced. Starting without a database is not expected yet.

Acceptance: No missing imports, placeholders or unapproved frameworks. Maven uses JDK 21.

Common errors: A JRE instead of JDK, network/proxy failure or wrong JAVA_HOME.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Initialize Java 21 application and project scope"`.

## 2. Create PostgreSQL schema and migrations

Objective: Create the relational model and authoritative Flyway schema.

Files: backend/src/main/resources/db/migration/V1__initial_schema.sql; backend/src/main/java/dev/billing/domain/*; backend/src/main/java/dev/billing/repository/*; compose.yaml; .env.example.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. In the Java 21 Spring Boot 3.5.7 billing tracker, implement app_users, disputes, dispute_decisions and audit_events in Flyway SQL. Use NUMERIC(12,2), TIMESTAMPTZ, foreign keys, unique final dispute decision, enum checks and deadline/resolution integrity. Add the documented indexes. Create all Role/Status/Category/Outcome enums and JPA entities including @Version. Create repositories including read-and-insert-only AuditRepository. Provide a PostgreSQL 16.4 Compose database with health check and named volume; keep the app service build for stage 10. Keep ddl-auto validate. No real records.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
docker compose up -d db
mvn -f backend/pom.xml -DskipTests package
```

Expected result: Four tables and all supporting Java types are defined; compilation passes. Validate migrations when starting the authenticated application at stage 3.

Acceptance: No H2 replacement, no schema auto-update, and positive charge/unique decision enforced by SQL.

Common errors: Missing DB_PASSWORD or Docker engine not running.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Add Flyway schema and versioned domain model"`.

## 3. Implement authentication and authorization

Objective: Establish session identity and a tested CSRF flow.

Files: backend/src/main/java/dev/billing/security/SecurityConfig.java; backend/src/main/java/dev/billing/auth/AuthController.java; backend/src/main/java/dev/billing/config/DemoSeed.java; backend/src/main/java/dev/billing/dto/ApiDtos.java; backend/src/main/resources/application-local.yml; backend/src/main/resources/application-test.yml.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Using the completed schema/repositories, implement Spring Security session form login at /api/auth/login and logout at /api/auth/logout. Use BCrypt, HttpOnly SameSite=Lax cookies, Secure default outside local/test, and CSRF for login/logout and all writes. Add GET /api/auth/csrf returning the masked token/header and GET /api/auth/me returning UserView without hash. Create all ApiDtos records needed by later stages now so imports compile. Seed the six fictional customer/support/manager accounts only in local/test. Include health endpoint. Never use an X-Role header. Explain session fixation protection and refreshing CSRF after authentication.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
mvn -f backend/pom.xml -DskipTests package
```

Expected result: Compilation passes; with local profile and database configured, /api/auth/csrf is 200 and anonymous /api/auth/me is 401.

Acceptance: Demo seeds are profile-scoped; password hashes never appear in responses.

Common errors: 403 from missing CSRF before login; app started without local profile.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Implement session authentication and CSRF protection"`.

## 4. Implement dispute creation and listing

Objective: Deliver ownership-scoped creation, history and detail.

Files: backend/src/main/java/dev/billing/service/DisputeService.java; backend/src/main/java/dev/billing/service/Rules.java; backend/src/main/java/dev/billing/controller/DisputeController.java; backend/src/main/java/dev/billing/exception/*.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Extend the secured billing tracker with complete customer create/list/detail methods. Validate category, 10–2000 character description, positive INR amount and precision. Set immutable createdAt and dueAt=createdAt+72 elapsed hours using injected Clock. Append creation audit atomically. Scope customer reads to owner and support reads to assignee; managers read all. Return DTOs and paginated PageView, with validated page/size/sort/direction and deterministic ID tie-breaker. Include all exception classes and helpers required to compile. Do not expose workflow endpoints until their methods are implemented; no TODO bodies. Return 400/401/403/404 correctly.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
mvn -f backend/pom.xml -DskipTests package
```

Expected result: Customer submission persists and appears in that customer list; another customer receives 403 on detail.

Acceptance: No editable owner/timestamp/status input and no unbounded dispute list.

Common errors: Serializing entities rather than DTOs; forgetting transaction or audit insertion.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Add validated customer dispute submission and scoped history"`.

## 5. Implement support assignment and workflow

Objective: Add controlled manager assignment and assigned support actions.

Files: backend/src/main/java/dev/billing/service/DisputeService.java; backend/src/main/java/dev/billing/service/Rules.java; backend/src/main/java/dev/billing/controller/DisputeController.java.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Extend DisputeService and controllers with manager-only assignment of OPEN disputes to valid SUPPORT accounts, the manager support directory, start-review and submit-for-decision. Only the assigned support agent may advance OPEN to IN_REVIEW to PENDING_DECISION. Require a nonnegative client version, compare it and use JPA @Version. Reject stale or unsupported actions with 409. Reassignment is OPEN-only and same-agent repeat is a conflict. Append every successful action in the same transaction. Complete all referenced methods, DTOs and exception mapping; do not add comments or uploads as new business features.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
mvn -f backend/pom.xml -DskipTests package
```

Expected result: Assigned support sees the case; unassigned support cannot read or act; versions advance.

Acceptance: State, role, assignee and version all checked on server.

Common errors: Trusting hidden UI buttons; advancing state before authorization.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Add assigned support workflow and optimistic conflicts"`.

## 6. Implement manager decisions and audit events

Objective: Record exactly one atomic final decision and expose its timeline.

Files: backend/src/main/java/dev/billing/service/DisputeService.java; backend/src/main/java/dev/billing/controller/DisputeController.java; backend/src/main/java/dev/billing/exception/ApiExceptionHandler.java.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Complete final manager decision processing for the billing tracker. Require PENDING_DECISION and matching version, valid outcome and 10–2000 character explanation. Use BigDecimal refund >=0 and <=charge with max two decimals; rejection requires zero. In one @Transactional method update final state/resolvedAt, flush the versioned dispute, insert a unique decision and append one final audit. Map optimistic/unique conflicts to 409. Add authorized paginated chronological audit reads with actor name, previous/new status and detail. No final-case editing and no money transfer. Keep repository audit operations append/read only.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
mvn -f backend/pom.xml -DskipTests package
```

Expected result: Approval/rejection records explainable final outcomes and timeline; repeated final action conflicts.

Acceptance: A failure in decision or audit insertion rolls back the whole transaction.

Common errors: Separate transactions for audit; floating-point money; missing uniqueness constraint.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Add atomic final decisions and audit timeline"`.

## 7. Implement SLA and weekly reporting

Objective: Turn documented UTC cohorts into SQL and CSV.

Files: backend/src/main/java/dev/billing/service/ReportingService.java; backend/src/main/java/dev/billing/controller/ReportingController.java; backend/src/main/java/dev/billing/config/OpenApiConfig.java.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Add manager-only SQL aggregation using JdbcTemplate in this Spring Boot app. SLA: open=unresolved, due-soon=[now,now+12h], overdue=due<now, resolved within=resolvedAt<=dueAt. Capture now once. Weekly input must be a Monday UTC date, start inclusive/end next Monday exclusive. Created uses createdAt; resolved, within, breached, mean elapsed hours and approved-refund sum use resolvedAt cohort. Empty averages/sums return zero; mean hours rounded to two decimals. Add JSON and safely quoted/formula-neutralized CSV. Add Swagger metadata including session/CSRF flow and form login. No external analytics or notification service.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
mvn -f backend/pom.xml -DskipTests package
```

Expected result: Dashboard/report endpoints return consistent DTOs and invalid or missing week is 400.

Acceptance: One-statement weekly aggregate; defined timestamps; no real-money implication.

Common errors: Counting unresolved overdue cases in resolved-week breach totals; incorrect inclusive end.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Add UTC SLA summary and weekly CSV reporting"`.

## 8. Build the React frontend

Objective: Connect every page to the secured backend.

Files: frontend/package.json; frontend/package-lock.json; frontend/tsconfig.json; frontend/vite.config.ts; frontend/index.html; frontend/src/*.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Build the small strict TypeScript React/Vite frontend for the completed billing API. Use the reference pinned dependency versions and generate package-lock with npm. Implement login, customer list/create, support queue, shared detail/timeline, manager assignment/decision and SLA/weekly CSV views. Use native fetch with same-origin cookies, CSRF retrieval and refresh after login/logout. Handle loading, empty lists, validation, 401, 403, 409, network errors, successful actions and pagination. Use accessible labels, keyboard controls and responsive CSS. Preserve amounts as decimal text in submission. Every button must work. Vite proxies /api to localhost:8080.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
cd frontend
npm ci
npm run typecheck
npm run build
```

Expected result: Type checking and Vite build pass; connected roles can complete the workflow.

Acceptance: No fake API responses, client-side-only authorization or decorative controls.

Common errors: Wrong proxy port, missing token, number precision or stale version handling.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Build accessible React workflow and manager dashboard"`.

## 9. Add tests, coverage and GitHub Actions

Objective: Create reproducible evidence rather than claimed counts.

Files: backend/src/test/*; frontend/src/test/*; e2e/*; evaluation/*; .github/workflows/ci.yml; .github/PULL_REQUEST_TEMPLATE.md.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Add meaningful JUnit rules tests (target >=20), real PostgreSQL Testcontainers integration tests (>=15), React component tests (>=6) and Playwright scenarios (>=5). Include role/ownership, CSRF login/logout, stale and double decisions, two concurrent managers, rollback after failed audit insertion, UTC SLA boundaries and independent weekly SQL fixtures. Default integration requires Docker; explicit TEST_DB_URL mode may use a disposable real PostgreSQL and must be labeled separately. Never silently skip Docker failures. Configure JaCoCo LINE reporting, fixed fixture outputs and official GitHub Actions with Java21, Node22, npm ci, Docker, report artifacts and visible failures. Generate actual lockfiles.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
mvn -f backend/pom.xml clean verify
cd frontend
npm ci
npm test
```

Expected result: Tests execute and coverage HTML/XML is generated; record actual failures/skips.

Acceptance: No mocked test described as PostgreSQL; no invented coverage or CI success.

Common errors: Docker unavailable, missing browsers or interpreting instruction coverage as line coverage.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Add automated integrity tests and CI evidence"`.

## 10. Final verification, documentation and Docker packaging

Objective: Produce a reproducible local demonstration and honest archive.

Files: Dockerfile; compose.yaml; scripts/*; README.md; LICENSE; CHANGELOG.md; CONTRIBUTING.md; SECURITY.md; docs/*; Billing-Dispute-Refund-Tracker-Files.md.

Standalone coding-assistant prompt:

```text
You are implementing Billing Dispute & Refund Tracker, an educational Java 21 / Spring Boot 3 / PostgreSQL / React TypeScript modular monolith with exactly six features: submission/tracking, support assignment, controlled decisions, SLA summary, audit timeline and weekly reports. Use synthetic data and no payment gateways. Finish the billing tracker without adding business scope. Build React in a Node stage, copy assets into the Maven Java21 build and run the JAR as an unprivileged user. Compose must run only app and PostgreSQL with persistent volume, database and app health checks, local profile, env config and localhost bindings. Create Windows11 PowerShell/Ubuntu setup and verification scripts with checked exit codes. Complete all requested docs, a ten-step learning guide, three-minute demo, verified research and honest resume evidence. Run available checks, capture only real screenshots, report PASSED/FAILED/NOT EXECUTED, include actual line coverage and package source/lockfiles/docs/evidence in the named ZIP and manifest.
Return complete files, required imports and exact commands. Do not claim a check passed without executing it. Preserve completed behavior from earlier checkpoints.
```

Commands:

```sh
docker compose up --build -d --wait --wait-timeout 240
cd e2e
npm ci
npx playwright install chromium
npm test
```

Expected result: Packaged UI loads and browser workflow passes; the archive contains every documented source path.

Acceptance: Manifest matches files; exclusions and unexecuted checks are explicit; no node_modules or secrets included.

Common errors: Forgetting frontend static copy, missing .env or claiming unrun container/CI checks.

Checkpoint (return to repository root first): `git add .` then `git commit -m "Document and package verified local demonstration"`.

After stage 10, run the complete verification script from the repository root: `bash scripts/verify.sh` on Ubuntu or `powershell -ExecutionPolicy Bypass -File scripts/verify.ps1` on Windows. Do not hide failed checks just to reach the next checkpoint.
