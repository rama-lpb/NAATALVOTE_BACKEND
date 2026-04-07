package com.naatalvote.application.election.usecases;

import com.naatalvote.domain.election.ElectionType;
import java.time.Instant;
import java.util.UUID;

public interface ProgramElectionUseCase {
  ProgramElectionResponse program(ProgramElectionRequest request);

  record ProgramElectionRequest(
      UUID adminId,
      String titre,
      String description,
      ElectionType type,
      Instant dateDebut,
      Instant dateFin
  ) {}

  record ProgramElectionResponse(UUID electionId) {}
}

