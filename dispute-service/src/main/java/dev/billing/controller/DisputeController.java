package dev.billing.controller;

import dev.billing.domain.Status;
import dev.billing.dto.ApiDtos.*;
import dev.billing.service.DisputeService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DisputeController {
  private final DisputeService service;

  public DisputeController(DisputeService service) {
    this.service = service;
  }

  @PostMapping("/disputes")
  @ResponseStatus(HttpStatus.CREATED)
  public DisputeView create(Principal p, @Valid @RequestBody CreateDispute r) {
    return service.create(service.actor(p.getName()), r);
  }

  @GetMapping("/disputes")
  public PageView<DisputeView> list(
      Principal p,
      @RequestParam(required = false) Status status,
      @RequestParam(required = false) String sla,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String direction) {
    return service.list(service.actor(p.getName()), status, sla, page, size, sort, direction);
  }

  @GetMapping("/disputes/{id}")
  public Detail detail(Principal p, @PathVariable Long id) {
    return service.detail(id, service.actor(p.getName()));
  }

  @PostMapping("/disputes/{id}/assign")
  public DisputeView assign(Principal p, @PathVariable Long id, @Valid @RequestBody Assign r) {
    return service.assign(id, service.actor(p.getName()), r);
  }

  @PostMapping("/disputes/{id}/start-review")
  public DisputeView start(Principal p, @PathVariable Long id, @Valid @RequestBody Version r) {
    return service.transition(id, service.actor(p.getName()), r, true);
  }

  @PostMapping("/disputes/{id}/submit-for-decision")
  public DisputeView submit(Principal p, @PathVariable Long id, @Valid @RequestBody Version r) {
    return service.transition(id, service.actor(p.getName()), r, false);
  }

  @PostMapping("/disputes/{id}/decision")
  public DisputeView decide(Principal p, @PathVariable Long id, @Valid @RequestBody Decide r) {
    return service.decide(id, service.actor(p.getName()), r);
  }

  @GetMapping("/disputes/{id}/audit")
  public PageView<AuditView> audit(
      Principal p,
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.audit(id, service.actor(p.getName()), page, size);
  }

  @GetMapping("/support-agents")
  public List<UserView> support(Principal p) {
    return service.support(service.actor(p.getName()));
  }
}
