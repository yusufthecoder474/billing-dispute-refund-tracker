package dev.billing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "disputes")
public class Dispute {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public Long customerId;

  public Long assignedSupportId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  public Category category;

  @Column(nullable = false, length = 2000)
  public String description;

  @Column(nullable = false, precision = 12, scale = 2)
  public BigDecimal disputedAmount;

  @Column(nullable = false, length = 3)
  public String currency = "INR";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  public Status status;

  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @Column(nullable = false, updatable = false)
  public Instant dueAt;

  public Instant resolvedAt;

  @Version
  @Column(nullable = false)
  public Long version;
}
