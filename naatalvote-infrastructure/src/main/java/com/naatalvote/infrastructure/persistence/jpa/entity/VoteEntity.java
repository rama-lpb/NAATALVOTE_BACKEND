package com.naatalvote.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vote")
public class VoteEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "token_anonyme", nullable = false, unique = true)
  private UUID tokenAnonyme;

  @Column(name = "election_id", nullable = false)
  private UUID electionId;

  @Column(name = "candidat_id", nullable = false)
  private UUID candidatId;

  @Column(name = "horodatage", nullable = false)
  private Instant horodatage;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getTokenAnonyme() {
    return tokenAnonyme;
  }

  public void setTokenAnonyme(UUID tokenAnonyme) {
    this.tokenAnonyme = tokenAnonyme;
  }

  public UUID getElectionId() {
    return electionId;
  }

  public void setElectionId(UUID electionId) {
    this.electionId = electionId;
  }

  public UUID getCandidatId() {
    return candidatId;
  }

  public void setCandidatId(UUID candidatId) {
    this.candidatId = candidatId;
  }

  public Instant getHorodatage() {
    return horodatage;
  }

  public void setHorodatage(Instant horodatage) {
    this.horodatage = horodatage;
  }
}

