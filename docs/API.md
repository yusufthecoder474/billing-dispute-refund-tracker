# REST API contract

Base path: /api. JSON uses camelCase and ISO-8601 UTC timestamps. INR amounts are decimal numbers in responses; clients may submit decimal strings to preserve decimal text. IDs and versions are integers. Responses are DTOs, not JPA entities. Errors use `{"status":409,"message":"Stale version: reload the dispute.","timestamp":"2026-11-02T12:00:00Z"}`. Examples below are illustrative contract examples, not captured transaction evidence.

## Authentication and CSRF

1. GET /api/auth/csrf before login; retain the JSESSIONID cookie. Response: `{"token":"session-specific-token","headerName":"X-CSRF-TOKEN"}`. It is public and creates a session when necessary.
2. POST /api/auth/login as `application/x-www-form-urlencoded` with `username=customer@demo.local&password=DemoPass123!` and the returned token in X-CSRF-TOKEN. Login is intentionally a form, not a JSON DTO. Success 200: `{"authenticated":true}`. Missing/invalid credentials return 401; a missing or invalid CSRF token returns 403.
3. GET /api/auth/me. Response: `{"id":1,"email":"customer@demo.local","displayName":"Demo customer","role":"CUSTOMER"}`. A missing session returns 401.
4. Fetch /api/auth/csrf again after login because authentication clears the old token. Send it on all subsequent POSTs. Never log or hardcode token values.
5. POST /api/auth/logout with no body and valid CSRF; success 204, session invalidated. Fetch a new token before logging in again.

Spring Security owns login/logout. The auth UserView never includes a password hash. Unknown form credentials are 401 rather than validation-detail 400 to avoid disclosing account existence. Login/logout/me have no business resource ID, so no domain 404 or version 409 applies. Logout is safe if repeated with a fresh token. Missing CSRF can return 403 before authentication checks; authenticated role/ownership failures always return 403. The CSRF endpoint is an additional security endpoint, not a seventh business feature.

Swagger UI: http://localhost:8080/swagger-ui/index.html in local/test profiles. Sign in in the application on the same origin, GET the CSRF endpoint in Swagger, then use **Authorize** to paste its token. Cookies are sent by the browser. Refresh authorization after login/logout. GET endpoints do not require the CSRF header even though the documentation's global scheme makes it available. The runtime OpenAPI is at /v3/api-docs; docs/API.md supplies the role and semantic constraints.

## Endpoint rules

All list endpoints are zero-based: page >= 0; size 1–100. A page past the end is a successful empty page. Sorting supports createdAt, dueAt or status, direction asc/desc, with ID as tie-breaker. No caller may change an ID, owner, timestamp, status or refund directly through a generic edit endpoint.

| Endpoint | Roles/access | Request and validation | Success DTO | 400 | 401/403 | 404 | 409 |
|---|---|---|---|---|---|---|---|
| POST /auth/login | Public with CSRF | Form username/password | 200 authenticated flag | Malformed transport | Invalid credentials / invalid CSRF | N/A | N/A |
| POST /auth/logout | Session or anonymous with CSRF | No body | 204 no body | N/A | Invalid CSRF | N/A | N/A |
| GET /auth/me | Any signed-in role | None | 200 UserView | N/A | Missing session | N/A | N/A |
| GET /auth/csrf | Public | None | 200 token/headerName | N/A | N/A | N/A | N/A |
| POST /disputes | CUSTOMER | CreateDispute: category enum, description 10–2000 nonblank chars, positive amount <=9999999999.99 and <=2 decimals | 201 DisputeView | Validation | Missing session / wrong role or CSRF | N/A | DB constraint conflict only |
| GET /disputes | All, scoped to owner/assignment; manager all | page, size, status enum, sort, direction; optional sla=DUE_SOON or OVERDUE | 200 PageView<DisputeView> | Invalid query | Missing session | N/A | N/A |
| GET /disputes/{id} | Owner, assigned support or manager | Numeric ID | 200 Detail | Invalid ID syntax | Missing session / not visible | Missing ID | N/A |
| POST /disputes/{id}/assign | MANAGER | Assign: positive supportId and nonnegative version; target role SUPPORT, dispute OPEN | 200 DisputeView | Invalid DTO or target role | Missing session / wrong role or CSRF | Missing dispute/agent | Stale version, wrong state or unchanged assignment |
| POST /disputes/{id}/start-review | Assigned SUPPORT | Version: nonnegative version; OPEN required | 200 DisputeView | Invalid DTO | Missing session / unassigned, wrong role or CSRF | Missing dispute | Stale/wrong state |
| POST /disputes/{id}/submit-for-decision | Assigned SUPPORT | Version; IN_REVIEW required | 200 DisputeView | Invalid DTO | Missing session / unassigned, wrong role or CSRF | Missing dispute | Stale/wrong state |
| POST /disputes/{id}/decision | MANAGER | Decide: outcome APPROVED/REJECTED, amount 0..charge with <=2 decimals, explanation 10–2000 chars, version; rejection amount must be 0 | 200 DisputeView | Invalid DTO/refund | Missing session / wrong role or CSRF | Missing dispute | Stale, not pending or decision already exists |
| GET /disputes/{id}/audit | Same visibility as detail | page, size | 200 PageView<AuditView> oldest first | Invalid pagination/ID | Missing session / not visible | Missing dispute | N/A |
| GET /support-agents | MANAGER | None | 200 UserView[] demo support directory | N/A | Missing session / wrong role | N/A | N/A |
| GET /sla/summary | MANAGER | None | 200 SlaSummary | N/A | Missing session / wrong role | N/A | N/A |
| GET /reports/weekly | MANAGER | week=YYYY-MM-DD, must be Monday | 200 WeeklyReport | Missing/invalid/non-Monday date | Missing session / wrong role | N/A | N/A |
| GET /reports/weekly.csv | MANAGER | Same week rule | 200 text/csv with attachment filename | Same as JSON report | Missing session / wrong role | N/A | N/A |
| GET /health | Public | None | 200 {status:UP} | N/A | N/A | N/A | N/A |

