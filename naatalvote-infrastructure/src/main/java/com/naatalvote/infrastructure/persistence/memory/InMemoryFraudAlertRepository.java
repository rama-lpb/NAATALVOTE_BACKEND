package com.naatalvote.infrastructure.persistence.memory;

import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.domain.fraud.FraudAlert;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryFraudAlertRepository implements FraudAlertRepositoryPort {
  private final ConcurrentHashMap<UUID, FraudAlert> store = new ConcurrentHashMap<>();

  @Override
  public FraudAlert save(FraudAlert alert) {
    store.put(alert.id(), alert);
    return alert;
  }

  @Override
  public Optional<FraudAlert> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<FraudAlert> findAll() {
    return new ArrayList<>(store.values());
  }
}

