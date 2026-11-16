package com.naatalvote.api.security;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

/**
 * Identité de l'appelant. En profil postgres, l'identifiant provient TOUJOURS du jeton signé (claim sub),
 * jamais du corps de la requête. En profil inmemory (démonstration sans jeton), la valeur fournie est acceptée.
 */
public final class CurrentUser {
  private CurrentUser() {}

  /** Identifiant de l'appelant : sujet du JWT s'il existe, sinon la valeur fournie (mode démonstration). */
  public static UUID idOr(Jwt jwt, String provided) {
    if (jwt != null) return UUID.fromString(jwt.getSubject());
    if (provided == null || provided.isBlank()) return null;
    return UUID.fromString(provided);
  }

  public static boolean hasRole(Jwt jwt, String role) {
    if (jwt == null) return false;
    Object roles = jwt.getClaims().get("roles");
    return roles instanceof List<?> l && l.contains(role);
  }

  /** Refuse (403) l'accès aux données d'un autre citoyen, sauf pour le super administrateur. */
  public static void requireSelfOrSuperAdmin(Jwt jwt, UUID resourceOwner) {
    if (jwt == null) return; // profil inmemory : pas d'authentification
    if (UUID.fromString(jwt.getSubject()).equals(resourceOwner)) return;
    if (hasRole(jwt, "SUPERADMIN")) return;
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé aux données d'un autre citoyen");
  }
}
