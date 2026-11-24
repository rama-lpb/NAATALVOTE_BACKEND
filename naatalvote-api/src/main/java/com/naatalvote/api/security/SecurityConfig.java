package com.naatalvote.api.security;

import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("postgres")
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/v1/auth/**",
                "/api/v1/health",
                "/api/v1/docs/**",
                "/api/v1/swagger/**",
                "/actuator/health",
                "/actuator/info"
            ).permitAll()
            // Contrôle d'accès par rôle côté serveur (ajouté lors de l'audit E221).
            // Les rôles proviennent de la revendication "roles" du JWT signé (RS256).
            .requestMatchers("/api/v1/superadmin/**").hasRole("SUPERADMIN")
            .requestMatchers("/api/v1/operateur/**", "/api/v1/security/**").hasAnyRole("OPERATEUR", "SUPERADMIN")
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/elections/**", "/api/v1/candidats/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/elections/**", "/api/v1/candidats/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/elections/**", "/api/v1/candidats/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/votes").hasRole("CITOYEN")
            .requestMatchers("/api/v1/citoyen/**").hasAnyRole("CITOYEN", "SUPERADMIN")
            // Lecture des élections, candidats, résultats et vérification d'un reçu : tout utilisateur authentifié
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
    return http.build();
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
    JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
    conv.setJwtGrantedAuthoritiesConverter(new RolesClaimConverter());
    return conv;
  }

  static final class RolesClaimConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
      Object roles = jwt.getClaims().get("roles");
      if (!(roles instanceof List<?> list)) return List.of();
      return list.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .<GrantedAuthority>map(r -> new SimpleGrantedAuthority("ROLE_" + r))
          .toList();
    }
  }
}
