package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.infrastructure.persistence.jpa.entity.ActionLogEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.ActionLogJpaRepository;
import java.util.List;
import java.util.UUID;

public final class ActionLogJpaAdapter implements ActionLogRepositoryPort {
  private final ActionLogJpaRepository repo;

  public ActionLogJpaAdapter(ActionLogJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public ActionLog append(ActionLog log) {
    repo.save(toEntity(log));
    return log;
  }

  @Override
  public List<ActionLog> findAll() {
    return repo.findAllByOrderByHorodatageDesc().stream().map(ActionLogJpaAdapter::toDomain).toList();
  }

  @Override
  public List<ActionLog> findByUserId(UUID userId) {
    return repo.findByUtilisateurIdOrderByHorodatageDesc(userId).stream().map(ActionLogJpaAdapter::toDomain).toList();
  }

  private static ActionLog toDomain(ActionLogEntity e) {
    return new ActionLog(e.getId(), e.getType(), e.getUtilisateurId(), e.getDescription(), e.getHorodatage(), e.getAdresseIp(), e.getSignature());
  }

  private static ActionLogEntity toEntity(ActionLog l) {
    ActionLogEntity e = new ActionLogEntity();
    e.setId(l.id());
    e.setType(l.type());
    e.setUtilisateurId(l.utilisateurId());
    e.setDescription(l.description());
    e.setHorodatage(l.horodatage());
    e.setAdresseIp(l.adresseIp());
    e.setSignature(l.signature());
    return e;
  }
}

