package dev.billing.integration;

import dev.billing.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** A deliberately small internal API-key guard; must only be used on a private network. */
@Component
public class InternalKey {
  private final byte[] expected;
  public InternalKey(@Value("${billing.internal-api-key:}") String key) {
    expected = key.getBytes(StandardCharsets.UTF_8);
  }
  public void require(String supplied) {
    if (expected.length < 24 || supplied == null ||
        !MessageDigest.isEqual(expected, supplied.getBytes(StandardCharsets.UTF_8))) {
      throw new ApiException(401, "Invalid internal service credential.");
    }
  }
}
