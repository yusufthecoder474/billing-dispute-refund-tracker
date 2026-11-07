# Contributing

Read [requirements](docs/REQUIREMENTS.md) and [development workflow](docs/DEVELOPMENT_WORKFLOW.md) before changing behavior. Keep the six-feature scope, Java 21/Spring Boot 3/PostgreSQL/React stack and synthetic-only data policy.

Use a feature branch and the PR template. Add tests for changed behavior, especially permissions, state transitions, money and timestamps. Never edit applied Flyway migrations; add a versioned migration. Run `mvn -f backend/pom.xml verify`, frontend checks and affected Playwright scenarios. Use `scripts/verify.sh` or `scripts/verify.ps1` for the full local suite with Docker running. Document any check you could not run. Do not fabricate a passing result or screenshot.
