package com.naatalvote.infrastructure.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naatalvote.application.auth.ports.CitizenRegistryPort;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fallback registry reading a bundled fixtures file from the classpath.
 *
 * <p>This is useful for demo/deploy environments where the external AppDAFF
 * simulator isn't deployed alongside the API.</p>
 */
public final class ClasspathFixturesCitizenRegistry implements CitizenRegistryPort {
  private static final String DEFAULT_RESOURCE = "/fixtures/users.json";

  private final Map<String, CitizenRecord> byCni;

  public ClasspathFixturesCitizenRegistry(ObjectMapper objectMapper) {
    this.byCni = load(objectMapper);
  }

  @Override
  public Optional<CitizenRecord> findByCni(String cni) {
    if (cni == null) return Optional.empty();
    String key = cni.trim();
    if (key.isEmpty()) return Optional.empty();
    return Optional.ofNullable(byCni.get(key));
  }

  private static Map<String, CitizenRecord> load(ObjectMapper objectMapper) {
    try (InputStream in = ClasspathFixturesCitizenRegistry.class.getResourceAsStream(DEFAULT_RESOURCE)) {
      if (in == null) return Map.of();

      JsonNode root = objectMapper.readTree(in);
      JsonNode users = root.get("users");
      if (users == null || !users.isArray()) return Map.of();

      HashMap<String, CitizenRecord> out = new HashMap<>();
      for (JsonNode u : users) {
        if (u == null || u.isNull()) continue;

        CitizenRecord rec = new CitizenRecord(
            text(u, "cni"),
            text(u, "nom"),
            text(u, "prenom"),
            text(u, "email"),
            list(u, "telephones"),
            text(u, "date_naissance"),
            text(u, "adresse"),
            list(u, "roles")
        );

        String primary = (rec.cni() == null ? "" : rec.cni().trim());
        if (!primary.isEmpty()) out.put(primary, rec);

        JsonNode aliases = u.get("aliases");
        if (aliases != null && aliases.isArray()) {
          for (JsonNode a : aliases) {
            String alias = a == null || a.isNull() ? "" : a.asText("");
            alias = alias == null ? "" : alias.trim();
            if (!alias.isEmpty()) out.put(alias, rec);
          }
        }
      }
      return Map.copyOf(out);
    } catch (Exception ignored) {
      return Map.of();
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

