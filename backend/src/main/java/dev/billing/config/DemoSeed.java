package dev.billing.config;

import dev.billing.domain.*;
import dev.billing.repository.UserRepository;
import java.time.Clock;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local", "test"})
public class DemoSeed implements CommandLineRunner {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final Clock clock;

  public DemoSeed(UserRepository u, PasswordEncoder e, Clock c) {
    users = u;
    encoder = e;
    clock = c;
  }

  @Override
  @Transactional
  public void run(String... args) {
    for (String name :
        new String[] {"customer", "customer2", "support", "support2", "manager", "manager2"}) {
      if (users.findByEmail(name + "@demo.local").isPresent()) continue;
      AppUser u = new AppUser();
      u.email = name + "@demo.local";
      u.displayName = "Demo " + name;
      u.role =
          name.startsWith("customer")
              ? Role.CUSTOMER
              : name.startsWith("support") ? Role.SUPPORT : Role.MANAGER;
      u.passwordHash = encoder.encode("DemoPass123!");
      u.createdAt = clock.instant();
      users.save(u);
    }
  }
}
