package dev.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import dev.billing.config.*;
import dev.billing.domain.*;
import dev.billing.repository.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowIT {
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine");

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry r) {
    String url = System.getenv("TEST_DB_URL");
    if (url == null) {
      POSTGRES.start();
      r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
      r.add("spring.datasource.username", POSTGRES::getUsername);
      r.add("spring.datasource.password", POSTGRES::getPassword);
    } else {
      r.add("spring.datasource.url", () -> url);
      r.add(
          "spring.datasource.username",
          () -> System.getenv().getOrDefault("TEST_DB_USER", "postgres"));
      r.add(
          "spring.datasource.password", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD", ""));
    }
  }

  @TestConfiguration
  static class TimeConfig {
    @Bean
    @Primary
    Clock fixedClock() {
      return Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC);
    }
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired UserRepository users;
  @Autowired DecisionRepository decisions;
  @Autowired AuditRepository audits;

  @BeforeEach
  void clear() {
    jdbc.execute("TRUNCATE audit_events, dispute_decisions, disputes RESTART IDENTITY CASCADE");
  }

  Long userId(String name) {
    return users.findByEmail(name + "@demo.local").orElseThrow().id;
  }

  org.springframework.test.web.servlet.request.RequestPostProcessor actor(String name) {
    return user(name + "@demo.local")
        .roles(
            name.startsWith("customer")
                ? "CUSTOMER"
                : name.startsWith("support") ? "SUPPORT" : "MANAGER");
  }

  JsonNode body(MvcResult r) throws Exception {
    return json.readTree(r.getResponse().getContentAsString());
  }

  MvcResult mutate(long id, String action, String who, String content, int expected)
      throws Exception {
    return mvc.perform(
            post("/api/disputes/" + id + "/" + action)
                .with(actor(who))
                .with(csrf())
                .contentType("application/json")
                .content(content))
        .andExpect(status().is(expected))
        .andReturn();
  }

  long create() throws Exception {
    return body(mvc.perform(
                post("/api/disputes")
                    .with(actor("customer"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        "{\"category\":\"DUPLICATE_CHARGE\",\"description\":\"Duplicate monthly"
                            + " charge\",\"disputedAmount\":100.00}"))
            .andExpect(status().isCreated())
            .andReturn())
        .get("id")
        .asLong();
  }

  void assign(long id) throws Exception {
    mutate(id, "assign", "manager", "{\"supportId\":" + userId("support") + ",\"version\":0}", 200);
  }

  long pending() throws Exception {
    long id = create();
    assign(id);
    mutate(id, "start-review", "support", "{\"version\":1}", 200);
    mutate(id, "submit-for-decision", "support", "{\"version\":2}", 200);
    return id;
  }

  String decision(String outcome, String amount, long version) {
    return "{\"outcome\":\""
        + outcome
        + "\",\"approvedRefundAmount\":"
        + amount
        + ",\"explanation\":\"Verified synthetic billing record\",\"version\":"
        + version
        + "}";
  }

  @Test
  void createsPersistsAndAudits() throws Exception {
    long id = create();
    assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM disputes", Integer.class));
    assertEquals(1, audits.countByDisputeIdAndAction(id, "CREATED"));
    assertEquals(
        72,
        jdbc.queryForObject(
            "SELECT extract(epoch from (due_at-created_at))/3600 FROM disputes WHERE id=?",
            Integer.class,
            id));
  }

  @Test
  void invalidAmount() throws Exception {
    mvc.perform(
            post("/api/disputes")
                .with(actor("customer"))
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"category\":\"UNEXPECTED_FEE\",\"description\":\"Incorrect negative"
                        + " charge\",\"disputedAmount\":-1}"))
        .andExpect(status().isBadRequest());
    assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM disputes", Integer.class));
  }

  @Test
  void wrongCustomerCannotRead() throws Exception {
    long id = create();
    mvc.perform(get("/api/disputes/" + id).with(actor("customer2")))
        .andExpect(status().isForbidden());
  }

  @Test
  void customerListIsScoped() throws Exception {
    create();
    mvc.perform(get("/api/disputes").with(actor("customer2")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void supportUnassignedForbidden() throws Exception {
    long id = create();
    mutate(id, "start-review", "support", "{\"version\":0}", 403);
    mvc.perform(get("/api/disputes/" + id).with(actor("support")))
        .andExpect(status().isForbidden());
  }

  @Test
  void assignedQueue() throws Exception {
    long id = create();
    assign(id);
    mvc.perform(get("/api/disputes").with(actor("support")))
        .andExpect(jsonPath("$.totalElements").value(1));
    mvc.perform(get("/api/disputes").with(actor("support2")))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void wrongRoleAssignment() throws Exception {
    long id = create();
    mutate(
        id, "assign", "customer", "{\"supportId\":" + userId("support") + ",\"version\":0}", 403);
  }

  @Test
  void assignmentRequiresSupportRole() throws Exception {
    long id = create();
    mutate(
        id, "assign", "manager", "{\"supportId\":" + userId("customer") + ",\"version\":0}", 400);
  }

  @Test
  void invalidTransition() throws Exception {
    long id = create();
    assign(id);
    mutate(id, "submit-for-decision", "support", "{\"version\":1}", 409);
  }

  @Test
  void approvalAtomicAndImmutable() throws Exception {
    long id = pending();
    mutate(id, "decision", "manager", decision("APPROVED", "75.50", 3), 200);
    assertEquals(1, decisions.countByDisputeId(id));
    assertEquals(1, audits.countByDisputeIdAndAction(id, "APPROVED"));
    var actions =
        jdbc.queryForList(
            "SELECT action FROM audit_events WHERE dispute_id=? ORDER BY created_at,id",
            String.class,
            id);
    assertEquals(
        List.of("CREATED", "ASSIGNED", "REVIEW_STARTED", "SUBMITTED_FOR_DECISION", "APPROVED"),
        actions);
    Path auditOutput = Path.of("target/evaluation/audit.json");
    Files.createDirectories(auditOutput.getParent());
    Files.writeString(
        auditOutput,
        json.writeValueAsString(
            Map.of("expectedEvents", 5, "observedEvents", actions.size(), "actions", actions)));

    assertEquals(
        5,
        jdbc.queryForObject(
            "SELECT count(*) FROM audit_events WHERE dispute_id=?", Integer.class, id));
    mutate(
        id, "assign", "manager", "{\"supportId\":" + userId("support2") + ",\"version\":4}", 409);
    mvc.perform(get("/api/disputes/" + id).with(actor("customer")))
        .andExpect(jsonPath("$.decision.approvedRefundAmount").value(75.5));
  }

  @Test
  void refundExceedsDispute() throws Exception {
    long id = pending();
    mutate(id, "decision", "manager", decision("APPROVED", "100.01", 3), 400);
    assertEquals(0, decisions.countByDisputeId(id));
  }

  @Test
  void negativeRefund() throws Exception {
    long id = pending();
    mutate(id, "decision", "manager", decision("APPROVED", "-1", 3), 400);
  }

  @Test
  void rejectRequiresZero() throws Exception {
    long id = pending();
    mutate(id, "decision", "manager", decision("REJECTED", "1", 3), 400);
    mutate(id, "decision", "manager", decision("REJECTED", "0", 3), 200);
    assertEquals(
        "RESOLVED_REJECTED",
        jdbc.queryForObject("SELECT status FROM disputes WHERE id=?", String.class, id));
  }

  @Test
  void repeatedDecisionConflict() throws Exception {
    long id = pending();
    mutate(id, "decision", "manager", decision("APPROVED", "50", 3), 200);
    mutate(id, "decision", "manager", decision("APPROVED", "50", 3), 409);
    mutate(id, "decision", "manager", decision("APPROVED", "50", 4), 409);
    assertEquals(1, decisions.countByDisputeId(id));
  }

  @Test
  void staleAssignmentConflict() throws Exception {
    long id = create();
    assign(id);
    mutate(
        id, "assign", "manager", "{\"supportId\":" + userId("support2") + ",\"version\":0}", 409);
  }

  @Test
  void concurrentManagersOnlyOneCommit() throws Exception {
    long id = pending();
    CountDownLatch ready = new CountDownLatch(2), go = new CountDownLatch(1);
    try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
      List<Future<Integer>> futures = new ArrayList<>();
      for (String name : List.of("manager", "manager2"))
        futures.add(
            pool.submit(
                () -> {
                  ready.countDown();
                  if (!go.await(10, TimeUnit.SECONDS))
                    throw new IllegalStateException("Barrier timed out");
                  return mvc.perform(
                          post("/api/disputes/" + id + "/decision")
                              .with(actor(name))
                              .with(csrf())
                              .contentType("application/json")
                              .content(decision("APPROVED", "50", 3)))
                      .andReturn()
                      .getResponse()
                      .getStatus();
                }));
      assertTrue(ready.await(10, TimeUnit.SECONDS));
      go.countDown();
      List<Integer> codes = new ArrayList<>();
      for (Future<Integer> f : futures) codes.add(f.get(20, TimeUnit.SECONDS));
      Collections.sort(codes);
      assertEquals(List.of(200, 409), codes);
      assertEquals(1, decisions.countByDisputeId(id));
      assertEquals(1, audits.countByDisputeIdAndAction(id, "APPROVED"));
      assertEquals(
          "RESOLVED_APPROVED",
          jdbc.queryForObject("SELECT status FROM disputes WHERE id=?", String.class, id));
      Path out = Path.of("target/evaluation/concurrency.json");
      Files.createDirectories(out.getParent());
      Files.writeString(
          out,
          "{\"requests\":2,\"httpStatuses\":[200,409],\"decisionRows\":1,\"duplicateDecisions\":0,\"finalAuditEvents\":1}");
    }
  }

  @Test
  void unauthenticated401() throws Exception {
    mvc.perform(get("/api/disputes")).andExpect(status().isUnauthorized());
  }

  @Test
  void csrfRequired() throws Exception {
    mvc.perform(
            post("/api/disputes")
                .with(actor("customer"))
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void loginSessionAndCsrfFlow() throws Exception {
    MvcResult token = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
    MockHttpSession session = (MockHttpSession) token.getRequest().getSession();
    JsonNode t = body(token);
    MvcResult login =
        mvc.perform(
                post("/api/auth/login")
                    .session(session)
                    .header(t.get("headerName").asText(), t.get("token").asText())
                    .param("username", "customer@demo.local")
                    .param("password", "DemoPass123!"))
            .andExpect(status().isOk())
            .andReturn();
    MockHttpSession logged = (MockHttpSession) login.getRequest().getSession();
    mvc.perform(get("/api/auth/me").session(logged))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("CUSTOMER"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
    MvcResult refreshed = mvc.perform(get("/api/auth/csrf").session(logged)).andReturn();
    JsonNode fresh = body(refreshed);
    mvc.perform(
            post("/api/auth/logout")
                .session(logged)
                .header(fresh.get("headerName").asText(), fresh.get("token").asText()))
        .andExpect(status().isNoContent());
    assertTrue(logged.isInvalid());
  }

  @Test
  void invalidLogin401() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .param("username", "customer@demo.local")
                .param("password", "incorrect"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void managerOnlyReports() throws Exception {
    mvc.perform(get("/api/sla/summary").with(actor("customer"))).andExpect(status().isForbidden());
    mvc.perform(get("/api/reports/weekly?week=2026-11-02").with(actor("support")))
        .andExpect(status().isForbidden());
  }

  @Test
  void paginationAndValidation() throws Exception {
    create();
    mvc.perform(get("/api/disputes?size=1&page=0&sort=dueAt&direction=asc").with(actor("customer")))
        .andExpect(jsonPath("$.totalPages").value(1));
    mvc.perform(get("/api/disputes?size=101").with(actor("customer")))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/disputes?sort=passwordHash").with(actor("customer")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void missingDispute404() throws Exception {
    mvc.perform(get("/api/disputes/9999").with(actor("manager"))).andExpect(status().isNotFound());
  }

  @Test
  void auditAccessAndContent() throws Exception {
    long id = create();
    mvc.perform(get("/api/disputes/" + id + "/audit").with(actor("customer2")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/disputes/" + id + "/audit").with(actor("customer")))
        .andExpect(jsonPath("$.content[0].action").value("CREATED"))
        .andExpect(jsonPath("$.content[0].actor").value("Demo customer"));
  }

  @Test
  void weeklyAndSlaFixture() throws Exception {
    String fixture =
        new String(
            getClass().getResourceAsStream("/evaluation-fixture.sql").readAllBytes(),
            java.nio.charset.StandardCharsets.UTF_8);
    for (String sql : fixture.split(";")) if (!sql.isBlank()) jdbc.execute(sql);
    JsonNode sla =
        body(
            mvc.perform(get("/api/sla/summary").with(actor("manager")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.open").value(4))
                .andExpect(jsonPath("$.dueSoon").value(2))
                .andExpect(jsonPath("$.overdue").value(1))
                .andExpect(jsonPath("$.resolved").value(3))
                .andExpect(jsonPath("$.resolvedWithinSla").value(2))
                .andExpect(jsonPath("$.resolvedBreached").value(1))
                .andReturn());
    JsonNode report =
        body(
            mvc.perform(get("/api/reports/weekly?week=2026-11-02").with(actor("manager")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.resolved").value(2))
                .andExpect(jsonPath("$.withinSla").value(1))
                .andExpect(jsonPath("$.breached").value(1))
                .andExpect(jsonPath("$.averageResolutionHours").value(73.0))
                .andExpect(jsonPath("$.approvedRefundTotal").value(25.5))
                .andReturn());
    mvc.perform(get("/api/reports/weekly.csv?week=2026-11-02").with(actor("manager")))
        .andExpect(status().isOk())
        .andExpect(
            header().string("Content-Disposition", "attachment; filename=weekly-2026-11-02.csv"));
    Path out = Path.of("target/evaluation/fixture-results.json");
    Files.createDirectories(out.getParent());
    Files.writeString(
        out,
        json.writerWithDefaultPrettyPrinter()
            .writeValueAsString(Map.of("sampleSize", 7, "sla", sla, "weekly", report)));
  }

  @Test
  void missingWeek400() throws Exception {
    mvc.perform(get("/api/reports/weekly").with(actor("manager")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void invalidWeek400() throws Exception {
    mvc.perform(get("/api/reports/weekly?week=2026-11-03").with(actor("manager")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void databaseDecisionUnique() throws Exception {
    long id = pending();
    mutate(id, "decision", "manager", decision("APPROVED", "10", 3), 200);
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () ->
            jdbc.update(
                "INSERT INTO"
                    + " dispute_decisions(dispute_id,manager_id,outcome,approved_refund_amount,explanation,created_at)"
                    + " SELECT"
                    + " dispute_id,manager_id,outcome,approved_refund_amount,explanation,created_at"
                    + " FROM dispute_decisions WHERE dispute_id=?",
                id));
  }

  @Test
  void failedAuditRollsBackDecision() throws Exception {
    long id = pending();
    jdbc.execute(
        "CREATE FUNCTION block_final_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF"
            + " NEW.action = 'APPROVED' THEN RAISE EXCEPTION 'injected audit failure'; END IF;"
            + " RETURN NEW; END $$");
    jdbc.execute(
        "CREATE TRIGGER fail_audit BEFORE INSERT ON audit_events FOR EACH ROW EXECUTE FUNCTION"
            + " block_final_audit()");
    try {
      try {
        mvc.perform(
                post("/api/disputes/" + id + "/decision")
                    .with(actor("manager"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(decision("APPROVED", "10", 3)))
            .andReturn();
      } catch (jakarta.servlet.ServletException expected) {
      }
      assertEquals(0, decisions.countByDisputeId(id));
      assertEquals(
          "PENDING_DECISION",
          jdbc.queryForObject("SELECT status FROM disputes WHERE id=?", String.class, id));
      assertEquals(0, audits.countByDisputeIdAndAction(id, "APPROVED"));
    } finally {
      jdbc.execute("DROP TRIGGER fail_audit ON audit_events");
      jdbc.execute("DROP FUNCTION block_final_audit()");
    }
  }

  @Test
  void swaggerAvailable() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("Billing Dispute & Refund Tracker"));
  }
}
