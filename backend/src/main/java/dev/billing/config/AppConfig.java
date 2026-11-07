package dev.billing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.time.Clock;
import org.springframework.context.annotation.*;

@Configuration
public class AppConfig {
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Billing Dispute & Refund Tracker")
                .version("1.0.0")
                .description(
                    "Session authentication and CSRF required. Simulated refund approvals only. See"
                        + " docs/API.md for login and examples."));
  }
}
