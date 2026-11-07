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

}
