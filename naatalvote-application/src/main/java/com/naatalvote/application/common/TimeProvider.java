package com.naatalvote.application.common;

import java.time.Instant;

public interface TimeProvider {
  Instant now();
}

