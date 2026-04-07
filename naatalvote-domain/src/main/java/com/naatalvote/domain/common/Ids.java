package com.naatalvote.domain.common;

import java.util.UUID;

public final class Ids {
  private Ids() {}

  public static UUID newUuid() {
    return UUID.randomUUID();
  }
}

