package com.naatalvote.infrastructure.security;

import com.naatalvote.application.auth.ports.OtpStorePort;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

public final class RedisOtpStore implements OtpStorePort {
  private final StringRedisTemplate redis;

  public RedisOtpStore(StringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  public void put(String key, String otp, Duration ttl) {
    redis.opsForValue().set(key, otp, ttl);
  }

  @Override
  public String get(String key) {
    return redis.opsForValue().get(key);
  }

  @Override
  public void remove(String key) {
    redis.delete(key);
  }
}

