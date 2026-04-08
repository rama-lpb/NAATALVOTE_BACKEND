package com.naatalvote.application.vote;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.domain.common.DomainException;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.application.support.memory.InMemoryTraceVoteRepository;
import com.naatalvote.application.support.memory.InMemoryVoteRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VoteServiceTest {
  @Test
  void preventsMultipleVoteSameElection() {
    UUID electionId = Ids.newUuid();
    UUID candidateId = Ids.newUuid();
    UUID citizenId = Ids.newUuid();

    InMemoryVoteRepository votes = new InMemoryVoteRepository();
    InMemoryTraceVoteRepository traces = new InMemoryTraceVoteRepository();
    TimeProvider time = () -> Instant.parse("2026-04-01T10:00:00Z");
    VoteService svc = new VoteService(votes, traces, time);

    svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, citizenId, null));
    assertThrows(DomainException.class, () -> svc.cast(new VoteService.CastVoteCommand(electionId, candidateId, citizenId, null)));
  }
}
