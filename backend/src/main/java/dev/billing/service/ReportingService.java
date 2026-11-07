package dev.billing.service;

import dev.billing.dto.ApiDtos.*;
import java.sql.Timestamp;
import java.time.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportingService {
  private final JdbcTemplate jdbc;
  private final Clock clock;

  public ReportingService(JdbcTemplate jdbc, Clock clock) {
    this.jdbc = jdbc;
    this.clock = clock;
  }

  public SlaSummary summary() {
    Instant now = clock.instant();
    return jdbc.queryForObject(
        """
        SELECT count(*) FILTER (WHERE resolved_at IS NULL) AS open,
         count(*) FILTER (WHERE resolved_at IS NULL AND due_at >= ? AND due_at <= ?) AS soon,
         count(*) FILTER (WHERE resolved_at IS NULL AND due_at < ?) AS overdue,
         count(*) FILTER (WHERE resolved_at IS NOT NULL) AS resolved,
         count(*) FILTER (WHERE resolved_at <= due_at) AS within_sla,
         count(*) FILTER (WHERE resolved_at > due_at) AS breached FROM disputes
        """,
        (rs, n) ->
            new SlaSummary(
                rs.getLong("open"),
                rs.getLong("soon"),
                rs.getLong("overdue"),
                rs.getLong("resolved"),
                rs.getLong("within_sla"),
                rs.getLong("breached"),
                now),
        Timestamp.from(now),
        Timestamp.from(now.plus(Duration.ofHours(12))),
        Timestamp.from(now));
  }

  public WeeklyReport weekly(LocalDate monday) {
    Instant start = Rules.weekStart(monday), end = start.plus(Duration.ofDays(7));
    return jdbc.queryForObject(
        """
        SELECT (SELECT count(*) FROM disputes WHERE created_at >= ? AND created_at < ?) AS created,
         count(*) AS resolved, count(*) FILTER (WHERE d.resolved_at <= d.due_at) AS within_sla,
         count(*) FILTER (WHERE d.resolved_at > d.due_at) AS breached,
         coalesce(round(avg(extract(epoch FROM (d.resolved_at-d.created_at))/3600)::numeric,2),0) AS average_hours,
         coalesce(sum(x.approved_refund_amount),0) AS refunds
        FROM disputes d LEFT JOIN dispute_decisions x ON x.dispute_id=d.id
        WHERE d.resolved_at >= ? AND d.resolved_at < ?
        """,
        (rs, n) ->
            new WeeklyReport(
                start,
                end,
                rs.getLong("created"),
                rs.getLong("resolved"),
                rs.getLong("within_sla"),
                rs.getLong("breached"),
                rs.getBigDecimal("average_hours"),
                rs.getBigDecimal("refunds")),
        Timestamp.from(start),
        Timestamp.from(end),
        Timestamp.from(start),
        Timestamp.from(end));
  }

  public String csv(WeeklyReport r) {
    return "start_utc,end_utc,created,resolved,within_sla,breached,average_resolution_hours,approved_refund_inr\r\n"
        + java.util.stream.Stream.of(
                r.start(),
                r.end(),
                r.created(),
                r.resolved(),
                r.withinSla(),
                r.breached(),
                r.averageResolutionHours(),
                r.approvedRefundTotal())
            .map(x -> Rules.csv(x.toString()))
            .collect(java.util.stream.Collectors.joining(","))
        + "\r\n";
  }
}
