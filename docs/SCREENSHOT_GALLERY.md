# Screenshot gallery — real project demonstration

[← Main README](../README.md) · [User manual](USER_MANUAL.md) · [Verification details](LOCAL_VERIFICATION.md)

All images on this page were copied from **actual screenshots shared during the Windows demonstration on 7 November 2026**, not generated mockups. Every application screenshot is preserved as a local repository file. They show synthetic data and a local application, not a public deployment.

The screenshots follow a single case: **Dispute #1**, a fictional **INR 1,000 duplicate charge** that received a **simulated INR 750 approval**. Times are stored/displayed in UTC. The terminal evidence later in this page includes both initial setup failures and their successful corrections.

## A. Customer → support → manager (13 screenshots)

### 1. Customer login

The local sign-in page with separate role-based demo accounts.

![Customer login](screenshots/01-login.png)

### 2. Customer queue

A new customer starts with zero disputes and a paginated list.

![Customer queue](screenshots/02-customer-disputes.png)

### 3. First dispute

Customer-submitted INR 1,000 dispute with 72-hour deadline and initial audit entry.

![First dispute](screenshots/03-customer-dispute-detail.png)

### 4. Manager queue

A manager sees all disputes, including Dispute #1.

![Manager queue](screenshots/04-manager-all-disputes.png)

### 5. Assignment form

The manager selects the support employee from a server-provided list.

![Assignment form](screenshots/05-manager-assign-support.png)

### 6. Assignment saved

Dispute version advances to 1 and an ASSIGNED audit entry appears.

![Assignment saved](screenshots/06-assignment-confirmed.png)

### 7. Assigned support view

Only the assigned agent can start the review.

![Assigned support view](screenshots/07-support-open.png)

### 8. Review started

IN_REVIEW status, version 2 and REVIEW_STARTED event.

![Review started](screenshots/08-support-in-review.png)

### 9. Submitted for decision

PENDING_DECISION status, version 3, and history retained.

![Submitted for decision](screenshots/09-pending-decision.png)

### 10. Manager decision

Simulated approval entry requires refund amount and explanation.

![Manager decision](screenshots/10-manager-decision-form.png)

### 11. Resolved dispute

INR 750 simulated approval, version 4, within SLA, five audit events.

![Resolved dispute](screenshots/11-resolved-and-audit.png)

### 12. SLA overview

One case resolved within SLA and no overdue cases in this snapshot.

![SLA overview](screenshots/12-sla-dashboard.png)

### 13. Weekly report

Monday-to-Monday report includes one resolution and INR 750 approved refund total.

![Weekly report](screenshots/13-weekly-report.png)

---

## B. Windows setup, testing and Git evidence (21 screenshots)

These screenshots document both the problems encountered and later successful checks. A failure pictured here should **not** be interpreted as a current project failure when the later correction is shown.

<details>
<summary>1. <strong>Initial port conflict</strong> — Port 5432 already allocated when Docker attempted startup.</summary>

![Initial port conflict](evidence/chat/01-initial-port-conflict.png)

</details>

<details>
<summary>2. <strong>Port collision in browser</strong> — EventPulse-SRE was already using port 8080.</summary>

![Port collision in browser](evidence/chat/02-other-app-at-8080.png)

</details>

<details>
<summary>3. <strong>Docker Compose healthy</strong> — App at 18081 and database at 55433 reported healthy.</summary>

![Docker Compose healthy](evidence/chat/03-healthy-docker-compose.png)

</details>

<details>
<summary>4. <strong>PowerShell execution policy</strong> — Unsigned local verification script initially blocked.</summary>

![PowerShell execution policy](evidence/chat/04-powershell-script-restriction.png)

</details>

<details>
<summary>5. <strong>Docker Desktop projects</strong> — Local Compose projects shown; includes Billing Tracker.</summary>

![Docker Desktop projects](evidence/chat/05-docker-desktop.png)

</details>

<details>
<summary>6. <strong>Maven missing from PATH</strong> — Verification initially stopped because mvn was not found.</summary>

![Maven missing from PATH](evidence/chat/06-maven-not-on-path.png)

</details>

<details>
<summary>7. <strong>Extracted Maven distribution</strong> — Maven folder found and used on Windows.</summary>

![Extracted Maven distribution](evidence/chat/07-maven-extracted.png)

