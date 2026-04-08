package com.naatalvote.application.support.memory;

import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.domain.election.Election;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryElectionRepository implements ElectionRepositoryPort {
  private final ConcurrentHashMap<UUID, Election> store = new ConcurrentHashMap<>();

  @Override
  public Election save(Election election) {
    store.put(election.id(), election);
    return election;
  }

  @Override
  public Optional<Election> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<Election> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Election> findAllPaged(int offset, int limit) {
    return store.values().stream().skip(offset).limit(limit).toList();
  }

  @Override
  public long count() {
    return store.size();
  }
}
