# Reproducible evaluation

Use a disposable database. Run `mvn -f backend/pom.xml clean verify` with Docker available. Inputs: backend/src/test/resources/evaluation-fixture.sql and evaluation/expected.json. Outputs: backend/target/evaluation/fixture-results.json and concurrency.json; Surefire/Failsafe XML; JaCoCo reports. Delivered observed outputs, when run successfully, are copied under evaluation/observed.

The fixture has seven synthetic disputes and a fixed clock at 2026-11-02T12:00:00Z. It uses no randomness, so no seed is required. Four unresolved records cover normal, exact 12-hour boundary, exact deadline and one second overdue. Three resolutions cover two within SLA and one breach; one resolution falls before the reporting week and must be excluded. Two included resolution durations are 72 and 74 hours, giving independently calculated mean 73.00. One approved decision contributes INR 25.50. Only one case is created inside the week.

The integration test checks SQL summary/report totals against explicit constants, not against a second call to the reporting implementation. Unit cases separately test classification boundary rules. The two-request concurrency output records decisions and successful final audit count. Workflow tests require five audit events for creation, assignment, review, submission and approval, and a forced audit failure must roll back the final decision.

Time to find due-soon cases in the UI versus a human spreadsheet/email baseline was NOT MEASURED. No improvement percentage, customer satisfaction or real business benefit is claimed. The fixture is small, deterministic and educational; it cannot establish operational reliability, usability or performance under sustained load.
