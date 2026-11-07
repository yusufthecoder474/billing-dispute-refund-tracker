package dev.billing.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true, length = 254)
  public String email;

  @Column(name = "password_hash", nullable = false, length = 100)
  public String passwordHash;

  @Column(name = "display_name", nullable = false, length = 100)
  public String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public Role role;

  @Column(nullable = false)
  public Instant createdAt;
}
