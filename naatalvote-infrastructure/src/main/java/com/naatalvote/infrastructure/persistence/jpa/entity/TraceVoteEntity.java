package com.naatalvote.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trace_vote")
public class TraceVoteEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "citoyen_id", nullable = false)
  private UUID citoyenId;

  @Column(name = "election_id", nullable = false)
  private UUID electionId;

  @Column(name = "a_vote", nullable = false)
  private boolean aVote;

  @Column(name = "horodatage")
  private Instant horodatage;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getCitoyenId() {
    return citoyenId;
  }

  public void setCitoyenId(UUID citoyenId) {
    this.citoyenId = citoyenId;
  }

  public UUID getElectionId() {
    return electionId;
  }

  public void setElectionId(UUID electionId) {
    this.electionId = electionId;
  }

  public boolean isAVote() {
    return aVote;
  }

  public void setAVote(boolean aVote) {
    this.aVote = aVote;
  }

  public Instant getHorodatage() {
    return horodatage;
  }

  public void setHorodatage(Instant horodatage) {
    this.horodatage = horodatage;
  }
}

