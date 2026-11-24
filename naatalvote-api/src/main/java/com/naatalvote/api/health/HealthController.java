package com.naatalvote.api.health;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @org.springframework.beans.factory.annotation.Value("${BUILD_SHA:local}")
  private String version;
  @org.springframework.beans.factory.annotation.Value("${naatalvote.demo:false}")
  private boolean demo;
  @GetMapping("/api/v1/health")
  public Map<String, Object> health() {
    return Map.of("status", "ok", "version", version, "demo", demo);
  }
}
