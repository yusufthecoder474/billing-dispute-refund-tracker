package dev.billing.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, updatable = false)
  public Long disputeId;

  @Column(nullable = false, updatable = false)
  public Long actorId;

  @Column(nullable = false, updatable = false, length = 40)
  public String action;

  @Enumerated(EnumType.STRING)
  @Column(updatable = false, length = 30)
  public Status previousStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false, length = 30)
  public Status newStatus;

  @Column(nullable = false, updatable = false, length = 500)
  public String detail;

  @Column(nullable = false, updatable = false)
  public Instant createdAt;
}
