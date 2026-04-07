package com.naatalvote.infrastructure.security;

import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.domain.auth.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

public final class JwtTokenIssuer implements TokenIssuerPort {
  private final JwtEncoder encoder;

  public JwtTokenIssuer(JwtEncoder encoder) {
    this.encoder = encoder;
  }

  @Override
  public String issueToken(UUID userId, List<UserRole> roles) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("naatalvote")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(30 * 60))
        .subject(userId.toString())
        .claim("roles", roles.stream().map(Enum::name).toList())
        .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}

