package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.domain.auth.User;
import com.naatalvote.infrastructure.persistence.jpa.entity.UserEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class UserJpaAdapter implements UserRepositoryPort {
  private final UserJpaRepository repo;

  public UserJpaAdapter(UserJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Optional<User> findByCni(String cni) {
    return repo.findByCni(cni).map(UserJpaAdapter::toDomain);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return repo.findById(id).map(UserJpaAdapter::toDomain);
  }

  @Override
  public List<User> findAll() {
    return repo.findAll().stream().map(UserJpaAdapter::toDomain).toList();
  }

  @Override
  public User save(User user) {
    repo.save(toEntity(user));
    return user;
  }

  private static User toDomain(UserEntity e) {
    return new User(
        e.getId(),
        e.getCni(),
        e.getNom(),
        e.getPrenom(),
        e.getEmail(),
        e.getTelephones(),
        e.getRoles(),
        e.getDateNaissance(),
        e.getAdresse()
    );
  }

  private static UserEntity toEntity(User u) {
    UserEntity e = new UserEntity();
    e.setId(u.id());
    e.setCni(u.cni());
    e.setNom(u.nom());
    e.setPrenom(u.prenom());
    e.setEmail(u.email());
    e.setDateNaissance(u.dateNaissance());
    e.setAdresse(u.adresse());
    e.setTelephones(u.telephones());
    e.setRoles(u.roles());
    return e;
  }
}
