# Three-minute interview demonstration

Prepare before the timed demo: start Docker Desktop/Engine; from the repository root run `docker compose up --build -d --wait --wait-timeout 240`. Open http://localhost:8080 in separate browser profiles or private windows for customer, support and manager; each profile must have its own cookie session. Use the documented demo accounts. Rehearse once and use a fresh synthetic dispute. Do not say an outcome occurred until you see it.

| Time | Show and do | Natural explanation |
|---|---|---|
| 0:00–0:25 | Customer login and New dispute page. | “I built this to make billing disputes traceable from submission to a final decision. This is a local student demonstration using fictional records. A refund approval here never transfers money.” |
| 0:25–0:50 | Enter a duplicate charge, INR 100 and a short description. Try amount -1 to show browser validation, then correct it and submit. | “The form collects the charge and reason. Validation also runs on the server, so bypassing the browser cannot submit a negative amount. Each case gets a fixed deadline 72 hours after creation.” |
| 0:50–1:15 | In the manager window, refresh Disputes, open the new ID and assign Demo support. In support window refresh the queue and open it. | “A manager assigns the case. Support can only see assigned cases, and customers can only see their own. Those checks are in the backend.” |
| 1:15–1:40 | Select Start review, then Submit for decision. | “The workflow has explicit steps. You cannot jump from a new dispute straight to a final approval. Each successful action adds an audit event.” |
| 1:40–2:05 | Manager reloads, enters INR 75.50 and a meaningful explanation, saves final decision. Show outcome and timeline. | “The decision, final status and audit event are saved together. If one part fails, the database rolls all of them back. The explanation stays visible to the customer.” |
| 2:05–2:30 | Show SLA & reports, choose a Monday, generate and download CSV. | “Due soon means the next 12 hours. Everything uses UTC, including weekends. Weekly created counts use creation time; resolution metrics use the week the case was resolved.” |
| 2:30–3:00 | Show DisputeService.decide, the @Version field, unique decision SQL constraint and TEST_RESULTS.md. | “Two managers may act on the same version. One can commit; the other gets a conflict and must reload. I tested the two-request case and checked that only one decision and final audit event remained. These are local verification results, not production scale claims.” |

For server-side validation, Swagger or the API tests can show a refund above the disputed amount returning 400. Do this outside the three-minute flow if switching tools would distract. If your demo fails, describe the error and inspect logs instead of claiming it succeeded.

**How is this different from a normal CRUD project?**

“The main work is enforcing rules across several records. A case can only move through valid states, only the right role can act, and a final decision can happen once. I use a versioned database transaction to protect concurrent decisions, with an audit record committed alongside the change. The deadline and weekly reports have explicit UTC boundaries, and automated tests check permissions, rollback and boundary cases. I can explain why the stored data stays consistent, not just how a form saves a row.”
