package dev.billing.reporting.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ReportExportClientTest {
  private final String apiKey = "test-very-long-internal-api-key";
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void httpContractIncludesKeyDateCursorAndParsesJson() {
    WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    server.start();
    try {
      server.stubFor(get(urlPathEqualTo("/internal/v1/disputes/export"))
          .withHeader("X-Internal-Api-Key", equalTo(apiKey))
          .withQueryParam("week", equalTo("2026-11-02"))
          .withQueryParam("cursor", equalTo("0"))
          .withQueryParam("limit", equalTo("100"))
          .willReturn(okJson("{\"items\":[{\"id\":7,\"createdAt\":\"2026-11-02T00:00:00Z\",\"dueAt\":\"2026-11-05T00:00:00Z\",\"resolvedAt\":null,\"approvedRefundAmount\":0}],\"nextCursor\":null}")));
      var client = new ReportExportClient(server.baseUrl(), apiKey, mapper, Duration.ofSeconds(2));
      var result = client.fetch(LocalDate.of(2026, 11, 2), 0);
      assertEquals(7, result.items().get(0).id());
      assertNull(result.nextCursor());
      server.verify(1, getRequestedFor(urlPathEqualTo("/internal/v1/disputes/export")));
    } finally { server.stop(); }
  }

  @Test
  void upstreamUnauthorizedIsNotRetried() {
    WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    server.start();
    try {
      server.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(401)));
      var client = new ReportExportClient(server.baseUrl(), apiKey, mapper, Duration.ofSeconds(2));
      var exception = assertThrows(ResponseStatusException.class, () -> client.fetch(LocalDate.of(2026, 11, 2), 0));
      assertEquals(502, exception.getStatusCode().value());
      server.verify(1, getRequestedFor(anyUrl()));
    } finally { server.stop(); }
  }

  @Test
  void remote500IsRetriedOnlyOnceThenReturns503() {
    WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    server.start();
    try {
      server.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(500)));
      var client = new ReportExportClient(server.baseUrl(), apiKey, mapper, Duration.ofSeconds(2));
      var exception = assertThrows(ResponseStatusException.class, () -> client.fetch(LocalDate.of(2026, 11, 2), 0));
      assertEquals(503, exception.getStatusCode().value());
      server.verify(2, getRequestedFor(anyUrl()));
    } finally { server.stop(); }
  }

  @Test
  void connectionRefusedReturnsClear503() {
    var client = new ReportExportClient("http://127.0.0.1:1", apiKey, mapper, Duration.ofMillis(250));
    var exception = assertThrows(ResponseStatusException.class, () -> client.fetch(LocalDate.of(2026, 11, 2), 0));
    assertEquals(503, exception.getStatusCode().value());
    assertTrue(exception.getReason().contains("unavailable"));
  }
}
