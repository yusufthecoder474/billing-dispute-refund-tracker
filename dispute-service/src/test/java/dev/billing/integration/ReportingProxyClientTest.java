package dev.billing.integration;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.billing.exception.ApiException;
import java.net.http.HttpClient;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReportingProxyClientTest {
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void unavailableReportingServiceReturns503NotEmptyReport() {
    var proxy = new ReportingProxyClient("http://127.0.0.1:1", "test-very-long-internal-api-key",
        mapper, HttpClient.newHttpClient());
    ApiException error = assertThrows(ApiException.class,
        () -> proxy.weekly(LocalDate.of(2026, 11, 2)));
    assertEquals(503, error.status);
    assertTrue(error.getMessage().contains("unavailable"));
  }

  @Test
  void missingServiceKeyFailsClosed() {
    var proxy = new ReportingProxyClient("http://127.0.0.1:1", "", mapper,
        HttpClient.newHttpClient());
    ApiException error = assertThrows(ApiException.class,
        () -> proxy.weekly(LocalDate.of(2026, 11, 2)));
    assertEquals(503, error.status);
  }
}
