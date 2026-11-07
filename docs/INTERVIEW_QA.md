# Interview questions and answers

**Why this project?** A dispute needs more than a saved form: the right person must handle it, the decision must be final, and the deadline and history must remain explainable. This is an applied student implementation using fictional data.

**Why one application?** The workflow fits one database transaction. A modular monolith keeps debugging, deployment and review manageable without distributed transactions or message brokers.

**How is authorization enforced?** Spring Security authenticates the session; services check the stored account role, owner or assignee. A hidden frontend button is convenience, not protection.

**Why session authentication?** The browser and API share one origin after packaging. A server session is enough for this scope. HttpOnly prevents JavaScript reading the session cookie; CSRF tokens protect authenticated writes.

**What happens after login?** Spring rotates the session identity and clears the old CSRF token. The client fetches a fresh token for its next mutation. Logout invalidates the session.

**How do you stop duplicate approvals?** Requests carry the last-read version. The service checks it; Hibernate's versioned update detects overlapping writes; the database permits only one decision per dispute. The loser gets 409, and its transaction leaves no decision/audit residue. The tests cover two competing requests, not large-scale load.

**Why flush before inserting the decision?** It forces the versioned dispute update before subsequent inserts. The losing request fails early, and any later failure still rolls everything back because flush is not commit.

**What if the audit insert fails?** The transaction rolls back the decision and dispute change. The integration test injects a database failure and verifies this behavior.

**Why BigDecimal?** Financial decimal inputs should not be calculated with binary floating point. The database is NUMERIC(12,2); DTO validation limits scale and amount. The frontend submits decimal text.

**How do you calculate the SLA?** Creation plus 72 elapsed hours, UTC, immutable. Unresolved cases are overdue strictly after the deadline. Resolution exactly at the deadline is within SLA. No business-calendar rules are implied.

**Which records enter the weekly report?** Creation counts use created_at. All outcome metrics use resolved_at in [Monday 00:00 UTC, next Monday 00:00 UTC). This avoids mixing newly created cases with a different resolution cohort.

**Why those indexes?** Customer history and assigned support queue need selective access paths; creation/resolution timestamps support weekly filters; audit IDs break timestamp ties. Explain query patterns rather than claiming measured speedups.

**Is audit append-only?** Through the application, yes: the repository/API offer insertion and reading. Administrators can still alter database rows. It is not a signed, tamper-proof ledger.

**How do you debug a failed case?** Reproduce with synthetic inputs, inspect HTTP status and request version, examine the audit sequence and database state, read server logs, then add a regression test for the root cause. Never expose passwords or tokens in logs.

**What did Git demonstrate?** The repository includes a branch/PR process and CI configuration. Only claim actual reviewed PRs or teamwork when your own GitHub history provides that evidence.

**What would need work before real use?** Real identity management, operational backups, rate limiting, security review, retention policy and deployment hardening. This app neither connects to a telecom billing system nor moves money.

**What does coverage prove?** It identifies executed source lines for this test run. It does not prove every assertion is strong or every concurrency schedule is safe. Report the exact LINE percentage from TEST_RESULTS.md, alongside scenario evidence and limitations.
