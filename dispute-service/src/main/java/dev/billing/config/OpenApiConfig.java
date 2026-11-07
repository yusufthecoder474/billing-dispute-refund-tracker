package dev.billing.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenApiCustomizer contract() {
    return api -> {
      if (api.getComponents() == null) api.setComponents(new Components());
      api.getComponents()
          .addSecuritySchemes(
              "csrf",
              new SecurityScheme()
                  .type(SecurityScheme.Type.APIKEY)
                  .in(SecurityScheme.In.HEADER)
                  .name("X-CSRF-TOKEN")
                  .description(
                      "Call /api/auth/csrf in this session, copy token, authorize here. Refresh"
                          + " after login/logout."));
      api.addSecurityItem(new SecurityRequirement().addList("csrf"));
      api.path(
          "/api/auth/login",
          new PathItem()
              .post(
                  new Operation()
                      .summary("Sign in using form credentials and CSRF")
                      .requestBody(
                          new RequestBody()
                              .required(true)
                              .content(
                                  new Content()
                                      .addMediaType(
                                          "application/x-www-form-urlencoded",
                                          new MediaType()
                                              .schema(
                                                  new ObjectSchema()
                                                      .addProperty("username", new StringSchema())
                                                      .addProperty("password", new StringSchema())
                                                      .addRequiredItem("username")
                                                      .addRequiredItem("password")))))
                      .responses(
                          new ApiResponses()
                              .addApiResponse(
                                  "200",
                                  new ApiResponse()
                                      .description(
                                          "Session established; {authenticated:true}. Fetch a new"
                                              + " CSRF token.")))));
      api.path(
          "/api/auth/logout",
          new PathItem()
              .post(
                  new Operation()
                      .summary("Invalidate session; CSRF required")
                      .responses(
                          new ApiResponses()
                              .addApiResponse(
                                  "204", new ApiResponse().description("Signed out; no body")))));
      api.getPaths()
          .forEach(
              (path, item) ->
                  item.readOperations()
                      .forEach(
                          operation -> {
                            operation
                                .getResponses()
                                .addApiResponse(
                                    "400",
                                    new ApiResponse()
                                        .description(
                                            "Invalid request; ApiError with status, message,"
                                                + " timestamp"));
                            operation
                                .getResponses()
                                .addApiResponse(
                                    "401",
                                    new ApiResponse()
                                        .description("Sign in required or invalid credentials"));
                            operation
                                .getResponses()
                                .addApiResponse(
                                    "403",
                                    new ApiResponse()
                                        .description("Role/ownership denied or CSRF invalid"));
                            if (path.contains("{id}"))
                              operation
                                  .getResponses()
                                  .addApiResponse(
                                      "404",
                                      new ApiResponse()
                                          .description(
                                              "Dispute or requested support agent not found"));
                            if (path.contains("{id}") && item.getPost() == operation)
                              operation
                                  .getResponses()
                                  .addApiResponse(
                                      "409",
                                      new ApiResponse()
                                          .description(
                                              "Stale version, invalid workflow state or duplicate"
                                                  + " decision"));
                          }));
    };
  }
}
