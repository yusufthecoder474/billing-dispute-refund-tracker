package dev.billing.controller;

import dev.billing.dto.ApiDtos.*;
import dev.billing.service.ReportingService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('MANAGER')")
public class ReportingController {
  private final ReportingService service;

  public ReportingController(ReportingService service) {
    this.service = service;
  }

  @GetMapping("/sla/summary")
  public SlaSummary summary() {
    return service.summary();
  }

  @GetMapping("/reports/weekly")
  public WeeklyReport weekly(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
    return service.weekly(week);
  }

  @GetMapping(value = "/reports/weekly.csv", produces = "text/csv")
  public ResponseEntity<String> csv(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=weekly-" + week + ".csv")
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .body(service.csv(service.weekly(week)));
  }
}
