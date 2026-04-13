package com.naatalvote.infrastructure.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naatalvote.application.auth.ports.CitizenRegistryPort;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AppdaffHttpCitizenRegistry implements CitizenRegistryPort {
  private final URI baseUrl;
  private final HttpClient http;
  private final ObjectMapper objectMapper;

  public AppdaffHttpCitizenRegistry(String baseUrl, HttpClient http, ObjectMapper objectMapper) {
    this.baseUrl = URI.create(baseUrl == null ? "" : baseUrl.trim());
    this.http = http;
    this.objectMapper = objectMapper;
  }

  @Override
  public Optional<CitizenRecord> findByCni(String cni) {
    if (cni == null || cni.trim().isEmpty()) return Optional.empty();
    String encoded = URLEncoder.encode(cni.trim(), StandardCharsets.UTF_8);
    URI url = baseUrl.resolve("/api/v1/citoyens/" + encoded);

    HttpRequest req = HttpRequest.newBuilder(url)
        .timeout(Duration.ofSeconds(3))
        .GET()
        .build();
    try {
      HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (res.statusCode() == 404) return Optional.empty();
      if (res.statusCode() < 200 || res.statusCode() >= 300) return Optional.empty();

      JsonNode n = objectMapper.readTree(res.body());
      return Optional.of(new CitizenRecord(
          text(n, "cni"),
          text(n, "nom"),
          text(n, "prenom"),
          text(n, "email"),
          list(n, "telephones"),
          text(n, "date_naissance"),
          text(n, "adresse"),
          list(n, "roles")
      ));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static String text(JsonNode n, String field) {
    JsonNode v = n.get(field);
    return v == null || v.isNull() ? "" : v.asText("");
  }

  private static List<String> list(JsonNode n, String field) {
    JsonNode v = n.get(field);
    if (v == null || !v.isArray()) return List.of();
    ArrayList<String> out = new ArrayList<>();
    v.forEach(item -> out.add(item == null || item.isNull() ? "" : item.asText("")));
    return out.stream().map(String::trim).filter(s -> !s.isBlank()).toList();
  }
}

