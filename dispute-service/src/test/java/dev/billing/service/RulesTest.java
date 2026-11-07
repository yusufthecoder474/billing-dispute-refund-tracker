package dev.billing.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.billing.domain.*;
import dev.billing.exception.ApiException;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

class RulesTest {
  private static final Instant DUE = Instant.parse("2026-11-05T00:00:00Z");

  @ParameterizedTest
  @CsvSource({
    "2026-11-04T11:59:59Z,OPEN",
    "2026-11-04T12:00:00Z,DUE_SOON",
    "2026-11-05T00:00:00Z,DUE_SOON",
    "2026-11-05T00:00:01Z,OVERDUE"
  })
  void openSlaBoundaries(String now, String expected) {
    assertEquals(expected, Rules.sla(DUE, null, Instant.parse(now)));
  }

  @ParameterizedTest
  @CsvSource({
    "2026-11-04T23:59:59Z,WITHIN_SLA",
    "2026-11-05T00:00:00Z,WITHIN_SLA",
    "2026-11-05T00:00:01Z,BREACHED"
  })
  void resolvedSla(String resolved, String expected) {
    assertEquals(expected, Rules.sla(DUE, Instant.parse(resolved), DUE.plusSeconds(100)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0.00", "1.00", "100.00"})
  void allowedRefund(String amount) {
    assertDoesNotThrow(
        () -> Rules.refund(new BigDecimal("100"), new BigDecimal(amount), Outcome.APPROVED));
  }

  @ParameterizedTest
  @ValueSource(strings = {"-0.01", "100.01", "0.001"})
  void invalidRefund(String amount) {
    assertThrows(
        ApiException.class,
        () -> Rules.refund(new BigDecimal("100"), new BigDecimal(amount), Outcome.APPROVED));
  }

  @Test
  void nullRefund() {
    assertThrows(ApiException.class, () -> Rules.refund(BigDecimal.TEN, null, Outcome.APPROVED));
  }

  @Test
  void rejectedNonzero() {
    assertThrows(
        ApiException.class, () -> Rules.refund(BigDecimal.TEN, BigDecimal.ONE, Outcome.REJECTED));
  }

  @Test
  void rejectedZero() {
    assertDoesNotThrow(() -> Rules.refund(BigDecimal.TEN, BigDecimal.ZERO, Outcome.REJECTED));
  }

  @Test
  void validVersion() {
    Dispute d = new Dispute();
    d.version = 2L;
    assertDoesNotThrow(() -> Rules.version(d, 2L));
  }

  @Test
  void staleVersion() {
    Dispute d = new Dispute();
    d.version = 2L;
    assertThrows(ApiException.class, () -> Rules.version(d, 1L));
  }

  @Test
  void nullVersion() {
    Dispute d = new Dispute();
    d.version = 2L;
    assertThrows(ApiException.class, () -> Rules.version(d, null));
  }

  @Test
  void validState() {
    Dispute d = new Dispute();
    d.status = Status.OPEN;
    assertDoesNotThrow(() -> Rules.state(d, Status.OPEN));
  }

  @Test
  void invalidState() {
    Dispute d = new Dispute();
    d.status = Status.RESOLVED_APPROVED;
    assertThrows(ApiException.class, () -> Rules.state(d, Status.OPEN));
  }

  @Test
  void correctRole() {
    AppUser u = new AppUser();
    u.role = Role.MANAGER;
    assertDoesNotThrow(() -> Rules.role(u, Role.MANAGER));
  }

  @Test
  void incorrectRole() {
    AppUser u = new AppUser();
    u.role = Role.CUSTOMER;
    assertThrows(ApiException.class, () -> Rules.role(u, Role.MANAGER));
  }

  @Test
  void assignedAgent() {
    Dispute d = new Dispute();
    d.assignedSupportId = 2L;
    AppUser u = new AppUser();
    u.id = 2L;
    u.role = Role.SUPPORT;
    assertDoesNotThrow(() -> Rules.assigned(d, u));
  }

  @Test
  void unassignedAgent() {
    Dispute d = new Dispute();
    AppUser u = new AppUser();
    u.id = 2L;
    u.role = Role.SUPPORT;
    assertThrows(ApiException.class, () -> Rules.assigned(d, u));
  }

  @Test
  void monday() {
    assertEquals(
        Instant.parse("2026-11-02T00:00:00Z"), Rules.weekStart(LocalDate.parse("2026-11-02")));
  }

  @Test
  void nonMonday() {
    assertThrows(ApiException.class, () -> Rules.weekStart(LocalDate.parse("2026-11-03")));
  }

  @ParameterizedTest
  @ValueSource(strings = {"=SUM(A1)", "+cmd", "-1", "@user", "  =1", "\tfoo", "\rfoo", "\nfoo"})
  void csvFormula(String input) {
    assertTrue(Rules.csv(input).startsWith("\"'"));
  }

  @Test
  void csvEscape() {
    assertEquals("\"a\"\"b,c\"", Rules.csv("a\"b,c"));
  }

  @Test
  void csvNull() {
    assertEquals("\"\"", Rules.csv(null));
  }
}
