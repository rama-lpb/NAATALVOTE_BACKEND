package com.naatalvote.application.election;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.domain.common.DomainException;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.domain.election.ElectionType;
import com.naatalvote.application.support.memory.InMemoryCandidateRepository;
import com.naatalvote.application.support.memory.InMemoryElectionRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ElectionServiceTest {
  @Test
  void blocksUpdateAtTMinus48h() {
    UUID adminId = Ids.newUuid();
    Instant now = Instant.parse("2026-04-01T10:00:00Z");
    Instant start = now.plusSeconds(48 * 3600); // exactly at freeze boundary
    Instant end = start.plusSeconds(3600);

    InMemoryElectionRepository elections = new InMemoryElectionRepository();
    InMemoryCandidateRepository candidates = new InMemoryCandidateRepository();
    TimeProvider time = () -> now;

    ElectionService svc = new ElectionService(elections, candidates, time);
    UUID electionId = svc.createElection(new ElectionService.CreateElectionCommand(adminId, "E", "D", ElectionType.PRESIDENTIELLE, start, end));

    assertThrows(DomainException.class, () -> svc.updateElection(electionId, new ElectionService.UpdateElectionCommand("X", null, null, null, null)));
  }
}
