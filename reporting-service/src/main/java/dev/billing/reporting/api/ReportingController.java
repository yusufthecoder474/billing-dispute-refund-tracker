package dev.billing.reporting.api;

import dev.billing.reporting.security.InternalKey;
import dev.billing.reporting.service.WeeklyReportingService;
import dev.billing.reporting.service.WeeklyReportingService.WeeklyReport;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class ReportingController {
  private final WeeklyReportingService service;
  private final InternalKey key;
  public ReportingController(WeeklyReportingService service, InternalKey key) {
    this.service = service; this.key = key;
  }

  @GetMapping("/api/health")
  public Map<String, String> health() { return Map.of("status", "UP"); }

  @SecurityRequirement(name = "internalKey")
  @GetMapping("/internal/v1/reports/weekly")
  public WeeklyReport weekly(
      @RequestHeader(value = "X-Internal-Api-Key", required = false) String supplied,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
    key.require(supplied);
    return service.weekly(week);
  }

  @SecurityRequirement(name = "internalKey")
  @GetMapping(value = "/internal/v1/reports/weekly.csv", produces = "text/csv")
  public ResponseEntity<String> csv(
      @RequestHeader(value = "X-Internal-Api-Key", required = false) String supplied,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
    key.require(supplied);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=weekly-" + week + ".csv")
        .body(service.csv(service.weekly(week)));
  }
}
