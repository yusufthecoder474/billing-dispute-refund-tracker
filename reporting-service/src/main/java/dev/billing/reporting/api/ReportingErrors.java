package dev.billing.reporting.api;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ReportingErrors {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String, Object>> known(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode()).body(Map.of(
        "status", e.getStatusCode().value(), "message", e.getReason() == null ? "Request failed" : e.getReason(),
        "timestamp", Instant.now().toString()));
  }
}
