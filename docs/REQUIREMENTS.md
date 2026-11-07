# Requirements and acceptance criteria

| ID | Implemented behavior | Acceptance evidence to run |
|---|---|---|
| F1 | Customers submit a positive INR dispute and read only their own history/detail. | WorkflowIT creation, ownership and pagination; customer-dispute.spec.ts. |
| F2 | Manager assigns OPEN disputes; support sees and acts on assigned cases only. | Assigned queue, wrong-role and unassigned tests; support-workflow.spec.ts. |
| F3 | OPEN → IN_REVIEW → PENDING_DECISION → one final decision, bounded refund and explanation. | Approval, rejection, repeated decision, rollback, stale update and two-manager race tests. |
| F4 | Immutable 72-hour UTC deadline; 12-hour due-soon window; strict overdue comparison; inclusive resolved-within boundary. | RulesTest boundary cases and seven-row PostgreSQL fixture. |
| F5 | Committed actions append actor/status/time audit records. | Creation, complete approval, ownership, rollback and concurrent final audit assertions. |
| F6 | Monday UTC weekly created/resolved/SLA/time/refund totals and safe CSV. | Independent expected fixture and manager browser export. |

Nonfunctional constraints: one Java 21/Spring Boot 3 app, PostgreSQL database, React/TypeScript frontend, one Git repository, session authorization and CSRF, pagination, optimistic locking, transaction atomicity, no real payment transfer. Final deployment has only app and database containers. All reported measurements must name their environment and sample size.

Definition of done: all delivered paths exist; Java compiles; frontend type-checks and builds; automated checks are run when their runtimes are available; every failure or unexecuted check is disclosed; documentation commands match the repository; ZIP and manifest are produced. See TEST_RESULTS.md for which runtime acceptance checks were actually verified.

Decisions: approval may be zero (a final approved adjustment with no refund); explanations need 10–2000 characters for either outcome. Description uses the same length bounds. Reassignment is permitted only while OPEN. Customers cannot edit a submitted dispute. Managers may read all disputes. Support cannot read unassigned disputes. There is no registration, comment system, attachment upload, cancellation, reopen or deletion endpoint. Reports use resolution cohorts, not creation cohorts, for outcome metrics.
