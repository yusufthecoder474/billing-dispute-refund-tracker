# Local deployment and optional hosting assessment

The supported zero-service-cost demonstration is Docker Compose on your own machine. Install Docker Desktop on Windows 11 or Docker Engine plus the Compose plugin on Ubuntu. Start the engine before setup. Internet access is needed for the initial image and dependency downloads.

From the extracted repository root:

```powershell
Copy-Item .env.example .env
docker compose up --build -d --wait --wait-timeout 240
Start-Process http://localhost:8080
```

```sh
cp .env.example .env
docker compose up --build -d --wait --wait-timeout 240
# Open http://localhost:8080 in your browser.
```

Do not overwrite an existing .env containing your chosen password. The setup scripts copy only if missing. Compose runs two containers, mounts a persistent database volume, waits for pg_isready and builds Vite assets into the Spring Boot JAR. Container startup runs Flyway then Hibernate schema validation. The application health URL is /api/health; this lightweight liveness check does not query the database after startup. Compose's database health check is separate.

Configuration: DB_URL is a JDBC URL, DB_USER and DB_PASSWORD are environment values, PORT defaults to 8080, COOKIE_SECURE defaults true outside the local/test override, and API_DOCS_ENABLED defaults false outside local/test. Compose binds both published ports to 127.0.0.1 and explicitly enables the local profile and demo seeds. APP_PORT and DB_PORT change host ports only. The final app runs as an unprivileged Linux user.

Logs and shutdown:

```sh
docker compose ps
docker compose logs --tail=100 app db
docker compose logs -f app
docker compose stop
docker compose start
# Remove containers while retaining the database volume:
docker compose down
```

Destructive reset: `docker compose down -v` deletes the database volume and every synthetic dispute. Only run it when you intentionally want a clean demo. Then start Compose again. Do not repair Flyway by deleting its history table. Future schema changes require new migrations.

## Development without the application container

Start only the database with `docker compose up -d db`. With Java JDK 21, Maven 3.9+ and Node 22.12+, run the backend and Vite in separate terminals. Set DB_PASSWORD to the value in .env; Maven does not automatically load .env.

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:DB_PASSWORD='local-billing-demo-change-me'
mvn -f backend/pom.xml spring-boot:run
# Second terminal, repository root:
Set-Location frontend
npm ci
npm run dev
```

```sh
SPRING_PROFILES_ACTIVE=local DB_PASSWORD=local-billing-demo-change-me mvn -f backend/pom.xml spring-boot:run
# Second terminal, repository root:
cd frontend
npm ci
npm run dev
```

Open http://localhost:5173. Vite proxies /api to localhost:8080. If you changed DB_PORT, also set DB_URL explicitly. For a production-style local package, use Compose; the Maven-only command does not build React for you.

## Optional public route, checked 7 November 2026

Official sources: [Render free instances](https://render.com/docs/free), [Docker deployment](https://render.com/docs/docker), [pricing](https://render.com/pricing). Render supports Java through Docker and offers free web and PostgreSQL instances. This is a temporary demo route: free PostgreSQL expires after 30 days, has 1 GB storage and no backups. Web instances sleep after 15 minutes idle and share 750 instance hours per workspace per month. Bandwidth/build quotas apply; without a payment method, excess usage can suspend services/builds. The reviewed sources do not establish a universal no-card signup guarantee; verify account-specific requirements before proceeding.

No durable, universally card-free public Java-plus-PostgreSQL route was verified here. Docker Compose remains the recommendation. No account or billable resource was created. If you independently choose a temporary Render demo, review current Free plan selections, expiration, usage limits and account requirements first; never enter payment details expecting this repository to prevent charges. Public use also needs HTTPS/Secure cookies and non-demo identity provisioning, which is outside this local prototype.
