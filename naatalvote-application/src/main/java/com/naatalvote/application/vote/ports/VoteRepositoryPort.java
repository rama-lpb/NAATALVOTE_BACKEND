package com.naatalvote.application.vote.ports;

import com.naatalvote.domain.vote.Vote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepositoryPort {
  Vote save(Vote vote);
  Optional<Vote> findByToken(UUID token);
  List<Vote> findByElectionId(UUID electionId);
}
