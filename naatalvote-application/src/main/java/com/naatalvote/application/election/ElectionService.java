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

  private final ElectionRepositoryPort elections;
  private final CandidateRepositoryPort candidates;
  private final TimeProvider time;

  public ElectionService(ElectionRepositoryPort elections, CandidateRepositoryPort candidates, TimeProvider time) {
    this.elections = Objects.requireNonNull(elections, "elections");
    this.candidates = Objects.requireNonNull(candidates, "candidates");
    this.time = Objects.requireNonNull(time, "time");
  }

  public List<Election> listElections() {
    return elections.findAll();
  }

  public Election getElection(UUID id) {
    return elections.findById(id).orElseThrow();
  }

  public List<Candidate> listCandidates(UUID electionId) {
    return candidates.findByElectionId(electionId);
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
        cmd.adminId()
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
        existing.adminId()
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

  public record CreateElectionCommand(UUID adminId, String titre, String description, ElectionType type, Instant dateDebut, Instant dateFin) {}
  public record UpdateElectionCommand(String titre, String description, ElectionType type, Instant dateDebut, Instant dateFin) {}
  public record CreateCandidateCommand(UUID electionId, String nom, String prenom, String partiPolitique, String biographie, String photoUrl, String programmeUrl) {}
  public record UpdateCandidateCommand(String nom, String prenom, String partiPolitique, String biographie, String photoUrl, String programmeUrl) {}
}
