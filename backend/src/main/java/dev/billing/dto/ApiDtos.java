package dev.billing.dto;

import dev.billing.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
  private ApiDtos() {}

  public record CreateDispute(
      @NotNull Category category,
      @NotBlank @Size(min = 10, max = 2000) String description,
      @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal disputedAmount) {}

  public record Version(@NotNull @PositiveOrZero Long version) {}

  public record Assign(@NotNull @Positive Long supportId, @NotNull @PositiveOrZero Long version) {}

  public record Decide(
      @NotNull Outcome outcome,
      @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
          BigDecimal approvedRefundAmount,
      @NotBlank @Size(min = 10, max = 2000) String explanation,
      @NotNull @PositiveOrZero Long version) {}

  public record UserView(Long id, String email, String displayName, Role role) {
    public static UserView of(AppUser u) {
      return new UserView(u.id, u.email, u.displayName, u.role);
    }
  }

  public record DecisionView(
      Outcome outcome,
      BigDecimal approvedRefundAmount,
      String explanation,
      Instant createdAt,
      Long managerId) {
    public static DecisionView of(Decision d) {
      return new DecisionView(
          d.outcome, d.approvedRefundAmount, d.explanation, d.createdAt, d.managerId);
    }
  }

  public record DisputeView(
      Long id,
      Long customerId,
      Long assignedSupportId,
      Category category,
      String description,
      BigDecimal disputedAmount,
      String currency,
      Status status,
      Instant createdAt,
      Instant dueAt,
      Instant resolvedAt,
      Long version) {
    public static DisputeView of(Dispute d) {
      return new DisputeView(
          d.id,
          d.customerId,
          d.assignedSupportId,
          d.category,
          d.description,
          d.disputedAmount,
          d.currency,
          d.status,
          d.createdAt,
          d.dueAt,
          d.resolvedAt,
          d.version);
    }
  }

  public record Detail(DisputeView dispute, DecisionView decision) {}

  public record AuditView(
      Long id,
      Long actorId,
      String actor,
      String action,
      Status previousStatus,
      Status newStatus,
      String detail,
      Instant createdAt) {}

  public record PageView<T>(
      List<T> content, int page, int size, long totalElements, int totalPages) {}

  public record SlaSummary(
      long open,
      long dueSoon,
      long overdue,
      long resolved,
      long resolvedWithinSla,
      long resolvedBreached,
      Instant asOf) {}

  public record WeeklyReport(
      Instant start,
      Instant end,
      long created,
      long resolved,
      long withinSla,
      long breached,
      BigDecimal averageResolutionHours,
      BigDecimal approvedRefundTotal) {}

  public record ApiError(int status, String message, Instant timestamp) {}
}
