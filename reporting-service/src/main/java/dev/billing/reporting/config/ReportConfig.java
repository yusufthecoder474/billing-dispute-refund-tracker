package dev.billing.reporting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportConfig {
  @Bean
  OpenAPI documentation() {
    return new OpenAPI()
        .info(new Info().title("Reporting Service Internal API").version("1.0.0")
            .description("Weekly billing dispute reporting. Internal network only; X-Internal-Api-Key required."))
        .components(new Components().addSecuritySchemes("internalKey", new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-Internal-Api-Key")))
        .addSecurityItem(new SecurityRequirement().addList("internalKey"));
  }
}
