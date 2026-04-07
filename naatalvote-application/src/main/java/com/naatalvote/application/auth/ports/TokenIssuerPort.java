package com.naatalvote.application.auth.ports;

import com.naatalvote.domain.auth.UserRole;
import java.util.List;
import java.util.UUID;

public interface TokenIssuerPort {
  String issueToken(UUID userId, List<UserRole> roles);
}

