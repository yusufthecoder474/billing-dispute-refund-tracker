package dev.billing.reporting.service;

import dev.billing.reporting.client.ReportExportClient;
import dev.billing.reporting.client.ReportExportClient.ExportPage;
import dev.billing.reporting.client.ReportExportClient.ExportRow;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Pure Java weekly aggregation over a restricted REST export. No SQL or shared database access. */
@Service
public class WeeklyReportingService {
  public record WeeklyReport(Instant start, Instant end, long created, long resolved,
      long withinSla, long breached, BigDecimal averageResolutionHours,
      BigDecimal approvedRefundTotal) {}

  private final ReportExportClient client;
  public WeeklyReportingService(ReportExportClient client) { this.client = client; }

  public WeeklyReport weekly(LocalDate monday) {
    if (monday == null || monday.getDayOfWeek() != DayOfWeek.MONDAY)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Week must begin on a Monday (UTC).");
    Instant start = monday.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = start.plus(Duration.ofDays(7));
    long created = 0, resolved = 0, within = 0, breached = 0;
    BigDecimal totalRefunds = BigDecimal.ZERO;
    BigDecimal totalSeconds = BigDecimal.ZERO;
    long cursor = 0;
    boolean finished = false;
    for (int page = 0; page < 200; page++) {
      ExportPage response = client.fetch(monday, cursor);
      if (response == null || response.items() == null)
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dispute export is incomplete.");
      List<ExportRow> items = response.items();
      if (items.size() > 100)
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dispute export exceeds page limit.");
      long highest = cursor;
      for (ExportRow r : items) {
        if (r == null || r.id() <= highest || r.createdAt() == null || r.dueAt() == null ||
            r.approvedRefundAmount() == null)
          throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dispute export contains invalid rows.");
        highest = r.id();
        if (!r.createdAt().isBefore(start) && r.createdAt().isBefore(end)) created++;
        if (r.resolvedAt() != null && !r.resolvedAt().isBefore(start) && r.resolvedAt().isBefore(end)) {
          resolved++;
          if (r.resolvedAt().isAfter(r.dueAt())) breached++; else within++;
          totalRefunds = totalRefunds.add(r.approvedRefundAmount());
          Duration d = Duration.between(r.createdAt(), r.resolvedAt());
          totalSeconds = totalSeconds.add(BigDecimal.valueOf(d.getSeconds())
              .add(BigDecimal.valueOf(d.getNano(), 9)));
        }
      }
      if (response.nextCursor() == null) { finished = true; break; }
      if (items.isEmpty() || response.nextCursor() != highest)
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dispute export cursor did not advance.");
      cursor = highest;
    }
    if (!finished) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Report exceeded the configured 20,000-row export limit.");
    BigDecimal averageHours = resolved == 0 ? BigDecimal.ZERO.setScale(2) :
        totalSeconds.divide(BigDecimal.valueOf(resolved).multiply(BigDecimal.valueOf(3600)),
            2, RoundingMode.HALF_UP);
    return new WeeklyReport(start, end, created, resolved, within, breached,
        averageHours, totalRefunds.setScale(2, RoundingMode.HALF_UP));
  }

  public String csv(WeeklyReport r) {
    return "start_utc,end_utc,created,resolved,within_sla,breached,average_resolution_hours,approved_refund_inr\r\n"
        + Stream.of(r.start(), r.end(), r.created(), r.resolved(), r.withinSla(), r.breached(),
            r.averageResolutionHours(), r.approvedRefundTotal())
            .map(x -> quote(x.toString())).collect(Collectors.joining(",")) + "\r\n";
  }

  private String quote(String value) {
    String stripped = value.stripLeading();
    if ((!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0)) >= 0) ||
        value.startsWith("\t") || value.startsWith("\r") || value.startsWith("\n"))
      value = "'" + value;
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }
}
