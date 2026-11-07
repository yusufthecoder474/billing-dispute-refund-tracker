# Install this README upgrade into your existing working project

This ZIP is a **documentation overlay**, not a replacement application. It includes an updated README, docs, 13 actual app screenshots from this conversation, 21 troubleshooting/test screenshots, the exported demo CSV and an optional future screenshot-capture script.

**Why an overlay?** You already patched frontend dependencies locally (Vite 7.3.7 / Vitest 4.1.12). Your original source ZIP predates those fixes. This package deliberately **does not contain or overwrite** `frontend/`, `backend/`, `.env`, lockfiles or database content.

## Windows installation

1. Save `Billing-Tracker-README-Upgrade.zip` in `D:\Downloads` (or note its actual location).
2. Open PowerShell **in your existing** `billing-dispute-refund-tracker` folder.
3. To protect your existing documentation before replacement, run:

```powershell
New-Item -ItemType Directory -Path '..\billing-docs-backup' -Force | Out-Null
Copy-Item README.md '..\billing-docs-backup\README.md' -Force
Copy-Item docs/USER_MANUAL.md '..\billing-docs-backup\USER_MANUAL.md' -Force
Copy-Item docs/ARCHITECTURE.md '..\billing-docs-backup\ARCHITECTURE.md' -Force
Copy-Item docs/TROUBLESHOOTING.md '..\billing-docs-backup\TROUBLESHOOTING.md' -Force
```

4. Extract the overlay **into this current folder** (not a newly nested folder):

```powershell
Expand-Archive -LiteralPath 'D:\Downloads\Billing-Tracker-README-Upgrade.zip' -DestinationPath (Get-Location).Path -Force
```

If your browser downloaded the ZIP elsewhere, change only the ZIP path.

5. Confirm the files and screenshot count:

```powershell
Test-Path .\README.md
(Get-ChildItem .\docs\screenshots\[0-9][0-9]-*.png).Count
(Get-ChildItem .\docs\evidence\chat\*.png).Count
```

Expected: README exists; **13 application screenshots** and **21 terminal screenshots**.

6. Verify all 13 app screenshot assets:

```powershell
(Get-ChildItem .\docs\screenshots\[0-9][0-9]-*.png).Count
```

Expected: **13**. The original project's two screenshot files may also remain, which is fine.

7. Open `README.md` in VS Code or publish the repository to GitHub to see Mermaid diagrams and image galleries rendered.

8. Before committing, review changes and ignore files:

```powershell
git status --short
git check-ignore .env
```

Expected: `.env` is ignored (if it exists), and the source files are still available. Backups are stored outside the Git repository in the neighboring `billing-docs-backup` directory.

The user already ran `git init -b main`. This overlay does **not** run Git commands or push any files on your behalf.

## What is new?

- `README.md` — project story, badges, six features, detailed workflow, Mermaid architecture + ER diagrams, selected real images, setup/test/API instructions.
- `docs/SCREENSHOT_GALLERY.md` — all 13 actual app screens and 21 original terminal/setup/test captures.
- `docs/LOCAL_VERIFICATION.md` — local/archived status separation and real measured results.
- `docs/USER_MANUAL.md` — human-readable customer/support/manager walkthrough with screenshots.
- `docs/ARCHITECTURE.md` — architecture, state transitions, concurrency sequence and schema summary.
- `docs/TROUBLESHOOTING.md` — Windows port conflicts, Maven PATH, isolated PostgreSQL test DB, PowerShell policies, npm fixes and Git setup.
- `docs/MAINTENANCE_PROMPT.md` — short prompt for future documentation edits, optional.
- `docs/screenshots/` and `docs/evidence/` — the original visuals.
- `e2e/readme-captures/`, `e2e/playwright.screenshots.config.ts`, `scripts/capture-readme-screenshots.*` — optional **unexecuted** future screenshot automation.

## Limitations

The screenshot automation is based on inspected source selectors but has not been rerun during packaging. You do **not** need it now: the original screenshots are already included. When run later it adds one synthetic dispute and replaces the 13 app screenshots. It leaves the preserved chat-evidence screenshots alone.
