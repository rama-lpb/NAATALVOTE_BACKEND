package com.naatalvote.application.common;

import java.time.Instant;

public final class SystemTimeProvider implements TimeProvider {
  @Override
  public Instant now() {
    return Instant.now();
  }
}

