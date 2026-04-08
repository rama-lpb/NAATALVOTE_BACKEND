package com.naatalvote.application.support.memory;

import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.domain.election.Candidate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCandidateRepository implements CandidateRepositoryPort {
  private final ConcurrentHashMap<UUID, Candidate> store = new ConcurrentHashMap<>();

  @Override
  public Candidate save(Candidate candidate) {
    store.put(candidate.id(), candidate);
    return candidate;
  }

  @Override
  public Optional<Candidate> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<Candidate> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Candidate> findByElectionId(UUID electionId) {
    List<Candidate> out = new ArrayList<>();
    for (Candidate c : store.values()) {
      if (c.electionId().equals(electionId)) out.add(c);
    }
    return out;
  }

  @Override
  public List<Candidate> findAllPaged(int offset, int limit) {
    return store.values().stream().skip(offset).limit(limit).toList();
  }

  @Override
  public long count() {
    return store.size();
  }

  @Override
  public void deleteById(UUID id) {
    store.remove(id);
  }
}
