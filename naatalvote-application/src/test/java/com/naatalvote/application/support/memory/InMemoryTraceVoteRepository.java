package com.naatalvote.application.support.memory;

import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.domain.vote.TraceVote;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTraceVoteRepository implements TraceVoteRepositoryPort {
  private final ConcurrentHashMap<String, TraceVote> store = new ConcurrentHashMap<>();

  @Override
  public TraceVote save(TraceVote traceVote) {
    store.put(key(traceVote.citoyenId(), traceVote.electionId()), traceVote);
    return traceVote;
  }

  @Override
  public Optional<TraceVote> findByCitoyenIdAndElectionId(UUID citoyenId, UUID electionId) {
    return Optional.ofNullable(store.get(key(citoyenId, electionId)));
  }

  @Override
  public List<TraceVote> findByCitoyenId(UUID citoyenId) {
    List<TraceVote> out = new ArrayList<>();
    for (TraceVote tv : store.values()) {
      if (tv.citoyenId().equals(citoyenId)) out.add(tv);
    }
    return out;
  }

  private static String key(UUID citoyenId, UUID electionId) {
    return citoyenId + ":" + electionId;
  }
}
