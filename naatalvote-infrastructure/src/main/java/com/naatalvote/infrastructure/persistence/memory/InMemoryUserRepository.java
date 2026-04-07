package com.naatalvote.infrastructure.persistence.memory;

import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.domain.auth.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryUserRepository implements UserRepositoryPort {
  private final ConcurrentHashMap<UUID, User> byId = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, UUID> idByCni = new ConcurrentHashMap<>();

  @Override
  public Optional<User> findByCni(String cni) {
    if (cni == null) return Optional.empty();
    UUID id = idByCni.get(cni.trim());
    if (id == null) return Optional.empty();
    return findById(id);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public List<User> findAll() {
    return new ArrayList<>(byId.values());
  }

  @Override
  public User save(User user) {
    byId.put(user.id(), user);
    idByCni.put(user.cni(), user.id());
    return user;
  }
}

