package com.naatalvote.infrastructure.external;

import com.naatalvote.application.auth.ports.CitizenRegistryPort;
import java.util.Objects;
import java.util.Optional;

public final class FallbackCitizenRegistry implements CitizenRegistryPort {
  private final CitizenRegistryPort primary;
  private final CitizenRegistryPort fallback;

  public FallbackCitizenRegistry(CitizenRegistryPort primary, CitizenRegistryPort fallback) {
    this.primary = Objects.requireNonNull(primary, "primary");
    this.fallback = Objects.requireNonNull(fallback, "fallback");
  }

  @Override
  public Optional<CitizenRecord> findByCni(String cni) {
    try {
      Optional<CitizenRecord> res = primary.findByCni(cni);
      if (res != null && res.isPresent()) return res;
    } catch (Exception ignored) {
      // fall through
    }
    return fallback.findByCni(cni);
  }
}

