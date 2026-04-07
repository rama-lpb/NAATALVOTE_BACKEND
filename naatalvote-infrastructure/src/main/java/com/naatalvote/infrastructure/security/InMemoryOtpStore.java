package com.naatalvote.infrastructure.security;

import com.naatalvote.application.auth.ports.OtpStorePort;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryOtpStore implements OtpStorePort {
  private record Entry(String otp, Instant expiresAt) {}

  private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

  @Override
  public void put(String key, String otp, Duration ttl) {
    store.put(key, new Entry(otp, Instant.now().plus(ttl)));
  }

  @Override
  public String get(String key) {
    Entry e = store.get(key);
    if (e == null) return null;
    if (Instant.now().isAfter(e.expiresAt)) {
      store.remove(key);
      return null;
    }
    return e.otp;
  }

  @Override
  public void remove(String key) {
    store.remove(key);
  }
}

