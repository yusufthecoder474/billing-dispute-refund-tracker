package dev.billing.controller;

import dev.billing.dto.ApiDtos.WeeklyReport;
import dev.billing.integration.ReportingProxyClient;
import dev.billing.service.Rules;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Public manager-only compatibility layer; existing React routes stay unchanged. */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('MANAGER')")
public class WeeklyReportProxyController {
  private final ReportingProxyClient client;
  public WeeklyReportProxyController(ReportingProxyClient client) { this.client = client; }

  @GetMapping("/weekly")
  public WeeklyReport weekly(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
    Rules.weekStart(week);
    return client.weekly(week);
  }

  @GetMapping(value = "/weekly.csv", produces = "text/csv")
  public ResponseEntity<String> csv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
    Rules.weekStart(week);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=weekly-" + week + ".csv")
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .body(client.csv(week));
  }
}
