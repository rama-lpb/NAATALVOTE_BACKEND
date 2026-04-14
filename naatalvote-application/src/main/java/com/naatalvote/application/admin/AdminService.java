package com.naatalvote.application.admin;

import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import com.naatalvote.domain.election.Candidate;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class AdminService {
  private final ElectionRepositoryPort elections;
  private final VoteRepositoryPort votes;
  private final CandidateRepositoryPort candidates;

  public AdminService(ElectionRepositoryPort elections, VoteRepositoryPort votes, CandidateRepositoryPort candidates) {
    this.elections = Objects.requireNonNull(elections, "elections");
    this.votes = Objects.requireNonNull(votes, "votes");
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public List<Election> listAdminElections(UUID adminId) {
    return elections.findAll().stream().filter(e -> adminId == null || e.adminId().equals(adminId)).toList();
  }

  public ElectionStats getElectionStats(UUID electionId) {
    Election e = elections.findById(electionId).orElseThrow(() -> new IllegalArgumentException("Élection non trouvée"));

    // Count actual vote objects in the repository
    List<com.naatalvote.domain.vote.Vote> repoVotes = votes.findByElectionId(electionId);
    int repoCount = repoVotes.size();

    // Build candidate vote counts from the repository if votes exist,
    // otherwise fall back to the stored votesCount on each Candidate (demo/seed data).
    List<CandidateVoteCount> candidateCounts;
    int totalVotes;

    if (repoCount > 0) {
      Map<UUID, Long> byCandidate = repoVotes.stream()
          .collect(Collectors.groupingBy(com.naatalvote.domain.vote.Vote::candidatId, Collectors.counting()));
      candidateCounts = byCandidate.entrySet().stream()
          .map(entry -> new CandidateVoteCount(entry.getKey(), entry.getValue().intValue()))
          .toList();
      totalVotes = repoCount;
    } else {
      // Fallback: use stored vote counts on candidates (populated at seed time)
      List<Candidate> elecCandidates = candidates.findByElectionId(electionId);
      candidateCounts = elecCandidates.stream()
          .filter(c -> c.votesCount() > 0)
          .map(c -> new CandidateVoteCount(c.id(), c.votesCount()))
          .toList();
      totalVotes = candidateCounts.stream().mapToInt(CandidateVoteCount::votes).sum();
      // Also try the election's own stored total if candidates have no individual counts
      if (totalVotes == 0) {
        totalVotes = e.votesCount();
      }
    }

    // Participation rate — always guard against division by zero
    int totalElecteurs = e.totalElecteurs();
    double participationRate;
    if (totalElecteurs > 0 && totalVotes > 0) {
      participationRate = Math.min(100.0, (totalVotes * 100.0) / totalElecteurs);
    } else {
      participationRate = 0.0;
    }

    return new ElectionStats(e.id(), e.titre(), totalVotes, participationRate, candidateCounts, e.statut().name());
  }

  public void closeElection(UUID electionId) {
    Election e = elections.findById(electionId).orElseThrow();
    Election closed = new Election(e.id(), e.titre(), e.description(), e.type(), e.dateDebut(), e.dateFin(), ElectionStatus.CLOTUREE, e.adminId());
    elections.save(closed);
  }

  public record ElectionStats(
      UUID electionId,
      String titre,
      int totalVotes,
      double participationRate,
      List<CandidateVoteCount> candidateVotes,
      String statut
  ) {}

  public record CandidateVoteCount(UUID candidatId, int votes) {}
}

