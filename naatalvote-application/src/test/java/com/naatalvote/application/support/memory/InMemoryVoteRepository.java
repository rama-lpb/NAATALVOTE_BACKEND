package com.naatalvote.application.support.memory;

import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import com.naatalvote.domain.vote.Vote;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryVoteRepository implements VoteRepositoryPort {
  private final ConcurrentHashMap<UUID, Vote> byId = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, Vote> byToken = new ConcurrentHashMap<>();

  @Override
  public Vote save(Vote vote) {
    byId.put(vote.id(), vote);
    byToken.put(vote.tokenAnonyme(), vote);
    return vote;
  }

  @Override
  public Optional<Vote> findByToken(UUID token) {
    return Optional.ofNullable(byToken.get(token));
  }

  @Override
  public List<Vote> findByElectionId(UUID electionId) {
    List<Vote> out = new ArrayList<>();
    for (Vote v : byId.values()) {
      if (v.electionId().equals(electionId)) out.add(v);
    }
    return out;
  }
}
