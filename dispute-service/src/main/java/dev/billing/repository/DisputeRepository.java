package dev.billing.repository;

import dev.billing.domain.Dispute;
import org.springframework.data.jpa.repository.*;

public interface DisputeRepository
    extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute> {}