</details>

<details>
<summary>8. <strong>Testcontainers discovery error</strong> — 30 integration errors before isolated database configuration.</summary>

![Testcontainers discovery error](evidence/chat/08-testcontainers-failure.png)

</details>

<details>
<summary>9. <strong>Root cause diagnosis</strong> — Could not find a valid Docker environment within Testcontainers.</summary>

![Root cause diagnosis](evidence/chat/09-root-cause-docker-env.png)

</details>

<details>
<summary>10. <strong>Disposable PostgreSQL</strong> — Separate test container launched on 55434.</summary>

![Disposable PostgreSQL](evidence/chat/10-isolated-test-db-created.png)

</details>

<details>
<summary>11. <strong>Java Maven BUILD SUCCESS</strong> — 30 PostgreSQL integration tests passed; report generated.</summary>

![Java Maven BUILD SUCCESS](evidence/chat/11-maven-build-success.png)

</details>

<details>
<summary>12. <strong>Frontend dependencies installed</strong> — Original frontend audit reported four issues before updates.</summary>

![Frontend dependencies installed](evidence/chat/12-frontend-npm-ci-warnings.png)

</details>

<details>
<summary>13. <strong>Original Vite build success</strong> — Original frontend build succeeded.</summary>

![Original Vite build success](evidence/chat/13-frontend-original-build.png)

</details>

<details>
<summary>14. <strong>Original eight component tests</strong> — Vitest component suite passed.</summary>

![Original eight component tests](evidence/chat/14-original-component-tests.png)

</details>

<details>
<summary>15. <strong>Playwright dependencies installed</strong> — E2E npm dependencies installed with zero reported vulnerabilities.</summary>

![Playwright dependencies installed](evidence/chat/15-e2e-dependency-install.png)

</details>

<details>
<summary>16. <strong>Five Playwright tests</strong> — Real browser and API scenarios passed in 12.2 seconds.</summary>

![Five Playwright tests](evidence/chat/16-playwright-5-passed.png)

</details>

<details>
<summary>17. <strong>Initial npm security audit</strong> — Four vulnerabilities found in frontend development tools.</summary>

![Initial npm security audit](evidence/chat/17-initial-security-audit.png)

</details>

<details>
<summary>18. <strong>Patched package audit</strong> — Vite/Vitest updated; npm install reported zero vulnerabilities.</summary>

![Patched package audit](evidence/chat/18-patched-dependencies-audit-clean.png)

</details>

<details>
<summary>19. <strong>Retested patched frontend</strong> — Vite build succeeded and eight tests passed under Vitest 4.1.12.</summary>

![Retested patched frontend](evidence/chat/19-updated-frontend-build-tests.png)

</details>

<details>
<summary>20. <strong>Git repo not yet initialized</strong> — git status failed before git init.</summary>

![Git repo not yet initialized](evidence/chat/20-git-repo-not-initialized.png)

</details>

<details>
<summary>21. <strong>Git initialized</strong> — main branch created, no commits yet.</summary>

![Git initialized](evidence/chat/21-git-initialized.png)

</details>

## C. Downloaded weekly CSV

The original exported example from the 2–8 November 2026 report is preserved in [`docs/evidence/weekly-2026-11-02.csv`](evidence/weekly-2026-11-02.csv). The CSV came from the running application and shows one created/resolved case and INR 750 of simulated approvals.

## Regenerate the app screenshots

The **13 original screenshots above are already included**. There is nothing you need to save manually. If you later change the application UI and want fresh images, the included optional Playwright capture script can regenerate the application screenshots in `docs/screenshots/`.

From the repository root on Windows, with the application already running:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\scripts\capture-readme-screenshots.ps1" -BaseUrl "http://127.0.0.1:18081"
```

Or on Linux:

```bash
bash scripts/capture-readme-screenshots.sh http://127.0.0.1:18081
```

The script signs in with local demo accounts and **creates another synthetic dispute** to exercise the workflow, which will change report totals. Run it against a disposable local demo, not a production database. The regenerated captures replace the 13 application files, while these historical terminal screenshots remain unchanged.

The screenshot-regeneration script is supplied for convenience and **has not been executed in this chat environment**. The 13 included images are already verified as file assets; you can publish the documentation without regenerating them.
