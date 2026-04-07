package com.naatalvote.application.election.ports;

import com.naatalvote.domain.election.Candidate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidateRepositoryPort {
  Candidate save(Candidate candidate);
  Optional<Candidate> findById(UUID id);
  List<Candidate> findAll();
  List<Candidate> findByElectionId(UUID electionId);
  void deleteById(UUID id);
}
