<div align="center">

# ðŸ§¾ Billing Dispute & Refund Tracker

**From a disputed charge to a fair, traceable decision.**

A full-stack software engineering project that helps customers report billing issues, gives support staff a clear workflow, and helps managers spot deadlines and record decisions.

**Java 21 Â· Spring Boot Â· PostgreSQL Â· React Â· TypeScript**

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-19-149ECA?logo=react&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

**[See the screenshots](#see-the-working-application) Â· [Run locally](#run-it-locally) Â· [Understand the workflow](#the-dispute-lifecycle) Â· [Read the user manual](docs/USER_MANUAL.md)**

<sub>Independent final-year educational project. Fictional accounts and charges. Refund approvals are simulated: no money is transferred.</sub>

</div>

---

## What problem does it solve?

Imagine you are charged twice for a monthly subscription. You contact support, but your complaint gets buried in messages. You do not know who is handling it, when you will receive an answer, or why a refund was approved or rejected. The manager also struggles to see which complaints are about to miss their deadlines. This application keeps the dispute, the responsible people, the deadline, the final explanation, and a time-stamped history in one place.

> **What it proves:** an explainable full-stack implementation of business rules, access control, database transactions, testing and reporting. **What it does not prove:** measured improvements in a real telecom company or real refund processing.

## See the working application

These are **real screenshots from a locally running Windows/Docker demonstration on 7 November 2026**, shared during the build and verification. The example dispute is fictional and uses INR 1,000 with a simulated INR 750 refund.

| Customer creates a dispute | Manager reviews the completed case |
|:--:|:--:|
| <a href="docs/screenshots/03-customer-dispute-detail.png"><img src="docs/screenshots/03-customer-dispute-detail.png" width="440" alt="Customer dispute number 1, duplicate charge and deadline"></a> | <a href="docs/screenshots/11-resolved-and-audit.png"><img src="docs/screenshots/11-resolved-and-audit.png" width="440" alt="Final refund decision with five audit history entries"></a> |
| **Customer view** â€” amount, deadline and current status | **Final decision** â€” outcome, explanation and audit history |

| Support moves the case forward | Manager tracks deadlines and reports |
|:--:|:--:|
| <a href="docs/screenshots/08-support-in-review.png"><img src="docs/screenshots/08-support-in-review.png" width="440" alt="Support review page with IN_REVIEW status"></a> | <a href="docs/screenshots/13-weekly-report.png"><img src="docs/screenshots/13-weekly-report.png" width="440" alt="Weekly report showing SLA totals and INR 750 simulated refunds"></a> |
| **Support workflow** â€” assigned review with traceability | **Weekly report** â€” resolution time, SLA counts and refund totals |

**[View all 13 step-by-step application screenshots and the full verification gallery](docs/SCREENSHOT_GALLERY.md)**. The gallery also includes screenshots of the Windows Docker build, Java tests, Playwright tests, dependency fixes and Git initialization. No manual screenshot saving is needed; they are already bundled with this documentation upgrade.

## Six features, with a business purpose

| Feature | What a nontechnical user sees | Why an engineer cares |
|---|---|---|
| ðŸ“ **Dispute submission** | A customer reports an incorrect charge and follows its status. | Forms, validation, REST APIs, role-scoped SQL data. |
| ðŸ‘¥ **Assignment and support queue** | A manager gives a case to a support employee. | Ownership checks, filtering and pagination. |
| âœ… **Controlled final decision** | A manager approves or rejects a simulated refund with an explanation. | Transactions, state transitions, data integrity. |
| â° **72-hour deadline tracking** | The dashboard shows open, due-soon, overdue and resolved cases. | Explicit UTC rules and boundary tests. |
| ðŸ§¾ **Audit timeline** | Every action shows who did it and when. | Debugging, traceability and atomic writes. |
| ðŸ“Š **Weekly reporting** | Managers review SLA results and download a CSV. | SQL aggregates, safe exports and documentation. |

## Who does what?

| Person | Can do | Cannot do |
|---|---|---|
| **Customer** | Submit disputes; see only their own cases and decisions. | Assign cases or approve refunds. |
| **Support agent** | See assigned cases; start review; submit for a manager's decision. | Approve a refund or read unrelated customers' cases. |
| **Manager** | See cases; assign support; approve/reject; see reports. | Change a completed final decision. |

## The dispute lifecycle

```mermaid
flowchart TD
    A["ðŸ§¾ Customer submits a dispute"] --> B["OPEN Â· deadline = created + 72 hours"]
    B --> C["ðŸ‘” Manager assigns support"]
    C --> D["ðŸ” Support starts review Â· IN_REVIEW"]
    D --> E["ðŸ“¨ Support requests decision Â· PENDING_DECISION"]
    E --> F{"Manager's decision"}
    F -->|Approve| G["âœ… RESOLVED_APPROVED"]
    F -->|Reject| H["âŒ RESOLVED_REJECTED"]
    G --> I["ðŸ“œ Audit timeline + SLA/report totals"]
    H --> I
    classDef start fill:#DCEFFA,color:#12374A,stroke:#1873A5
    classDef work fill:#E8F5F2,color:#134C49,stroke:#148378
    classDef finish fill:#F2EEE4,color:#4F4230,stroke:#A78B52
    class A,B start
    class C,D,E work
    class F,G,H,I finish
```

**Important:** A refund is an approval record only. The system never contacts a payment gateway. The status must advance in order; users cannot jump straight from OPEN to APPROVED. [Full rules](docs/ARCHITECTURE.md#status-transitions-and-data-integrity).

## Architecture at a glance

```mermaid
flowchart LR
    Person["ðŸ‘¥ Customer / Support / Manager"] --> UI["âš›ï¸ React + TypeScript\nForms and dashboard"]
    UI --> API["â˜• Spring Boot\nValidated REST API + session security"]
    API --> Rules["Workflow services\nTransaction + concurrency checks"]
    Rules --> DB[("ðŸ˜ PostgreSQL\nDisputes, decisions, users, audit")]
    DB --> Report["ðŸ“Š Weekly SQL reporting + CSV"]
    API --> Report
    Flyway["Flyway migrations"] -.-> DB
    classDef frontend fill:#E2F2FA,color:#12435C,stroke:#4B9ABE
    classDef backend fill:#EAF5EE,color:#184836,stroke:#64A87C
    classDef data fill:#F1EAF9,color:#4B2F65,stroke:#9572BB
    class UI frontend
    class API,Rules,Report backend
    class DB,Flyway data
```

This is a **modular monolith**: one backend and one database, with a small React user interface. There is no Kafka, Kubernetes, AI or external payment service. For this project, that keeps the code understandable and makes changes easier to test.

[Architecture, workflow and sequence diagrams](docs/ARCHITECTURE.md) Â· [Database schema and indexes](docs/DATABASE_DESIGN.md) Â· [API documentation](docs/API.md)

## Tech stack, grouped by purpose

| Layer | Tools | Why used |
|---|---|---|
| **Frontend** | React 19, TypeScript, HTML5, CSS3, Vite | Accessible forms and role-specific pages. |
| **Backend** | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, Bean Validation | Business rules, authentication and transactional APIs. |
| **Data** | PostgreSQL 16, Flyway, SQL, `BigDecimal` | Durable records, schema migrations and money-safe calculations. |
| **Backend tests** | JUnit 5, Spring Boot Test, PostgreSQL integration tests, JaCoCo | Validate decisions, permissions, rollback and coverage. |
| **Frontend & browser tests** | React Testing Library, Vitest, Playwright | Verify forms, navigation and complete workflows. |
| **Development & delivery** | Git, GitHub Actions, Maven, npm, Docker Compose | Version control, repeatable builds and local deployment. |
| **Documentation** | Markdown, OpenAPI/Swagger UI, Mermaid | Explain APIs, workflows, setup and troubleshooting. |

**Dependency note:** the first ZIP contained Vite 7.1.12 and Vitest 3.2.4. On **7 November 2026**, the local frontend was upgraded to **Vite 7.3.7 and Vitest 4.1.12**; that installation reported **0 npm vulnerabilities** and subsequently passed its build and eight component tests. This README upgrade does **not** replace your locally updated `frontend/package.json` or lockfile.

## Database: four key tables

```mermaid
erDiagram
  APP_USERS ||--o{ DISPUTES : submits
  APP_USERS o|--o{ DISPUTES : handles
  APP_USERS ||--o{ DISPUTE_DECISIONS : decides
  DISPUTES ||--o| DISPUTE_DECISIONS : has
  DISPUTES ||--o{ AUDIT_EVENTS : records
  APP_USERS ||--o{ AUDIT_EVENTS : performs
  APP_USERS {
    bigint id PK
    string email UK
    string role
  }
  DISPUTES {
    bigint id PK
    bigint customer_id FK
    bigint assigned_support_id FK
    decimal disputed_amount
    string status
    bigint version
    datetime due_at
  }
  DISPUTE_DECISIONS {
    bigint id PK
    bigint dispute_id FK
    bigint manager_id FK
    decimal approved_refund_amount
    string outcome
  }
  AUDIT_EVENTS {
    bigint id PK
    bigint dispute_id FK
    bigint actor_id FK
    string action
    datetime created_at
  }
```

A **unique decision per dispute** and **optimistic locking** (`@Version`) help prevent two managers from successfully recording conflicting final decisions. The final status, decision and audit write commit together. [Read the concurrency explanation](docs/ARCHITECTURE.md#how-two-managers-are-handled).

## Run it locally

### Start with Docker (recommended)

Prerequisite: Docker Desktop running with Linux containers. For the original project files, open **PowerShell at the repository root** and run:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
# These host ports avoid conflicts with other local projects (for example EventPulse-SRE).
(Get-Content .env) -replace '^DB_PORT=.*$', 'DB_PORT=55433' -replace '^APP_PORT=.*$', 'APP_PORT=18081' | Set-Content .env
docker compose up --build -d --wait --wait-timeout 240
docker compose ps
Start-Process http://localhost:18081
```

Expected: the `app` and `db` services both show **healthy**. Your Windows demonstration succeeded with **application port 18081** and **database host port 55433**. The normal `.env.example` defaults may use 8080/5432; change those if the ports are free. **Never run `docker compose down -v` casually: it deletes the project's database volume.**

Linux/macOS shell:

```sh
cp -n .env.example .env  # only if not already present
# Edit APP_PORT and DB_PORT in .env if another project occupies them.
docker compose up --build -d --wait --wait-timeout 240
# Open http://localhost:8080, or your chosen APP_PORT.
```

### Local development tools

Java 21 JDK, Maven 3.9+, Node.js compatible with the pinned frontend packages and npm are needed if you run tests outside Docker. The included `scripts/setup.ps1` and `scripts/setup.sh` provide project setup instructions. [Windows troubleshooting](docs/TROUBLESHOOTING.md).

### Local demonstration accounts

| Role | Email | Password |
|---|---|---|
| Customer | `customer@demo.local` | `DemoPass123!` |
| Support | `support@demo.local` | `DemoPass123!` |
| Manager | `manager@demo.local` | `DemoPass123!` |

Accounts are seeded in the **local demonstration profile only**. These credentials must never be used for public hosting, and the project is not a production payment system.

## Tests and verified evidence

| Check | Evidence available | Status |
|---|---|---|
| Spring Boot / Maven build | Windows Maven `BUILD SUCCESS` screenshot | **Passed locally** |
| Java unit tests | Archived JUnit report: 37 | **Passed in archived run** |
| PostgreSQL integration tests | Windows Maven output: 30 tests, zero failures/errors/skips | **Passed locally** |
| React TypeScript + Vite build | Windows build after dependency update | **Passed locally** |
| React component tests | Windows Vitest 4.1.12: 8/8 | **Passed locally** |
| Playwright browser/API suite | Windows Chromium: 5/5 | **Passed locally** |
| Backend JaCoCo **line** coverage | Archived report: **406/420 = 96.67%** | **Recorded in original run** |
| Docker Compose deployment | Windows `app` and `db` healthy | **Passed locally** |
| Hosted GitHub Actions | Build and verify workflow passed on 7 November 2026 | **Passed** |

The **80 test cases** (37 + 30 + 8 + 5) reflect the archived and Windows evidence described above, not one single fresh command. The 96.67% figure is **line coverage**, not branch, instruction or production-quality certification. Tests use synthetic data. [Screenshots and exact context](docs/LOCAL_VERIFICATION.md).

To rerun the Java checks without Testcontainers auto-discovery on your Windows setup, use a **disposable separate** PostgreSQL test database (details in [troubleshooting](docs/TROUBLESHOOTING.md#testcontainers-cannot-find-docker-on-windows)). Do not run destructive integration tests against your app DB on port `55433`.

To rerun the frontend tests:

```powershell
Set-Location frontend
npm ci
npm run build
npm test
Set-Location ..
```

To rerun Playwright against an already running demo app:

```powershell
Set-Location e2e
npm ci
npx playwright install chromium
$env:BASE_URL = 'http://127.0.0.1:18081'
npm test
Set-Location ..
```

**Playwright tests create synthetic disputes.** They may change demonstration dashboard totals; they do not need your Dispute #1 to be deleted. To create fresh marketing screenshots in a later run, see [screenshot automation](docs/SCREENSHOT_GALLERY.md#regenerate-the-app-screenshots).

## Selected REST APIs

| Method and path | Purpose |
|---|---|
| `POST /api/disputes` | Submit a billing dispute. |
| `GET /api/disputes` | List permitted disputes with pagination. |
| `POST /api/disputes/{id}/assign` | Manager assigns support. |
| `POST /api/disputes/{id}/start-review` | Support begins review. |
| `POST /api/disputes/{id}/submit-for-decision` | Support sends case to manager. |
| `POST /api/disputes/{id}/decision` | Manager records final simulated decision. |
| `GET /api/disputes/{id}/audit` | Read committed history. |
| `GET /api/sla/summary` | Manager's SLA dashboard. |
| `GET /api/reports/weekly.csv` | Manager's downloadable report. |

OpenAPI/Swagger UI is available in the local application. [Complete endpoint and permission guide](docs/API.md).

## Repository layout

```text
billing-dispute-refund-tracker/
â”œâ”€â”€ backend/                   Java 21 Spring Boot, Flyway, JUnit
â”œâ”€â”€ frontend/                  React, TypeScript, Vite, Vitest
â”œâ”€â”€ e2e/                       Playwright end-to-end scenarios
â”œâ”€â”€ scripts/                   Windows and Linux setup/verification
â”œâ”€â”€ docs/                      Architecture, API, manual, research, screenshots
â”‚   â”œâ”€â”€ screenshots/           Working application gallery
â”‚   â””â”€â”€ evidence/chat/         Archived troubleshooting/testing screenshots
â”œâ”€â”€ evaluation/                Archived reproducible test evidence
â”œâ”€â”€ .github/workflows/ci.yml   GitHub Actions workflow definition
â”œâ”€â”€ compose.yaml               App + PostgreSQL local services
â”œâ”€â”€ Dockerfile                 Multi-stage application build
â””â”€â”€ README.md                  You are here
```

## Documentation for different readers

| Reader | Start here |
|---|---|
| First-time user | [Plain-language user manual](docs/USER_MANUAL.md) |
| Recruiter or reviewer | [Screenshots](docs/SCREENSHOT_GALLERY.md) Â· [Three-minute demo](docs/DEMO_SCRIPT.md) |
| Developer | [Architecture](docs/ARCHITECTURE.md) Â· [API contract](docs/API.md) Â· [Database design](docs/DATABASE_DESIGN.md) |
| Student rebuilding it | [Ten-step build guide](docs/BUILD_STEP_BY_STEP.md) Â· [Git contribution workflow](docs/DEVELOPMENT_WORKFLOW.md) |
| Troubleshooting | [Windows and Docker troubleshooting](docs/TROUBLESHOOTING.md) |
| Testing reviewer | [Local verification record](docs/LOCAL_VERIFICATION.md) Â· [Original test report](docs/TEST_RESULTS.md) |
| Academic review | [Research and standards rationale](docs/RESEARCH_GAP.md) |

## Scope and limitations

- All accounts and charges are fictional; **no actual refunds or financial transfers**.
- No telecom billing provider, bank, payment gateway, SMS or AI integration.
- The audit log is append-oriented through application code, **not tamper-proof against a database administrator**.
- The fixed 72-hour SLA includes weekends and holidays. It is a project rule, not a telecom provider commitment.
- No live customer traffic, production load measurements or measured business improvement is claimed.
- A successful local Docker build is not evidence of a successful GitHub-hosted CI run.
- Security is appropriate to an educational local demo, not a production security certification.

## Open source and acknowledgements

Built as an independent final-year software engineering portfolio project. Source files and documentation are covered by the repository's [LICENSE](LICENSE). Project decisions, build steps and limitations are documented so another developer can understand or reproduce the implementation.

