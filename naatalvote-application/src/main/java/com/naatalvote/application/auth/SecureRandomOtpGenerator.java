package com.naatalvote.application.auth;

import java.security.SecureRandom;

public final class SecureRandomOtpGenerator implements OtpGenerator {
  private static final SecureRandom RNG = new SecureRandom();

  @Override
  public String generate() {
    int n = 100_000 + RNG.nextInt(900_000);
    return String.valueOf(n);
  }
}

