package dev.billing.repository;

import dev.billing.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByEmail(String email);

  List<AppUser> findByRoleOrderByDisplayName(Role role);
}