No client-provided role header is used. A database integrity conflict maps to a safe 409 without SQL details; unexpected server failures map to 500 with a generic message and are logged server-side.

## DTO examples

CreateDispute request to POST /disputes:

```json
{"category":"DUPLICATE_CHARGE","description":"My synthetic monthly charge appears twice.","disputedAmount":"100.00"}
```

201 DisputeView; the same DTO shape is returned by all successful workflow POSTs:

```json
{"id":1,"customerId":1,"assignedSupportId":null,"category":"DUPLICATE_CHARGE","description":"My synthetic monthly charge appears twice.","disputedAmount":100.00,"currency":"INR","status":"OPEN","createdAt":"2026-11-02T12:00:00Z","dueAt":"2026-11-05T12:00:00Z","resolvedAt":null,"version":0}
```

GET /disputes?page=0&size=10&sort=dueAt&direction=asc&status=OPEN returns a PageView. Empty example: `{"content":[],"page":0,"size":10,"totalElements":0,"totalPages":0}`. Nonempty content contains DisputeView objects above. Customer/support scoping is automatic, never a request parameter.

POST /disputes/1/assign: `{"supportId":3,"version":0}` → DisputeView with assignedSupportId=3, status=OPEN, version=1.

POST /disputes/1/start-review: `{"version":1}` → DisputeView with status=IN_REVIEW, version=2.

POST /disputes/1/submit-for-decision: `{"version":2}` → DisputeView with status=PENDING_DECISION, version=3.

POST /disputes/1/decision:

```json
{"outcome":"APPROVED","approvedRefundAmount":"75.50","explanation":"Synthetic duplicate charge confirmed during review.","version":3}
```

Success is DisputeView with status=RESOLVED_APPROVED, a resolvedAt timestamp and version=4. For rejection use outcome=REJECTED and approvedRefundAmount="0.00"; success status is RESOLVED_REJECTED. A second decision returns 409 even with the latest version because the dispute is final.

GET /disputes/1 returns `{"dispute":<DisputeView>,"decision":null}` before resolution. After resolution decision is a DecisionView:

```json
{"outcome":"APPROVED","approvedRefundAmount":75.50,"explanation":"Synthetic duplicate charge confirmed during review.","createdAt":"2026-11-02T14:00:00Z","managerId":5}
```

The angle-bracket notation describes nesting, not literal JSON. GET /disputes/1/audit?page=0&size=20 example:

```json
{"content":[{"id":1,"actorId":1,"actor":"Demo customer","action":"CREATED","previousStatus":null,"newStatus":"OPEN","detail":"Dispute submitted","createdAt":"2026-11-02T12:00:00Z"}],"page":0,"size":20,"totalElements":1,"totalPages":1}
```

GET /support-agents example: `[{"id":3,"email":"support@demo.local","displayName":"Demo support","role":"SUPPORT"}]`. Seeded IDs are not a public contract; fetch the directory rather than hardcoding them.

GET /sla/summary example:

```json
{"open":4,"dueSoon":2,"overdue":1,"resolved":3,"resolvedWithinSla":2,"resolvedBreached":1,"asOf":"2026-11-02T12:00:00Z"}
```

GET /reports/weekly?week=2026-11-02 example:

```json
{"start":"2026-11-02T00:00:00Z","end":"2026-11-09T00:00:00Z","created":1,"resolved":2,"withinSla":1,"breached":1,"averageResolutionHours":73.00,"approvedRefundTotal":25.50}
```

GET /reports/weekly.csv?week=2026-11-02 returns the same metrics, with header `start_utc,end_utc,created,resolved,within_sla,breached,average_resolution_hours,approved_refund_inr` and one quoted data row. Content-Disposition is attachment; filename=weekly-2026-11-02.csv. Text cells are escaped and formula prefixes neutralized by Rules.csv. There is no customer text in this aggregate export.

## Metric definitions

| Metric | Selection and calculation |
|---|---|
| created | created_at >= Monday start and < next Monday |
| resolved | resolved_at within that half-open period, irrespective of creation week |
| withinSla | Resolved cohort with resolved_at <= due_at |
| breached | Resolved cohort with resolved_at > due_at; unresolved overdue cases are not included |
| averageResolutionHours | Mean (resolved_at-created_at) in elapsed hours for resolved cohort, rounded to 2 decimals; 0 for empty cohort |
| approvedRefundTotal | Sum of final decision amounts for resolved cohort; rejections are zero; 0 for empty cohort |

SLA dashboard uses all-time records, not the selected report week. Open = resolved_at is null. Due-soon includes due_at equal to now or now+12h. Overdue is due_at < now. Resolved classifications compare the resolution timestamp, so they never become overdue later.
