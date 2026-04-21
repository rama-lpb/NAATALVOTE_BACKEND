package com.naatalvote.application.election;

import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.domain.common.DomainException;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.election.Candidate;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.domain.election.ElectionType;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ElectionService {
  private static final Duration FREEZE_WINDOW = Duration.ofHours(48);
  private static final int DEFAULT_PAGE_SIZE = 20;

  private final ElectionRepositoryPort elections;
  private final CandidateRepositoryPort candidates;
  private final TimeProvider time;

  public ElectionService(ElectionRepositoryPort elections, CandidateRepositoryPort candidates, TimeProvider time) {
    this.elections = elections;
    this.candidates = candidates;
    this.time = time;
  }

  public List<Election> listElections() {
    return elections.findAll().stream().map(this::enrichElection).toList();
  }

  public PagedResult<Election> listElectionsPaged(int page, int size) {
    int pageSize = size > 0 ? Math.min(size, 100) : DEFAULT_PAGE_SIZE;
    int offset = page * pageSize;
    List<Election> content = elections.findAllPaged(offset, pageSize).stream().map(this::enrichElection).toList();
    long total = elections.count();
    return new PagedResult<>(content, page, pageSize, total);
  }

  public Election getElection(UUID id) {
    Election e = elections.findById(id).orElseThrow(() -> new DomainException("Élection non trouvée"));
    return enrichElection(e);
  }

  public List<Candidate> listCandidates(UUID electionId) {
    return candidates.findByElectionId(electionId);
  }

  public PagedResult<Candidate> listCandidatesPaged(int page, int size) {
    int pageSize = size > 0 ? Math.min(size, 100) : DEFAULT_PAGE_SIZE;
    int offset = page * pageSize;
    List<Candidate> content = candidates.findAllPaged(offset, pageSize);
    long total = candidates.count();
    return new PagedResult<>(content, page, pageSize, total);
  }

  public UUID createElection(CreateElectionCommand cmd) {
    Election e = new Election(
        Ids.newUuid(),
        cmd.titre(),
        cmd.description(),
        cmd.type(),
        cmd.dateDebut(),
        cmd.dateFin(),
        ElectionStatus.PROGRAMMEE,
        cmd.adminId(),
        List.of(),
        cmd.region(),
        cmd.totalElecteurs(),
        0
    );
    elections.save(e);
    return e.id();
  }

  public void updateElection(UUID electionId, UpdateElectionCommand cmd) {
    Election existing = getElection(electionId);
    ensureModifiable(existing);
    Election next = new Election(
        existing.id(),
        cmd.titre() == null ? existing.titre() : cmd.titre(),
        cmd.description() == null ? existing.description() : cmd.description(),
        cmd.type() == null ? existing.type() : cmd.type(),
        cmd.dateDebut() == null ? existing.dateDebut() : cmd.dateDebut(),
        cmd.dateFin() == null ? existing.dateFin() : cmd.dateFin(),
        existing.statut(),
        existing.adminId(),
        existing.candidatIds(),
        cmd.region() == null ? existing.region() : cmd.region(),
        cmd.totalElecteurs() == null ? existing.totalElecteurs() : cmd.totalElecteurs(),
        existing.votesCount()
    );
    elections.save(next);
  }

  public void publishElection(UUID electionId) {
    // publication statistique (pas de modification des résultats)
    getElection(electionId);
  }

  public UUID createCandidate(CreateCandidateCommand cmd) {
    Election e = getElection(cmd.electionId());
    ensureModifiable(e);
    Candidate c = new Candidate(
        Ids.newUuid(),
        cmd.electionId(),
        cmd.nom(),
        cmd.prenom(),
        cmd.partiPolitique(),
        cmd.biographie(),
        cmd.photoUrl(),
        cmd.programmeUrl()
    );
    candidates.save(c);
    return c.id();
  }

  public void updateCandidate(UUID candidateId, UpdateCandidateCommand cmd) {
    Candidate existing = candidates.findById(candidateId).orElseThrow();
    Election e = getElection(existing.electionId());
    ensureModifiable(e);
    Candidate next = new Candidate(
        existing.id(),
        existing.electionId(),
        cmd.nom() == null ? existing.nom() : cmd.nom(),
        cmd.prenom() == null ? existing.prenom() : cmd.prenom(),
        cmd.partiPolitique() == null ? existing.partiPolitique() : cmd.partiPolitique(),
        cmd.biographie() == null ? existing.biographie() : cmd.biographie(),
        cmd.photoUrl() == null ? existing.photoUrl() : cmd.photoUrl(),
        cmd.programmeUrl() == null ? existing.programmeUrl() : cmd.programmeUrl()
    );
    candidates.save(next);
  }

  public void deleteCandidate(UUID candidateId) {
    Candidate existing = candidates.findById(candidateId).orElseThrow();
    Election e = getElection(existing.electionId());
    ensureModifiable(e);
    candidates.deleteById(candidateId);
  }

  public List<Candidate> listAllCandidates() {
    return candidates.findAll();
  }

  public Candidate getCandidate(UUID candidateId) {
    return candidates.findById(candidateId).orElseThrow();
  }

  private void ensureModifiable(Election e) {
    Instant freezeAt = e.dateDebut().minus(FREEZE_WINDOW);
    if (!time.now().isBefore(freezeAt)) {
      throw new DomainException("Élection verrouillée (T-48h): modification interdite");
    }
    if (e.statut() != ElectionStatus.PROGRAMMEE) {
      throw new DomainException("Élection non modifiable (statut=" + e.statut().name() + ")");
    }
  }

  public record CreateElectionCommand(
      UUID adminId,
      String titre,
      String description,
      ElectionType type,
      Instant dateDebut,
      Instant dateFin,
      String region,
      int totalElecteurs
  ) {}
  public record UpdateElectionCommand(
      String titre,
      String description,
      ElectionType type,
      Instant dateDebut,
      Instant dateFin,
      String region,
      Integer totalElecteurs
  ) {}
  public record CreateCandidateCommand(UUID electionId, String nom, String prenom, String partiPolitique, String biographie, String photoUrl, String programmeUrl) {}
  public record UpdateCandidateCommand(String nom, String prenom, String partiPolitique, String biographie, String photoUrl, String programmeUrl) {}
  public record PagedResult<T>(List<T> content, int page, int pageSize, long total) {
    public int totalPages() {
      return (int) Math.ceil((double) total / pageSize);
    }
    public boolean hasNext() {
      return page + 1 < totalPages();
    }
    public boolean hasPrevious() {
      return page > 0;
    }
  }

  private Election enrichElection(Election e) {
    List<UUID> candidateIds = candidates.findByElectionId(e.id()).stream().map(Candidate::id).toList();
    return new Election(
        e.id(),
        e.titre(),
        e.description(),
        e.type(),
        e.dateDebut(),
        e.dateFin(),
        e.statut(),
        e.adminId(),
        candidateIds,
        e.region(),
        e.totalElecteurs(),
        e.votesCount()
    );
  }
}
