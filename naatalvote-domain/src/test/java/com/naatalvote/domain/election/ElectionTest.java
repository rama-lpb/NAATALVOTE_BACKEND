package com.naatalvote.domain.election;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.naatalvote.domain.common.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ElectionTest {
  @Test
  void rejectsEndBeforeStart() {
    Instant start = Instant.parse("2026-01-02T10:00:00Z");
    Instant end = Instant.parse("2026-01-02T09:00:00Z");
    assertThrows(DomainException.class, () -> new Election(
        UUID.randomUUID(),
        "Test",
        "Desc",
        ElectionType.PRESIDENTIELLE,
        start,
        end,
        ElectionStatus.PROGRAMMEE,
        UUID.randomUUID()
    ));
  }
}

