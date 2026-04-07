package com.naatalvote.application.vote.ports;

import com.naatalvote.domain.vote.TraceVote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TraceVoteRepositoryPort {
  TraceVote save(TraceVote traceVote);
  Optional<TraceVote> findByCitoyenIdAndElectionId(UUID citoyenId, UUID electionId);
  List<TraceVote> findByCitoyenId(UUID citoyenId);
}
