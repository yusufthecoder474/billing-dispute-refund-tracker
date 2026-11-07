# Local deployment and optional hosting assessment

The verified local environment uses **three Docker Compose containers**: PostgreSQL 16.4, dispute-service (Spring Boot, internal port 8080, default host port 18081) and reporting-service (Spring Boot, internal port 8081, default host port 18082). Only dispute-service connects directly to PostgreSQL. The React frontend is served by dispute-service.

## Windows quick start

Install and start Docker Desktop. From the repository root:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup.ps1
docker compose ps
python .\scripts\smoke-two-services.py
Start-Process http://localhost:18081
```

The setup script preserves an existing `.env`, copies `.env.example` if absent, generates a local random `INTERNAL_API_KEY` if missing, and builds/starts the three services with health checks. Keep `.env` private and never push it. Default database port is 55433, dispute web port 18081, reporting port 18082. Override host ports in `.env` if needed. `docker compose config --quiet` checks the configuration.

**Linux/macOS:** run `./scripts/setup.sh` from the repository root; check the instructions in [the microservices guide](MICROSERVICES.md). The reporting health endpoint is `http://localhost:18082/api/health`. Both services have OpenAPI endpoints in the local Compose profile.

## Logs and safe shutdown

```powershell
docker compose ps
docker compose logs --tail=100 dispute-service reporting-service db
docker compose stop
docker compose start
# Removes containers while retaining the database volume:
docker compose down
```

Avoid `docker compose down -v` unless intentionally deleting all synthetic demo database records. Tests requiring a database should use a separate disposable PostgreSQL database (previous local test host port 55434), **not** the running demo database on port 55433.

## Independent builds and test verification

```powershell
mvn -B -f .\dispute-service\pom.xml clean verify
mvn -B -f .\reporting-service\pom.xml clean verify
python .\scripts\coverage-summary.py
```

The second service gets reporting inputs through an API-key-protected REST export. If either service is unavailable, weekly reports fail with HTTP 503 rather than silently returning incomplete data. See [microservices API and design](MICROSERVICES.md), [verification record](LOCAL_VERIFICATION.md), and [troubleshooting](TROUBLESHOOTING.md).

**Last verified locally:** 7 November 2026, with three healthy containers and a successful reporting smoke test. Both GitHub Actions runs passed (feature branch 3m 19s; `main` 3m 34s). No public deployment has been verified. The optional hosting assessment below was researched for the original application and is **not** an executed hosting guide for this two-service version.

## Optional public route, checked 7 November 2026

Official sources: [Render free instances](https://render.com/docs/free), [Docker deployment](https://render.com/docs/docker), [pricing](https://render.com/pricing). Render supports Java through Docker and offers free web and PostgreSQL instances. This is a temporary demo route: free PostgreSQL expires after 30 days, has 1 GB storage and no backups. Web instances sleep after 15 minutes idle and share 750 instance hours per workspace per month. Bandwidth/build quotas apply; without a payment method, excess usage can suspend services/builds. The reviewed sources do not establish a universal no-card signup guarantee; verify account-specific requirements before proceeding.

No durable, universally card-free public Java-plus-PostgreSQL route was verified here. Docker Compose remains the recommendation. No account or billable resource was created. If you independently choose a temporary Render demo, review current Free plan selections, expiration, usage limits and account requirements first; never enter payment details expecting this repository to prevent charges. Public use also needs HTTPS/Secure cookies and non-demo identity provisioning, which is outside this local prototype.
