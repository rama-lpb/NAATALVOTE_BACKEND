package com.naatalvote.infrastructure.persistence.jpa.entity;

import com.naatalvote.domain.fraud.SuspensionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "suspension")
public class SuspensionEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "citoyen_id", nullable = false)
  private UUID citoyenId;

  @Column(name = "motif", nullable = false)
  private String motif;

  @Column(name = "operateur_id", nullable = false)
  private UUID operateurId;

  @Column(name = "superadmin_id")
  private UUID superadminId;

  @Enumerated(EnumType.STRING)
  @Column(name = "statut", nullable = false)
  private SuspensionStatus statut;

  @Column(name = "date_creation", nullable = false)
  private Instant dateCreation;

  @Column(name = "date_decision")
  private Instant dateDecision;

  @Column(name = "justification", nullable = false)
  private String justification;

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

  public String getMotif() {
    return motif;
  }

  public void setMotif(String motif) {
    this.motif = motif;
  }

  public UUID getOperateurId() {
    return operateurId;
  }

  public void setOperateurId(UUID operateurId) {
    this.operateurId = operateurId;
  }

  public UUID getSuperadminId() {
    return superadminId;
  }

  public void setSuperadminId(UUID superadminId) {
    this.superadminId = superadminId;
  }

  public SuspensionStatus getStatut() {
    return statut;
  }

  public void setStatut(SuspensionStatus statut) {
    this.statut = statut;
  }

  public Instant getDateCreation() {
    return dateCreation;
  }

  public void setDateCreation(Instant dateCreation) {
    this.dateCreation = dateCreation;
  }

  public Instant getDateDecision() {
    return dateDecision;
  }

  public void setDateDecision(Instant dateDecision) {
    this.dateDecision = dateDecision;
  }

  public String getJustification() {
    return justification;
  }

  public void setJustification(String justification) {
    this.justification = justification;
  }
}

