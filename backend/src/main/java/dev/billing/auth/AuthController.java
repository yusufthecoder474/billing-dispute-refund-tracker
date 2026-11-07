package dev.billing.auth;

import dev.billing.dto.ApiDtos.UserView;
import dev.billing.repository.UserRepository;
import java.security.Principal;
import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
  private final UserRepository users;

  public AuthController(UserRepository users) {
    this.users = users;
  }

  @GetMapping("/auth/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
  }

  @GetMapping("/auth/me")
  public UserView me(Principal p) {
    return UserView.of(users.findByEmail(p.getName()).orElseThrow());
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "UP");
  }
}
