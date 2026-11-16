package com.naatalvote.application.election;

import static org.junit.jupiter.api.Assertions.*;

import com.naatalvote.application.support.memory.InMemoryCandidateRepository;
import com.naatalvote.application.support.memory.InMemoryElectionRepository;
import com.naatalvote.domain.common.DomainException;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.election.ElectionType;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Règles de programmation et de gel (tests ajoutés lors de l'audit E221). */
class ElectionFreezeRulesTest {
  final Instant now = Instant.parse("2026-11-16T10:00:00Z");
  final ElectionService svc = new ElectionService(new InMemoryElectionRepository(), new InMemoryCandidateRepository(), () -> now);

  UUID create(Duration startIn) {
    return svc.createElection(new ElectionService.CreateElectionCommand(Ids.newUuid(), "Municipales", "", ElectionType.MUNICIPALE,
        now.plus(startIn), now.plus(startIn).plus(Duration.ofHours(10)), "Dakar", 100));
  }

  @Test
  void cannotCreateAnElectionInThePast() {
    assertThrows(DomainException.class, () -> create(Duration.ofHours(-1)));
  }

  @Test
  void cannotBypassTheFreezeByMovingTheStartDateCloser() {
    UUID id = create(Duration.ofDays(10));
    var cmd = new ElectionService.UpdateElectionCommand(null, null, null, now.plus(Duration.ofHours(1)), now.plus(Duration.ofHours(5)), null, null);
    assertThrows(DomainException.class, () -> svc.updateElection(id, cmd));
  }

  @Test
  void canPostponeAnElectionOutsideTheFreezeWindow() {
    UUID id = create(Duration.ofDays(10));
    var cmd = new ElectionService.UpdateElectionCommand(null, null, null, now.plus(Duration.ofDays(12)), now.plus(Duration.ofDays(13)), null, null);
    assertDoesNotThrow(() -> svc.updateElection(id, cmd));
  }
}
