# Execution report

Date: 7 November 2026 (Asia/Kolkata; raw logs use UTC). Environment: Linux x86_64, Temurin JDK 21.0.13+9, Maven 3.9.11, Node 24.22.1, npm 11.9.0, real PostgreSQL 16.4 and Playwright 1.56.1 Chromium 141.0.7390.37. Docker and PowerShell were unavailable.

| Check | Status | Passed | Failed/errors | Skipped | Command and evidence |
|---|---|---:|---:|---:|---|
| Backend compile and unit tests | PASSED | 37 | 0 | 0 | `mvn -B clean verify` from backend; RulesTest JUnit XML |
| PostgreSQL integration, explicit external mode | PASSED | 30 | 0 | 0 | Same command with TEST_DB_URL/USER/PASSWORD; WorkflowIT XML |
| Executable Spring Boot package | PASSED | N/A | 0 | N/A | `mvn -B -DskipTests package`, after copying Vite dist into static resources; JAR launched for browser suite |
| Frontend lockfile install | PASSED | N/A | 0 | N/A | `npm ci --no-audit --no-fund` from frontend |
| TypeScript check and Vite build | PASSED | N/A | 0 | N/A | `npm run typecheck`, `npm run build` from frontend |
| React component tests | PASSED | 8 | 0 | 0 | `npm test` from frontend |
| Browser/API end-to-end scenarios | PASSED | 5 | 0 | 0 | `node node_modules/playwright/cli.js test` from e2e against the actual JAR and PostgreSQL; equivalent local CLI to `npm test` |
| JaCoCo LINE coverage | PASSED | 406 covered lines | 14 missed lines | N/A | 96.67% = 406/(406+14); target >=75% |
| Runtime OpenAPI and health endpoint | PASSED | N/A | 0 | N/A | HTTP GET /v3/api-docs and /api/health captured in evaluation |
| Testcontainers provisioning | NOT EXECUTED | N/A | N/A | N/A | Docker daemon unavailable; default path is still configured and does not silently skip |
| Docker image / Compose startup | NOT EXECUTED | N/A | N/A | N/A | No Docker installed; files reviewed, not runtime-verified |
| Bash script syntax | PASSED | N/A | 0 | N/A | `bash -n scripts/setup.sh scripts/verify.sh`; runtime execution still unverified |
| Windows PowerShell setup/verify | NOT EXECUTED | N/A | N/A | N/A | Linux host without PowerShell |
| Full Ubuntu setup/verify scripts | NOT EXECUTED | N/A | N/A | N/A | Depend on Docker; individual native build/test equivalents ran |
| Hosted GitHub Actions | NOT EXECUTED | N/A | N/A | N/A | No remote repository/workflow run created |
| Manual-versus-UI timing | NOT EXECUTED | N/A | N/A | N/A | No timed human baseline |

There are no remaining failed checks in the executed final suite. **NOT EXECUTED** is not a pass. The 80 passing test cases comprise 37 + 30 + 8 + 5 across layers and include parameterized unit invocations. Coverage is JaCoCo's top-level LINE counter, not instruction or branch coverage. No coverage exclusions were configured. It measures only the instrumented backend tests, not browser coverage.

## Exact native verification route

A temporary PostgreSQL 16.4 binary distribution was used because Docker was absent. It ran in a disposable directory with loopback access, not an in-memory replacement. The same WorkflowIT class selects Testcontainers when TEST_DB_URL is absent. For a local disposable database, the portable equivalent is:

```sh
TEST_DB_URL=jdbc:postgresql://127.0.0.1:55432/billing_test TEST_DB_USER=agent TEST_DB_PASSWORD='' mvn -B -f backend/pom.xml clean verify
```

The host additionally needed a temporary Maven proxy settings file and the system Java trust store. Those environment-specific paths/settings are not part of the project or normal setup instructions. A full JDK was installed because the preinstalled Java runtime lacked javac. Test resources select Mockito's subclass mock maker, avoiding self-attach instrumentation; no PostgreSQL assertions were replaced with mocks.

## Observed evaluation

| Evaluation | Sample | Observed |
|---|---|---|
| SLA aggregate fixture | 7 records | 4 open, 2 due soon, 1 overdue, 3 resolved; 2 resolved within SLA, 1 breached, matching expected totals |
| Weekly report | 2 resolutions in the selected week | 1 created, 2 resolved, 1 within, 1 breached, 73.00 average hours, INR 25.50 refund total |
| Competing final decisions | 2 requests | HTTP 200 and 409; 1 decision row, 0 duplicate decisions, 1 final audit event |
| Approval audit path | 5 required actions | 5 observed in expected order |
| Forced audit insert failure | 1 final-decision attempt | No decision inserted; dispute remains PENDING_DECISION |
| Real UI screenshots | 2 captures | Detail/timeline and manager dashboard from successful browser run |

See [observed outputs](../evaluation/observed/summary.json), [fixture inputs](../backend/src/test/resources/evaluation-fixture.sql), [expected values](../evaluation/expected.json), [JaCoCo XML](../evaluation/observed/jacoco.xml) and the test logs in evaluation/observed. JUnit XML copies omit environment property blocks but retain cases, outcomes and timing.

## Earlier failures corrected before the final run

| Earlier check | Status at the time | Cause and resolution |
|---|---|---|
| Maven dependency resolution | FAILED | Host proxy/trust-store configuration; fixed in temporary runner settings. |
| Initial Java compile | FAILED | Preinstalled JRE did not provide the compiler; downloaded full JDK 21. |
| Initial Vite build | FAILED | Unnecessary empty data-URL CSS import; removed. |
| Initial integration invocation | FAILED | Mockito inline mock maker could not attach an agent in this runtime; selected subclass maker in test resources. |

The rollback test intentionally logs a database exception while proving rollback; that expected injected failure is not a failed test. This small deterministic suite does not prove production reliability, financial correctness for real telecom records, throughput, or improved customer satisfaction.
