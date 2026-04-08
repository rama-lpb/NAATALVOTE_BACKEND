package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.domain.election.Election;
import com.naatalvote.infrastructure.persistence.jpa.entity.ElectionEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.ElectionJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ElectionJpaAdapter implements ElectionRepositoryPort {
  private final ElectionJpaRepository repo;

  public ElectionJpaAdapter(ElectionJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Election save(Election election) {
    repo.save(toEntity(election));
    return election;
  }

  @Override
  public Optional<Election> findById(UUID id) {
    return repo.findById(id).map(ElectionJpaAdapter::toDomain);
  }

  @Override
  public List<Election> findAll() {
    return repo.findAll().stream().map(ElectionJpaAdapter::toDomain).toList();
  }

  @Override
  public List<Election> findAllPaged(int offset, int limit) {
    int page = offset / limit;
    return repo.findAll(org.springframework.data.domain.PageRequest.of(page, limit))
        .stream().map(ElectionJpaAdapter::toDomain).toList();
  }

  @Override
  public long count() {
    return repo.count();
  }

  private static Election toDomain(ElectionEntity e) {
    return new Election(e.getId(), e.getTitre(), e.getDescription(), e.getType(), e.getDateDebut(), e.getDateFin(), e.getStatut(), e.getAdminId());
  }

  private static ElectionEntity toEntity(Election e) {
    ElectionEntity out = new ElectionEntity();
    out.setId(e.id());
    out.setTitre(e.titre());
    out.setDescription(e.description());
    out.setType(e.type());
    out.setDateDebut(e.dateDebut());
    out.setDateFin(e.dateFin());
    out.setStatut(e.statut());
    out.setAdminId(e.adminId());
    return out;
  }
}

