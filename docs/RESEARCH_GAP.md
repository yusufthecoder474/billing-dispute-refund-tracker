# Applied engineering rationale and verified references

Question: can a small, explainable application enforce dispute ownership, decision uniqueness and deadline/report consistency using a relational database and automated tests?

This is an applied software engineering implementation, not a new algorithm or evidence that existing ticketing products lack these capabilities. The practical need is a runnable student reference that connects complaint handling to executable transaction, authorization and reporting rules. No survey, adoption study or measured commercial improvement is claimed.

The following official publications were checked on 7 November 2026. ISO discussion is limited to public abstracts; the full paid standards were not reviewed. No unverifiable paper or DOI is included.

| Exact publication | Issuer/editors, year and publisher | Verified relevance | Practical boundary and project response |
|---|---|---|---|
| [ISO 10002:2018 — Quality management — Customer satisfaction — Guidelines for complaints handling in organizations](https://committee.iso.org/standard/10002?browse=ics) | ISO, 2018; ISO | Describes complaint-handling processes across planning, operation and improvement. | A process guideline is not this application's source code. F1, F3 and F5 turn the selected local rules into submission, controlled decisions and traceable history. No ISO conformity claim. |
| [ISO/IEC 20000-1:2018 — Information technology — Service management — Part 1: Service management system requirements](https://www.iso.org/standard/70636.html) | ISO/IEC, 2018; ISO | Covers establishing and improving service management, including monitoring and measurement. | It does not prescribe this educational 72-hour deadline or SQL report. F4 and F6 implement and test explicitly chosen UTC rules; they do not certify a service management system. |
| [PROV-DM: The PROV Data Model](https://www.w3.org/TR/2013/REC-prov-dm-20130430/) | Editors Luc Moreau and Paolo Missier, 2013; W3C Recommendation, 30 April | Models entities, activities and responsible agents for provenance. | The domain-neutral model is not a billing workflow. F5 stores a simple actor/action/entity/time history; it does not implement PROV serialization or claim PROV conformance. |

These are implementation needs, not demonstrated gaps in the literature. Evaluation tests whether the chosen implementation obeys its own requirements. A real operational study would be required to evaluate staff effort, turnaround, or customer satisfaction.

| Dimension | Manual spreadsheet/email baseline (conceptual) | Implemented application |
|---|---|---|
| State | People update labels; consistency depends on procedure. | Server rejects unsupported transitions. |
| Decision duplication | Requires manual coordination or separately configured controls. | Versioned transaction and unique decision FK. |
| Ownership | Depends on file/mail access configuration. | Server scopes customer/support reads and checks actions. |
| Deadline | Formula and timezone rules must be maintained. | Fixed UTC deadline and SQL boundary tests. |
| History | Depends on enabled change history and email retention. | Committed actions append an audit event. |
| Weekly totals | Manual filtering and formulas. | Defined timestamp cohorts and CSV aggregation. |
| Time to locate due-soon cases | Not measured. | Not measured against a human baseline. |

The baseline describes a simple unautomated workflow, not every spreadsheet or ticketing system. The project does not claim measured superiority over commercial products.
