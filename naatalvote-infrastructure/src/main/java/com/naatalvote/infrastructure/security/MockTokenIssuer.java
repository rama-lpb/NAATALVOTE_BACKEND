package com.naatalvote.infrastructure.security;

import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.domain.auth.UserRole;
import java.util.List;
import java.util.UUID;

public final class MockTokenIssuer implements TokenIssuerPort {
  @Override
  public String issueToken(UUID userId, List<UserRole> roles) {
    return "mock-token-" + userId + "-" + String.join(",", roles.stream().map(Enum::name).toList());
  }
}

