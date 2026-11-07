package dev.billing.integration;

import dev.billing.service.Rules;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import dev.billing.exception.ApiException;

/** Internal minimal-data export, accessible only with the shared service credential. */
@RestController
@RequestMapping("/internal/v1/disputes")
@SecurityRequirement(name = "internalKey")
public class ReportExportController {
  private final JdbcTemplate jdbc;
  private final InternalKey key;
  public ReportExportController(JdbcTemplate jdbc, InternalKey key) {
    this.jdbc = jdbc;
    this.key = key;
  }

  public record ExportRow(long id, Instant createdAt, Instant dueAt,
      Instant resolvedAt, BigDecimal approvedRefundAmount) {}
  public record ExportPage(List<ExportRow> items, Long nextCursor) {}

  @GetMapping("/export")
  public ExportPage export(
      @RequestHeader(value = "X-Internal-Api-Key", required = false) String supplied,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
      @RequestParam(defaultValue = "0") long cursor,
      @RequestParam(defaultValue = "100") int limit) {
    key.require(supplied);
    if (cursor < 0 || limit < 1 || limit > 100)
      throw new ApiException(400, "Cursor and limit must be within supported bounds.");
    Instant start = Rules.weekStart(week);
    Instant end = start.plus(java.time.Duration.ofDays(7));
    Timestamp a = Timestamp.from(start), b = Timestamp.from(end);
    // Keyset pagination is stable by ID; concurrent updates can still alter membership.
    List<ExportRow> rows = jdbc.query(
        """
        SELECT d.id, d.created_at, d.due_at, d.resolved_at,
               COALESCE(x.approved_refund_amount, 0) AS approved_refund_amount
        FROM disputes d LEFT JOIN dispute_decisions x ON x.dispute_id = d.id
        WHERE d.id > ? AND
              ((d.created_at >= ? AND d.created_at < ?) OR
               (d.resolved_at >= ? AND d.resolved_at < ?))
        ORDER BY d.id LIMIT ?
        """,
        (rs, n) -> new ExportRow(rs.getLong("id"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("due_at").toInstant(),
            rs.getTimestamp("resolved_at") == null ? null : rs.getTimestamp("resolved_at").toInstant(),
            rs.getBigDecimal("approved_refund_amount")),
        cursor, a, b, a, b, limit + 1);
    boolean more = rows.size() > limit;
    List<ExportRow> page = more ? rows.subList(0, limit) : rows;
    return new ExportPage(page, more ? page.get(page.size() - 1).id() : null);
  }
}
