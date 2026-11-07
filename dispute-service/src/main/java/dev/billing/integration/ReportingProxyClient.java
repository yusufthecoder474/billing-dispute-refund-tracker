package dev.billing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.billing.dto.ApiDtos.WeeklyReport;
import dev.billing.exception.ApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** The only dispute-service HTTP client for reporting-service. */
@Component
public class ReportingProxyClient {
  private final HttpClient http;
  private final String baseUrl;
  private final String key;
  private final ObjectMapper mapper;

  @Autowired
  public ReportingProxyClient(
      @Value("${reporting.base-url:http://127.0.0.1:8081}") String baseUrl,
      @Value("${billing.internal-api-key:}") String key,
      ObjectMapper mapper) {
    this(baseUrl, key, mapper, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
  }

  // Constructor for deterministic HTTP contract tests.
  ReportingProxyClient(String baseUrl, String key, ObjectMapper mapper, HttpClient http) {
    this.http = http;
    this.baseUrl = baseUrl.replaceAll("/+$", "");
    this.key = key;
    this.mapper = mapper;
  }

  private String request(String path) {
    if (key.length() < 24) throw new ApiException(503, "Reporting is not configured.");
    HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path))
        .header("X-Internal-Api-Key", key)
        .timeout(Duration.ofSeconds(5)).GET().build();
    for (int attempt = 0; attempt < 2; attempt++) {
      try {
        HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) return response.body();
        if (response.statusCode() == 400) throw new ApiException(400, "Invalid week.");
        if (response.statusCode() < 500)
          throw new ApiException(502, "Reporting service rejected the internal request.");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new ApiException(503, "Reporting service is unavailable. Retry later.");
      } catch (IOException e) {
        // Retry one idempotent GET only. Never leak hostnames or credentials to clients.
      }
    }
    throw new ApiException(503, "Reporting service is unavailable. Retry later.");
  }

  public WeeklyReport weekly(LocalDate week) {
    String body = request("/internal/v1/reports/weekly?week=" + week);
    try { return mapper.readValue(body, WeeklyReport.class); }
    catch (IOException e) { throw new ApiException(502, "Invalid response from reporting service."); }
  }

  public String csv(LocalDate week) {
    return request("/internal/v1/reports/weekly.csv?week=" + week);
  }
}
