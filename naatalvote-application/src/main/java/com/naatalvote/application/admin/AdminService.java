package com.naatalvote.application.admin;

import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AdminService {
  private final ElectionRepositoryPort elections;

  public AdminService(ElectionRepositoryPort elections) {
    this.elections = Objects.requireNonNull(elections, "elections");
  }

  public List<Election> listAdminElections(UUID adminId) {
    return elections.findAll().stream().filter(e -> adminId == null || e.adminId().equals(adminId)).toList();
  }

  public void closeElection(UUID electionId) {
    Election e = elections.findById(electionId).orElseThrow();
    Election closed = new Election(e.id(), e.titre(), e.description(), e.type(), e.dateDebut(), e.dateFin(), ElectionStatus.CLOTUREE, e.adminId());
    elections.save(closed);
  }
}

