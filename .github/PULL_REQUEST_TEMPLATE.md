## Purpose
Explain the behavior being changed and why.

## Related requirement
Link the F1–F6 requirement and relevant issue, if one exists.

## Changes
Describe the implementation and any migration or API changes.

## Test evidence
List commands, results and report links. Attach real screenshots for UI changes.

## Risk assessment
Explain affected permissions, data, compatibility and rollback approach.

## Reviewer checklist
- [ ] Correct behavior and allowed state transitions
- [ ] Server authorization, CSRF and input validation
- [ ] SQL queries, indexes and pagination
- [ ] Transaction boundaries and concurrent updates
- [ ] Safe errors without secrets
- [ ] Meaningful tests and visible failures
- [ ] Clear names and readable code
- [ ] Documentation updated
- [ ] Backward compatibility and migrations reviewed
