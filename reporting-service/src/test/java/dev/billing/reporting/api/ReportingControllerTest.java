package dev.billing.reporting.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import dev.billing.reporting.security.InternalKey;
import dev.billing.reporting.service.WeeklyReportingService;
import dev.billing.reporting.service.WeeklyReportingService.WeeklyReport;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReportingControllerTest {
  @Test
  void missingKeyIsUnauthorizedAndCorrectKeyReturnsReport() throws Exception {
    WeeklyReportingService service = mock(WeeklyReportingService.class);
    InternalKey guard = new InternalKey("test-very-long-internal-api-key");
    var mvc = MockMvcBuilders.standaloneSetup(new ReportingController(service, guard))
        .setControllerAdvice(new ReportingErrors())
        .setMessageConverters(new MappingJackson2HttpMessageConverter(
            new ObjectMapper().findAndRegisterModules())).build();
    mvc.perform(get("/internal/v1/reports/weekly?week=2026-11-02"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
    when(service.weekly(LocalDate.of(2026, 11, 2))).thenReturn(
        new WeeklyReport(Instant.parse("2026-11-02T00:00:00Z"),
            Instant.parse("2026-11-09T00:00:00Z"), 1, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO));
    mvc.perform(get("/internal/v1/reports/weekly?week=2026-11-02")
            .header("X-Internal-Api-Key", "test-very-long-internal-api-key"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.created").value(1));
  }
}
