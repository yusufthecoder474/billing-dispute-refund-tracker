package dev.billing.service;

import dev.billing.domain.*;
import dev.billing.exception.ApiException;
import java.math.BigDecimal;
import java.time.*;

public final class Rules {
  private Rules() {}

  public static void role(AppUser u, Role role) {
    if (u.role != role) throw new ApiException(403, "Role not allowed.");
  }

  public static void version(Dispute d, Long version) {
    if (version == null || !version.equals(d.version))
      throw new ApiException(409, "Stale version: reload the dispute.");
  }

  public static void state(Dispute d, Status expected) {
    if (d.status != expected)
      throw new ApiException(
          409, "Action requires " + expected + "; current status is " + d.status + ".");
  }

  public static void assigned(Dispute d, AppUser actor) {
    role(actor, Role.SUPPORT);
    if (!actor.id.equals(d.assignedSupportId))
      throw new ApiException(403, "Only the assigned support agent can process this dispute.");
  }

  public static void refund(BigDecimal disputed, BigDecimal refund, Outcome outcome) {
    if (refund == null
        || refund.signum() < 0
        || refund.scale() > 2
        || refund.compareTo(disputed) > 0)
      throw new ApiException(
          400,
          "Refund must be between zero and the disputed amount, with at most two decimal places.");
    if (outcome == Outcome.REJECTED && refund.signum() != 0)
      throw new ApiException(400, "Rejected disputes require a zero refund.");
  }

  public static String sla(Instant due, Instant resolved, Instant now) {
    if (resolved != null) return resolved.isAfter(due) ? "BREACHED" : "WITHIN_SLA";
    if (now.isAfter(due)) return "OVERDUE";
    return !due.isAfter(now.plus(Duration.ofHours(12))) ? "DUE_SOON" : "OPEN";
  }

  public static Instant weekStart(LocalDate monday) {
    if (monday.getDayOfWeek() != DayOfWeek.MONDAY)
      throw new ApiException(400, "Week must start on a Monday (UTC).");
    return monday.atStartOfDay(ZoneOffset.UTC).toInstant();
  }

  public static String csv(String value) {
    String v = value == null ? "" : value;
    String stripped = v.stripLeading();
    if (!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0)) >= 0
        || v.startsWith("\t")
        || v.startsWith("\r")
        || v.startsWith("\n")) v = "'" + v;
    return "\"" + v.replace("\"", "\"\"") + "\"";
  }
}
