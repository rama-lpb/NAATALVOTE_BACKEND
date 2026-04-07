package com.naatalvote.infrastructure.persistence.jpa.adapter;

import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.domain.election.Candidate;
import com.naatalvote.infrastructure.persistence.jpa.entity.CandidateEntity;
import com.naatalvote.infrastructure.persistence.jpa.repository.CandidateJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CandidateJpaAdapter implements CandidateRepositoryPort {
  private final CandidateJpaRepository repo;

  public CandidateJpaAdapter(CandidateJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Candidate save(Candidate candidate) {
    repo.save(toEntity(candidate));
    return candidate;
  }

  @Override
  public Optional<Candidate> findById(UUID id) {
    return repo.findById(id).map(CandidateJpaAdapter::toDomain);
  }

  @Override
  public List<Candidate> findAll() {
    return repo.findAll().stream().map(CandidateJpaAdapter::toDomain).toList();
  }

  @Override
  public List<Candidate> findByElectionId(UUID electionId) {
    return repo.findByElectionId(electionId).stream().map(CandidateJpaAdapter::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    repo.deleteById(id);
  }

  private static Candidate toDomain(CandidateEntity e) {
    return new Candidate(e.getId(), e.getElectionId(), e.getNom(), e.getPrenom(), e.getPartiPolitique(), e.getBiographie(), e.getPhotoUrl(), e.getProgrammeUrl());
  }

  private static CandidateEntity toEntity(Candidate c) {
    CandidateEntity e = new CandidateEntity();
    e.setId(c.id());
    e.setElectionId(c.electionId());
    e.setNom(c.nom());
    e.setPrenom(c.prenom());
    e.setPartiPolitique(c.partiPolitique());
    e.setBiographie(c.biographie());
    e.setPhotoUrl(c.photoUrl());
    e.setProgrammeUrl(c.programmeUrl());
    return e;
  }
}

