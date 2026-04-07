package com.naatalvote.domain.vote;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Vote {
  private final UUID id;
  private final UUID tokenAnonyme;
  private final UUID electionId;
  private final UUID candidatId;
  private final Instant horodatage;

  public Vote(UUID id, UUID tokenAnonyme, UUID electionId, UUID candidatId, Instant horodatage) {
    this.id = Objects.requireNonNull(id, "id");
    this.tokenAnonyme = Objects.requireNonNull(tokenAnonyme, "tokenAnonyme");
    this.electionId = Objects.requireNonNull(electionId, "electionId");
    this.candidatId = Objects.requireNonNull(candidatId, "candidatId");
    this.horodatage = Objects.requireNonNull(horodatage, "horodatage");
  }

  public UUID id() {
    return id;
  }

  public UUID tokenAnonyme() {
    return tokenAnonyme;
  }

  public UUID electionId() {
    return electionId;
  }

  public UUID candidatId() {
    return candidatId;
  }

  public Instant horodatage() {
    return horodatage;
  }
}

