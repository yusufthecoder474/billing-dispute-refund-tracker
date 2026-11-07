package dev.billing.repository;

import dev.billing.domain.Decision;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionRepository extends JpaRepository<Decision, Long> {
  Optional<Decision> findByDisputeId(Long id);

  long countByDisputeId(Long id);
}
