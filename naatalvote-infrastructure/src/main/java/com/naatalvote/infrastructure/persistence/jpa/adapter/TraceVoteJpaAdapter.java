package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.domain.vote.TraceVote;
import com.naatalvote.infrastructure.persistence.jpa.entity.TraceVoteEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.TraceVoteJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TraceVoteJpaAdapter implements TraceVoteRepositoryPort {
  private final TraceVoteJpaRepository repo;

  public TraceVoteJpaAdapter(TraceVoteJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public TraceVote save(TraceVote traceVote) {
    repo.save(toEntity(traceVote));
    return traceVote;
  }

  @Override
  public Optional<TraceVote> findByCitoyenIdAndElectionId(UUID citoyenId, UUID electionId) {
    return repo.findByCitoyenIdAndElectionId(citoyenId, electionId).map(TraceVoteJpaAdapter::toDomain);
  }

  @Override
  public List<TraceVote> findByCitoyenId(UUID citoyenId) {
    return repo.findByCitoyenId(citoyenId).stream().map(TraceVoteJpaAdapter::toDomain).toList();
  }

  private static TraceVote toDomain(TraceVoteEntity e) {
    return new TraceVote(e.getId(), e.getCitoyenId(), e.getElectionId(), e.isAVote(), e.getHorodatage());
  }

  private static TraceVoteEntity toEntity(TraceVote tv) {
    TraceVoteEntity e = new TraceVoteEntity();
    e.setId(tv.id());
    e.setCitoyenId(tv.citoyenId());
    e.setElectionId(tv.electionId());
    e.setAVote(tv.aVote());
    e.setHorodatage(tv.horodatage());
    return e;
  }
}

