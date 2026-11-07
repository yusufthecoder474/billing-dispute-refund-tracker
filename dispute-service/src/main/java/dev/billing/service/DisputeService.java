package dev.billing.service;

import dev.billing.domain.*;
import dev.billing.dto.ApiDtos.*;
import dev.billing.exception.ApiException;
import dev.billing.repository.*;
import jakarta.persistence.criteria.Predicate;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DisputeService {
  private final DisputeRepository disputes;
  private final DecisionRepository decisions;
  private final AuditRepository audits;
  private final UserRepository users;
  private final Clock clock;

  public DisputeService(
      DisputeRepository d, DecisionRepository x, AuditRepository a, UserRepository u, Clock c) {
    disputes = d;
    decisions = x;
    audits = a;
    users = u;
    clock = c;
  }

  public AppUser actor(String email) {
    return users.findByEmail(email).orElseThrow(() -> new ApiException(401, "Sign in required."));
  }

  private Dispute get(Long id) {
    return disputes.findById(id).orElseThrow(() -> new ApiException(404, "Dispute not found."));
  }

  private void visible(Dispute d, AppUser u) {
    if (u.role == Role.CUSTOMER && !u.id.equals(d.customerId)
        || u.role == Role.SUPPORT && !u.id.equals(d.assignedSupportId))
      throw new ApiException(403, "Access denied.");
  }

  private void audit(Dispute d, AppUser u, String action, Status before, String detail) {
    AuditEvent e = new AuditEvent();
    e.disputeId = d.id;
    e.actorId = u.id;
    e.action = action;
    e.previousStatus = before;
    e.newStatus = d.status;
    e.detail = detail;
    e.createdAt = clock.instant();
    audits.save(e);
  }

  @Transactional
  public DisputeView create(AppUser u, CreateDispute request) {
    Rules.role(u, Role.CUSTOMER);
    Dispute d = new Dispute();
    d.customerId = u.id;
    d.category = request.category();
    d.description = request.description().strip();
    if (d.description.length() < 10)
      throw new ApiException(400, "Description requires at least 10 non-padding characters.");
    d.disputedAmount = request.disputedAmount();
    d.status = Status.OPEN;
    d.createdAt = clock.instant();
    d.dueAt = d.createdAt.plus(Duration.ofHours(72));
    disputes.saveAndFlush(d);
    audit(d, u, "CREATED", null, "Dispute submitted");
    return DisputeView.of(d);
  }

  public Detail detail(Long id, AppUser u) {
    Dispute d = get(id);
    visible(d, u);
    return new Detail(
        DisputeView.of(d), decisions.findByDisputeId(id).map(DecisionView::of).orElse(null));
  }

  public PageView<DisputeView> list(
      AppUser u, Status status, String sla, int page, int size, String sort, String direction) {
    if (!Set.of("createdAt", "dueAt", "status").contains(sort)
        || !Set.of("asc", "desc").contains(direction)
        || page < 0
        || size < 1
        || size > 100) throw new ApiException(400, "Invalid pagination or sorting.");
    if (sla != null && !Set.of("DUE_SOON", "OVERDUE").contains(sla))
      throw new ApiException(400, "Invalid SLA filter.");
    Instant now = clock.instant();
    Specification<Dispute> spec =
        (r, q, b) -> {
          List<Predicate> p = new ArrayList<>();
          if (u.role == Role.CUSTOMER) p.add(b.equal(r.get("customerId"), u.id));
          if (u.role == Role.SUPPORT) p.add(b.equal(r.get("assignedSupportId"), u.id));
          if (status != null) p.add(b.equal(r.get("status"), status));
          if (sla != null) {
            p.add(b.isNull(r.get("resolvedAt")));
            if (sla.equals("OVERDUE")) p.add(b.lessThan(r.get("dueAt"), now));
            else {
              p.add(b.greaterThanOrEqualTo(r.get("dueAt"), now));
              p.add(b.lessThanOrEqualTo(r.get("dueAt"), now.plus(Duration.ofHours(12))));
            }
          }
          return b.and(p.toArray(Predicate[]::new));
        };
    Page<Dispute> result =
        disputes.findAll(
            spec,
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(direction), sort).and(Sort.by("id"))));
    return new PageView<>(
        result.map(DisputeView::of).getContent(),
        page,
        size,
        result.getTotalElements(),
        result.getTotalPages());
  }

  @Transactional
  public DisputeView assign(Long id, AppUser u, Assign r) {
    Rules.role(u, Role.MANAGER);
    Dispute d = get(id);
    Rules.version(d, r.version());
    Rules.state(d, Status.OPEN);
    AppUser support =
        users
            .findById(r.supportId())
            .orElseThrow(() -> new ApiException(404, "Support agent not found."));
    if (support.role != Role.SUPPORT)
      throw new ApiException(400, "Assignment requires a support account.");
    if (r.supportId().equals(d.assignedSupportId))
      throw new ApiException(409, "Already assigned to this support agent.");
    d.assignedSupportId = support.id;
    disputes.flush();
    audit(d, u, "ASSIGNED", d.status, "Assigned to " + support.displayName);
    return DisputeView.of(d);
  }

  @Transactional
  public DisputeView transition(Long id, AppUser u, Version r, boolean start) {
    Dispute d = get(id);
    Rules.assigned(d, u);
    Rules.version(d, r.version());
    Status before = start ? Status.OPEN : Status.IN_REVIEW;
    Rules.state(d, before);
    d.status = start ? Status.IN_REVIEW : Status.PENDING_DECISION;
    disputes.flush();
    audit(d, u, start ? "REVIEW_STARTED" : "SUBMITTED_FOR_DECISION", before, "Workflow advanced");
    return DisputeView.of(d);
  }

  @Transactional
  public DisputeView decide(Long id, AppUser u, Decide r) {
    Rules.role(u, Role.MANAGER);
    Dispute d = get(id);
    Rules.version(d, r.version());
    Rules.state(d, Status.PENDING_DECISION);
    Rules.refund(d.disputedAmount, r.approvedRefundAmount(), r.outcome());
    if (r.explanation().strip().length() < 10)
      throw new ApiException(400, "Explanation requires at least 10 non-padding characters.");
    d.status =
        r.outcome() == Outcome.APPROVED ? Status.RESOLVED_APPROVED : Status.RESOLVED_REJECTED;
    d.resolvedAt = clock.instant();
    // Flush the versioned row first: concurrent losers roll back before adding a decision or audit.
    disputes.flush();
    Decision x = new Decision();
    x.disputeId = id;
    x.managerId = u.id;
    x.outcome = r.outcome();
    x.approvedRefundAmount = r.approvedRefundAmount();
    x.explanation = r.explanation().strip();
    x.createdAt = d.resolvedAt;
    decisions.saveAndFlush(x);
    audit(
        d,
        u,
        r.outcome().name(),
        Status.PENDING_DECISION,
        "Simulated refund approval: INR " + r.approvedRefundAmount());
    return DisputeView.of(d);
  }

  public PageView<AuditView> audit(Long id, AppUser u, int page, int size) {
    visible(get(id), u);
    if (page < 0 || size < 1 || size > 100) throw new ApiException(400, "Invalid pagination.");
    Page<AuditEvent> result =
        audits.findByDisputeIdOrderByCreatedAtAscIdAsc(id, PageRequest.of(page, size));
    return new PageView<>(
        result.stream()
            .map(
                e ->
                    new AuditView(
                        e.id,
                        e.actorId,
                        users.findById(e.actorId).orElseThrow().displayName,
                        e.action,
                        e.previousStatus,
                        e.newStatus,
                        e.detail,
                        e.createdAt))
            .toList(),
        page,
        size,
        result.getTotalElements(),
        result.getTotalPages());
  }

  public List<UserView> support(AppUser u) {
    Rules.role(u, Role.MANAGER);
    return users.findByRoleOrderByDisplayName(Role.SUPPORT).stream().map(UserView::of).toList();
  }
}
