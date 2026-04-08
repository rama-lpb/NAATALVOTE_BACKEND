package com.naatalvote.application.admin;

import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class AdminService {
  private final ElectionRepositoryPort elections;
  private final VoteRepositoryPort votes;

  public AdminService(ElectionRepositoryPort elections, VoteRepositoryPort votes) {
    this.elections = Objects.requireNonNull(elections, "elections");
    this.votes = Objects.requireNonNull(votes, "votes");
  }

  public List<Election> listAdminElections(UUID adminId) {
    return elections.findAll().stream().filter(e -> adminId == null || e.adminId().equals(adminId)).toList();
  }

  public ElectionStats getElectionStats(UUID electionId) {
    Election e = elections.findById(electionId).orElseThrow(() -> new IllegalArgumentException("Élection non trouvée"));
    List<?> electionVotes = votes.findByElectionId(electionId);
    int totalVotes = electionVotes.size();
    
    Map<UUID, Long> votesByCandidate = electionVotes.stream()
        .collect(Collectors.groupingBy(v -> ((com.naatalvote.domain.vote.Vote)v).candidatId(), Collectors.counting()));
    
    List<CandidateVoteCount> candidateCounts = votesByCandidate.entrySet().stream()
        .map(entry -> new CandidateVoteCount(entry.getKey(), entry.getValue().intValue()))
        .toList();
    
    double participationRate = 0.0;
    if (e.statut() == ElectionStatus.CLOTUREE) {
      participationRate = 100.0;
    } else if (e.statut() == ElectionStatus.EN_COURS) {
      long daysRunning = Instant.now().getEpochSecond() - e.dateDebut().getEpochSecond();
      if (daysRunning > 0) {
        double estimatedTotal = (double) totalVotes * 86400 / daysRunning;
        participationRate = Math.min(100.0, (totalVotes / estimatedTotal) * 100);
      }
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

