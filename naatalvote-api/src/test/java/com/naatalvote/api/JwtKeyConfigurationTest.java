package com.naatalvote.api;

import static org.junit.jupiter.api.Assertions.*;
import com.naatalvote.infrastructure.config.JwtConfig;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtKeyConfigurationTest {
  @Test
  void productionRejectsMissingOrPartialSigningKeys() {
    var config = new JwtConfig();
    assertThrows(IllegalStateException.class, () -> config.rsaKeyPair("", "", false));
    assertThrows(IllegalStateException.class, () -> config.rsaKeyPair("partial", "", false));
  }

  @Test
  void configuredKeysRemainTheSameAcrossRestarts() {
    var config = new JwtConfig();
    var initial = config.rsaKeyPair("", "", true);
    String privatePem = "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder().encodeToString(initial.getPrivate().getEncoded()) + "\n-----END PRIVATE KEY-----";
    String publicPem = "-----BEGIN PUBLIC KEY-----\n" + Base64.getEncoder().encodeToString(initial.getPublic().getEncoded()) + "\n-----END PUBLIC KEY-----";
    var first = config.rsaKeyPair(privatePem, publicPem, false);
    var restarted = config.rsaKeyPair(privatePem, publicPem, false);
    assertArrayEquals(first.getPrivate().getEncoded(), restarted.getPrivate().getEncoded());
    assertArrayEquals(first.getPublic().getEncoded(), restarted.getPublic().getEncoded());
  }
}
