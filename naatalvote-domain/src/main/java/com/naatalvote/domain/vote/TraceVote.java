package com.naatalvote.domain.vote;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class TraceVote {
  private final UUID id;
  private final UUID citoyenId;
  private final UUID electionId;
  private final boolean aVote;
  private final Instant horodatage;

  public TraceVote(UUID id, UUID citoyenId, UUID electionId, boolean aVote, Instant horodatage) {
    this.id = Objects.requireNonNull(id, "id");
    this.citoyenId = Objects.requireNonNull(citoyenId, "citoyenId");
    this.electionId = Objects.requireNonNull(electionId, "electionId");
    this.aVote = aVote;
    this.horodatage = horodatage;
  }

  public UUID id() {
    return id;
  }

  public UUID citoyenId() {
    return citoyenId;
  }

  public UUID electionId() {
    return electionId;
  }

  public boolean aVote() {
    return aVote;
  }

  public Instant horodatage() {
    return horodatage;
  }
}

