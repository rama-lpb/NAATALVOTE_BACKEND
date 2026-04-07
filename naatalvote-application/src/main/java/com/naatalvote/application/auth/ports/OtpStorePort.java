package com.naatalvote.application.auth.ports;

import java.time.Duration;

public interface OtpStorePort {
  void put(String key, String otp, Duration ttl);
  String get(String key);
  void remove(String key);
}

