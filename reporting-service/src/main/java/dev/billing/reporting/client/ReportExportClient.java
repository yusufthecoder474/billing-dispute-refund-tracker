package dev.billing.reporting.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Calls dispute-service's narrow, versioned export API; contains no database driver or query. */
@Component
public class ReportExportClient {
  public record ExportRow(long id, Instant createdAt, Instant dueAt,
      Instant resolvedAt, BigDecimal approvedRefundAmount) {}
  public record ExportPage(List<ExportRow> items, Long nextCursor) {}

  private final HttpClient http;
  private final ObjectMapper mapper;
  private final String baseUrl;
  private final String key;
  private final Duration timeout;

  @Autowired
  public ReportExportClient(
      @Value("${dispute.base-url:http://127.0.0.1:8080}") String baseUrl,
      @Value("${billing.internal-api-key:}") String key,
      ObjectMapper mapper) {
    this(baseUrl, key, mapper, Duration.ofSeconds(5));
  }
  public ReportExportClient(String baseUrl, String key, ObjectMapper mapper, Duration timeout) {
    this.baseUrl = baseUrl.replaceAll("/+$", "");
    this.key = key;
    this.mapper = mapper;
    this.timeout = timeout;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
  }

  public ExportPage fetch(LocalDate week, long cursor) {
    if (key.length() < 24)
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Internal reporting is not configured.");
    String url = baseUrl + "/internal/v1/disputes/export?week=" + week
        + "&cursor=" + cursor + "&limit=100";
    HttpRequest request = HttpRequest.newBuilder(URI.create(url))
        .header("X-Internal-Api-Key", key).timeout(timeout).GET().build();
    for (int attempt = 0; attempt < 2; attempt++) {
      try {
        HttpResponse<String> result = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() == 200) {
          try { return mapper.readValue(result.body(), ExportPage.class); }
          catch (IOException badJson) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dispute export contract is invalid.");
          }
        }
        if (result.statusCode() >= 400 && result.statusCode() < 500)
          throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dispute export rejected the internal request.");
        // Retry a 5xx once. Only GET is retried.
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dispute service unavailable.");
      } catch (IOException connectionFailure) {
        // One retry for transient connection and read failures; never log the API key.
      }
    }
    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dispute service unavailable.");
  }
}
