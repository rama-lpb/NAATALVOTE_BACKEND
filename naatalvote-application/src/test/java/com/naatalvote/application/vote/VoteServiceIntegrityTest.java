package com.naatalvote.application.vote;

import static org.junit.jupiter.api.Assertions.*;

import com.naatalvote.application.support.memory.*;
import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.election.*;
import com.naatalvote.domain.vote.TraceVote;
import com.naatalvote.domain.vote.Vote;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Intégrité et anonymat du vote (tests ajoutés lors de l'audit E221). */
class VoteServiceIntegrityTest {
  final UUID electionId = Ids.newUuid();
  final UUID candidateId = Ids.newUuid();
  final Instant now = Instant.parse("2026-11-16T10:37:42Z");
  final InMemoryElectionRepository elections = new InMemoryElectionRepository();
  final InMemoryCandidateRepository candidates = new InMemoryCandidateRepository();
  final InMemoryVoteRepository votes = new InMemoryVoteRepository();

  VoteServiceIntegrityTest() {
    elections.save(new Election(electionId, "Municipales", "t", ElectionType.MUNICIPALE, now.minusSeconds(3600),
        now.plusSeconds(86400), ElectionStatus.EN_COURS, Ids.newUuid(), List.of(candidateId), "Dakar", 10, 0));
    candidates.save(new Candidate(candidateId, electionId, "Sow", "Ali", "P", "", "", ""));
  }

  @Test
  void noBallotIsWrittenWhenTheParticipationTraceIsRejected() {
    // Simule la contrainte UNIQUE(citoyen_id, election_id) levée par une requête concurrente
    TraceVoteRepositoryPort rejecting = new TraceVoteRepositoryPort() {
      @Override public TraceVote save(TraceVote t) { throw new IllegalStateException("unique violation"); }
      @Override public Optional<TraceVote> findByCitoyenIdAndElectionId(UUID c, UUID e) { return Optional.empty(); }
      @Override public List<TraceVote> findByCitoyenId(UUID c) { return List.of(); }
    };
    VoteService svc = new VoteService(elections, candidates, votes, rejecting, () -> now);
    assertThrows(IllegalStateException.class, () -> svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, Ids.newUuid(), null)));
    assertTrue(votes.findByElectionId(electionId).isEmpty(), "aucun bulletin ne doit exister sans trace");
  }

  @Test
  void ballotTimestampIsRoundedSoItCannotBeMatchedWithTheTrace() {
    InMemoryTraceVoteRepository traces = new InMemoryTraceVoteRepository();
    VoteService svc = new VoteService(elections, candidates, votes, traces, () -> now);
    UUID citizen = Ids.newUuid();
    svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, citizen, null));
    Vote ballot = votes.findByElectionId(electionId).get(0);
    TraceVote trace = traces.findByCitoyenIdAndElectionId(citizen, electionId).orElseThrow();
    assertEquals(now.truncatedTo(ChronoUnit.HOURS), ballot.horodatage());
    assertNotEquals(trace.horodatage(), ballot.horodatage());
  }

  @Test
  void ballotModelContainsNoVoterIdentity() {
    List<String> fields = new ArrayList<>();
    if (Vote.class.isRecord()) {
      for (RecordComponent c : Vote.class.getRecordComponents()) fields.add(c.getName().toLowerCase());
    } else {
      for (var f : Vote.class.getDeclaredFields()) fields.add(f.getName().toLowerCase());
    }
    fields.forEach(f -> assertFalse(f.contains("citoyen") || f.contains("user") || f.contains("cni"), "champ interdit dans Vote : " + f));
  }

  @Test
  void resultsCountOneBallotPerVoter() {
    InMemoryTraceVoteRepository traces = new InMemoryTraceVoteRepository();
    VoteService svc = new VoteService(elections, candidates, votes, traces, () -> now);
    for (int i = 0; i < 3; i++) svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, Ids.newUuid(), null));
    VoteService.Results r = svc.results(electionId);
    assertEquals(3, r.totalVotes());
    assertEquals(100.0, r.results().get(0).percent(), 0.001);
  }
}
