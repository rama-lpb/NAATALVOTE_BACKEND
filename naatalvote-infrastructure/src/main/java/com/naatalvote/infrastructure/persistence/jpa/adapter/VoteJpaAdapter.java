package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import com.naatalvote.domain.vote.Vote;
import com.naatalvote.infrastructure.persistence.jpa.entity.VoteEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.VoteJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class VoteJpaAdapter implements VoteRepositoryPort {
  private final VoteJpaRepository repo;

  public VoteJpaAdapter(VoteJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Vote save(Vote vote) {
    repo.save(toEntity(vote));
    return vote;
  }

  @Override
  public Optional<Vote> findByToken(UUID token) {
    return repo.findByTokenAnonyme(token).map(VoteJpaAdapter::toDomain);
  }

  @Override
  public List<Vote> findByElectionId(UUID electionId) {
    return repo.findByElectionId(electionId).stream().map(VoteJpaAdapter::toDomain).toList();
  }

  private static Vote toDomain(VoteEntity e) {
    return new Vote(e.getId(), e.getTokenAnonyme(), e.getElectionId(), e.getCandidatId(), e.getHorodatage());
  }

  private static VoteEntity toEntity(Vote v) {
    VoteEntity e = new VoteEntity();
    e.setId(v.id());
    e.setTokenAnonyme(v.tokenAnonyme());
    e.setElectionId(v.electionId());
    e.setCandidatId(v.candidatId());
    e.setHorodatage(v.horodatage());
    return e;
  }
}

