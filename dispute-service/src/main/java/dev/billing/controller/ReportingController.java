package dev.billing.controller;

import dev.billing.dto.ApiDtos.SlaSummary;
import dev.billing.service.ReportingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('MANAGER')")
public class ReportingController {
  private final ReportingService service;
  public ReportingController(ReportingService service) { this.service = service; }

  @GetMapping("/sla/summary")
  public SlaSummary summary() { return service.summary(); }
}
