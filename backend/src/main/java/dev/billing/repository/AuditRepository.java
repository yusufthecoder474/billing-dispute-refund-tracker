package dev.billing.repository;

import dev.billing.domain.AuditEvent;
import org.springframework.data.domain.*;
import org.springframework.data.repository.Repository;

public interface AuditRepository extends Repository<AuditEvent, Long> {
  AuditEvent save(AuditEvent event);

  Page<AuditEvent> findByDisputeIdOrderByCreatedAtAscIdAsc(Long id, Pageable pageable);

  long countByDisputeIdAndAction(Long id, String action);
}
