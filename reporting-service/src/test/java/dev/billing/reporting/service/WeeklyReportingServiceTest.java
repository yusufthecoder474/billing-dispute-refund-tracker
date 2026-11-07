package dev.billing.reporting.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import dev.billing.reporting.client.ReportExportClient;
import dev.billing.reporting.client.ReportExportClient.ExportPage;
import dev.billing.reporting.client.ReportExportClient.ExportRow;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class WeeklyReportingServiceTest {
  private static final LocalDate WEEK = LocalDate.of(2026, 11, 2);
  private final ReportExportClient client = mock(ReportExportClient.class);
  private final WeeklyReportingService service = new WeeklyReportingService(client);
  private Instant at(String s) { return Instant.parse(s); }

  @Test
  void aggregatesCreatedResolvedSlaAndRefundsAcrossPages() {
    when(client.fetch(WEEK, 0L)).thenReturn(new ExportPage(List.of(
        new ExportRow(1, at("2026-11-02T00:00:00Z"), at("2026-11-05T00:00:00Z"),
            at("2026-11-02T02:00:00Z"), new BigDecimal("50.00")),
        new ExportRow(2, at("2026-11-01T00:00:00Z"), at("2026-11-04T00:00:00Z"),
            at("2026-11-03T00:00:00Z"), new BigDecimal("0.00"))), 2L));
    when(client.fetch(WEEK, 2L)).thenReturn(new ExportPage(List.of(
        new ExportRow(3, at("2026-10-30T00:00:00Z"), at("2026-11-02T00:00:00Z"),
            at("2026-11-04T00:00:00Z"), new BigDecimal("10.50"))), null));
    var r = service.weekly(WEEK);
    assertEquals(1, r.created());
    assertEquals(3, r.resolved());
    assertEquals(2, r.withinSla());
    assertEquals(1, r.breached());
    assertEquals(new BigDecimal("56.67"), r.averageResolutionHours());
    assertEquals(new BigDecimal("60.50"), r.approvedRefundTotal());
    assertTrue(service.csv(r).contains("60.50"));
    verify(client).fetch(WEEK, 2L);
  }

  @Test
  void emptyWeekUsesZeroMetricsAndNoFurtherCalls() {
    when(client.fetch(WEEK, 0L)).thenReturn(new ExportPage(List.of(), null));
    var r = service.weekly(WEEK);
    assertEquals(0, r.created());
    assertEquals(0, r.resolved());
    assertEquals(new BigDecimal("0.00"), r.averageResolutionHours());
    assertEquals(new BigDecimal("0.00"), r.approvedRefundTotal());
    verify(client, times(1)).fetch(WEEK, 0L);
  }

  @Test
  void nonMondayFailsBeforeNetworkCall() {
    assertThrows(ResponseStatusException.class, () -> service.weekly(WEEK.plusDays(1)));
    verifyNoInteractions(client);
  }

  @Test
  void nonAdvancingPageCursorIsRejected() {
    when(client.fetch(WEEK, 0L)).thenReturn(new ExportPage(List.of(
        new ExportRow(1, at("2026-11-02T00:00:00Z"), at("2026-11-05T00:00:00Z"),
            null, BigDecimal.ZERO)), 0L));
    assertThrows(ResponseStatusException.class, () -> service.weekly(WEEK));
  }

  @Test
  void repeatedOrDisorderedRowsAreRejected() {
    when(client.fetch(WEEK, 0L)).thenReturn(new ExportPage(List.of(
        new ExportRow(3, at("2026-11-02T00:00:00Z"), at("2026-11-05T00:00:00Z"), null, BigDecimal.ZERO),
        new ExportRow(2, at("2026-11-03T00:00:00Z"), at("2026-11-06T00:00:00Z"), null, BigDecimal.ZERO)), null));
    assertThrows(ResponseStatusException.class, () -> service.weekly(WEEK));
  }
}
