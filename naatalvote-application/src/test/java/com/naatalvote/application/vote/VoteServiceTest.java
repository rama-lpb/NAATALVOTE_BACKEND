package com.naatalvote.application.vote;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.application.support.memory.InMemoryCandidateRepository;
import com.naatalvote.application.support.memory.InMemoryElectionRepository;
import com.naatalvote.domain.common.DomainException;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.election.Candidate;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.domain.election.ElectionType;
import com.naatalvote.application.support.memory.InMemoryTraceVoteRepository;
import com.naatalvote.application.support.memory.InMemoryVoteRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VoteServiceTest {
  @Test
  void preventsMultipleVoteSameElection() {
    UUID electionId = Ids.newUuid();
    UUID candidateId = Ids.newUuid();
    UUID citizenId = Ids.newUuid();
    UUID adminId = Ids.newUuid();

    InMemoryElectionRepository elections = new InMemoryElectionRepository();
    elections.save(new Election(
        electionId,
        "Présidentielle 2026",
        "Test",
        ElectionType.PRESIDENTIELLE,
        Instant.parse("2026-04-01T08:00:00Z"),
        Instant.parse("2026-04-02T08:00:00Z"),
        ElectionStatus.EN_COURS,
        adminId,
        List.of(candidateId),
        "Dakar",
        100,
        0
    ));
    InMemoryCandidateRepository candidates = new InMemoryCandidateRepository();
    candidates.save(new Candidate(candidateId, electionId, "Diop", "Awa", "Parti A", "", "", ""));
    InMemoryVoteRepository votes = new InMemoryVoteRepository();
    InMemoryTraceVoteRepository traces = new InMemoryTraceVoteRepository();
    TimeProvider time = () -> Instant.parse("2026-04-01T10:00:00Z");
    VoteService svc = new VoteService(elections, candidates, votes, traces, time);

    svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, citizenId, null));
    assertThrows(DomainException.class, () -> svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, citizenId, null)));
  }

  @Test
  void blocksVoteWhenElectionNotInProgress() {
    UUID electionId = Ids.newUuid();
    UUID candidateId = Ids.newUuid();
    UUID citizenId = Ids.newUuid();
    UUID adminId = Ids.newUuid();

    InMemoryElectionRepository elections = new InMemoryElectionRepository();
    elections.save(new Election(
        electionId,
        "Municipales",
        "Test",
        ElectionType.MUNICIPALE,
        Instant.parse("2026-06-01T08:00:00Z"),
        Instant.parse("2026-06-01T18:00:00Z"),
        ElectionStatus.PROGRAMMEE,
        adminId,
        List.of(candidateId),
        "Thiès",
        100,
        0
    ));
    InMemoryCandidateRepository candidates = new InMemoryCandidateRepository();
    candidates.save(new Candidate(candidateId, electionId, "Fall", "Mamadou", "Parti B", "", "", ""));
    InMemoryVoteRepository votes = new InMemoryVoteRepository();
    InMemoryTraceVoteRepository traces = new InMemoryTraceVoteRepository();
    TimeProvider time = () -> Instant.parse("2026-04-01T10:00:00Z");
    VoteService svc = new VoteService(elections, candidates, votes, traces, time);

    assertThrows(DomainException.class, () -> svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, citizenId, null)));
  }

  @Test
  void blocksVoteForCandidateFromAnotherElection() {
    UUID electionId = Ids.newUuid();
    UUID otherElectionId = Ids.newUuid();
    UUID candidateId = Ids.newUuid();
    UUID citizenId = Ids.newUuid();
    UUID adminId = Ids.newUuid();

    InMemoryElectionRepository elections = new InMemoryElectionRepository();
    elections.save(new Election(
        electionId,
        "Législatives",
        "Test",
        ElectionType.LEGISLATIVE,
        Instant.parse("2026-04-01T08:00:00Z"),
        Instant.parse("2026-04-02T08:00:00Z"),
        ElectionStatus.EN_COURS,
        adminId,
        List.of(),
        "Dakar",
        100,
        0
    ));
    elections.save(new Election(
        otherElectionId,
        "Régionales",
        "Test",
        ElectionType.REGIONALE,
        Instant.parse("2026-04-01T08:00:00Z"),
        Instant.parse("2026-04-02T08:00:00Z"),
        ElectionStatus.EN_COURS,
        adminId,
        List.of(candidateId),
        "Saint-Louis",
        100,
        0
    ));
    InMemoryCandidateRepository candidates = new InMemoryCandidateRepository();
    candidates.save(new Candidate(candidateId, otherElectionId, "Ndiaye", "Khady", "Parti C", "", "", ""));
    InMemoryVoteRepository votes = new InMemoryVoteRepository();
    InMemoryTraceVoteRepository traces = new InMemoryTraceVoteRepository();
    TimeProvider time = () -> Instant.parse("2026-04-01T10:00:00Z");
    VoteService svc = new VoteService(elections, candidates, votes, traces, time);

    assertThrows(DomainException.class, () -> svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, citizenId, null)));
  }
}
