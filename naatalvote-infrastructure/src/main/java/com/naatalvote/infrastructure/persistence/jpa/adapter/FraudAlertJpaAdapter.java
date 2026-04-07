package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.domain.fraud.FraudAlert;
import com.naatalvote.infrastructure.persistence.jpa.entity.FraudAlertEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.FraudAlertJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FraudAlertJpaAdapter implements FraudAlertRepositoryPort {
  private final FraudAlertJpaRepository repo;

  public FraudAlertJpaAdapter(FraudAlertJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public FraudAlert save(FraudAlert alert) {
    repo.save(toEntity(alert));
    return alert;
  }

  @Override
  public Optional<FraudAlert> findById(UUID id) {
    return repo.findById(id).map(FraudAlertJpaAdapter::toDomain);
  }

  @Override
  public List<FraudAlert> findAll() {
    return repo.findAll().stream().map(FraudAlertJpaAdapter::toDomain).toList();
  }

  private static FraudAlert toDomain(FraudAlertEntity e) {
    return new FraudAlert(e.getId(), e.getType(), e.getCitoyenId(), e.getElectionId(), e.getDescription(), e.getStatut(), e.getDateDetection(), e.getOperateurId(), e.getDateTraitement(), e.getIp());
  }

  private static FraudAlertEntity toEntity(FraudAlert a) {
    FraudAlertEntity e = new FraudAlertEntity();
    e.setId(a.id());
    e.setType(a.type());
    e.setCitoyenId(a.citoyenId());
    e.setElectionId(a.electionId());
    e.setDescription(a.description());
    e.setStatut(a.statut());
    e.setDateDetection(a.dateDetection());
    e.setOperateurId(a.operateurId());
    e.setDateTraitement(a.dateTraitement());
    e.setIp(a.ip());
    return e;
  }
}

