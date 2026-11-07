# Development and review workflow

This is an independent student reference implementation. The following process supports a team, but is not evidence that collaboration or merged pull requests occurred.

1. Start from current main: `git switch main` then `git pull --ff-only`.
2. Create a focused branch: `git switch -c feature/dispute-submission`.
3. Edit code and tests together. Run the relevant checks in TEST_PLAN.md.
4. Review `git diff` and `git status`. Never stage `.env`, credentials, generated dependencies or real records.
5. Commit: `git add backend frontend docs` then `git commit -m "Add validated customer dispute submission"`.
6. Push: `git push -u origin feature/dispute-submission`.
7. On GitHub, open a pull request to main. Fill in the supplied template and attach actual test evidence. Request a reviewer using GitHub's Reviewers control.
8. Address comments with follow-up commits and explain the change in the discussion. Re-run affected checks.
9. Merge only after checks pass and the reviewer has resolved concerns. Use the repository's chosen merge style; do not rewrite others' commits without agreement.

Suggested branches: `feature/support-workflow`, `feature/sla-dashboard`, `feature/weekly-report`, `test/concurrent-decisions`, `fix/refund-validation`, `docs/user-manual`.

Review correctness, security, input validation, SQL/index behavior, transaction boundaries, concurrency, safe errors, meaningful tests, naming/readability, documentation and backward compatibility. Pay particular attention to authorization on reads as well as writes, the immutable deadline and rollback after audit insertion failure. A green build alone cannot prove production suitability.

Example PR description (illustrative, not an actual PR):

> A stale manager decision now returns HTTP 409 and prompts a reload. The request version is checked before changing the dispute, while JPA optimistic locking handles overlapping transactions. This addresses F3 and F5. Validation: attach the actual WorkflowIT concurrent-decision result and browser error-state screenshot from your run. Risk: clients must send the version from the latest detail response. No schema change.

GitHub Actions uses official actions/checkout@v4, setup-java@v4, setup-node@v4 and upload-artifact@v4 major tags. These are documented trusted publisher versions, not immutable SHA pins. Pin reviewed SHAs for stricter supply-chain controls. CI has read-only repository permission and runs on ubuntu-latest, which is expected to provide Docker for Testcontainers. The delivered workflow is not evidence that a hosted CI run succeeded.
