package dev.billing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "dispute_decisions")
public class Decision {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true)
  public Long disputeId;

  @Column(nullable = false)
  public Long managerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public Outcome outcome;

  @Column(nullable = false, precision = 12, scale = 2)
  public BigDecimal approvedRefundAmount;

  @Column(nullable = false, length = 2000)
  public String explanation;

  @Column(nullable = false)
  public Instant createdAt;
}
