package dev.billing.reporting.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Simple shared secret for a private Docker Compose network; not external identity management. */
@Component
public class InternalKey {
  private final byte[] expected;
  public InternalKey(@Value("${billing.internal-api-key:}") String key) {
    expected = key.getBytes(StandardCharsets.UTF_8);
  }
  public void require(String supplied) {
    if (expected.length < 24 || supplied == null ||
        !MessageDigest.isEqual(expected, supplied.getBytes(StandardCharsets.UTF_8))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal service credential.");
    }
  }
}
