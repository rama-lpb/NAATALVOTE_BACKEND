package com.naatalvote.infrastructure.persistence.memory;

import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.domain.fraud.Suspension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySuspensionRepository implements SuspensionRepositoryPort {
  private final ConcurrentHashMap<UUID, Suspension> store = new ConcurrentHashMap<>();

  @Override
  public Suspension save(Suspension suspension) {
    store.put(suspension.id(), suspension);
    return suspension;
  }

  @Override
  public Optional<Suspension> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<Suspension> findAll() {
    return new ArrayList<>(store.values());
  }
}

