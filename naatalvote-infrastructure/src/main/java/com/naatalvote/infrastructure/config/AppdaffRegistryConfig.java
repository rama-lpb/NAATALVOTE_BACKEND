package com.naatalvote.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naatalvote.application.auth.ports.CitizenRegistryPort;
import com.naatalvote.infrastructure.external.AppdaffHttpCitizenRegistry;
import com.naatalvote.infrastructure.external.ClasspathFixturesCitizenRegistry;
import com.naatalvote.infrastructure.external.FallbackCitizenRegistry;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppdaffRegistryConfig {
  @Bean
  public HttpClient appdaffHttpClient() {
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build();
  }

  @Bean
  public CitizenRegistryPort citizenRegistryPort(
      @Value("${appdaff.base-url}") String baseUrl,
      HttpClient appdaffHttpClient,
      ObjectMapper objectMapper
  ) {
    CitizenRegistryPort http = new AppdaffHttpCitizenRegistry(baseUrl, appdaffHttpClient, objectMapper);
    CitizenRegistryPort fixtures = new ClasspathFixturesCitizenRegistry(objectMapper);
    return new FallbackCitizenRegistry(http, fixtures);
  }
}
