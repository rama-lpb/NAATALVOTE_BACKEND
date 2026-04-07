package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.domain.fraud.Suspension;
import com.naatalvote.infrastructure.persistence.jpa.entity.SuspensionEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.SuspensionJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SuspensionJpaAdapter implements SuspensionRepositoryPort {
  private final SuspensionJpaRepository repo;

  public SuspensionJpaAdapter(SuspensionJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Suspension save(Suspension suspension) {
    repo.save(toEntity(suspension));
    return suspension;
  }

  @Override
  public Optional<Suspension> findById(UUID id) {
    return repo.findById(id).map(SuspensionJpaAdapter::toDomain);
  }

  @Override
  public List<Suspension> findAll() {
    return repo.findAll().stream().map(SuspensionJpaAdapter::toDomain).toList();
  }

  private static Suspension toDomain(SuspensionEntity e) {
    return new Suspension(
        e.getId(),
        e.getCitoyenId(),
        e.getMotif(),
        e.getOperateurId(),
        e.getSuperadminId(),
        e.getStatut(),
        e.getDateCreation(),
        e.getDateDecision(),
        e.getJustification()
    );
  }

  private static SuspensionEntity toEntity(Suspension s) {
    SuspensionEntity e = new SuspensionEntity();
    e.setId(s.id());
    e.setCitoyenId(s.citoyenId());
    e.setMotif(s.motif());
    e.setOperateurId(s.operateurId());
    e.setSuperadminId(s.superadminId());
    e.setStatut(s.statut());
    e.setDateCreation(s.dateCreation());
    e.setDateDecision(s.dateDecision());
    e.setJustification(s.justification());
    return e;
  }
}

