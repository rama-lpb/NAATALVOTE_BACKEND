package com.naatalvote.domain.fraud;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Suspension {
  private final UUID id;
  private final UUID citoyenId;
  private final String motif;
  private final UUID operateurId;
  private final UUID superadminId;
  private final SuspensionStatus statut;
  private final Instant dateCreation;
  private final Instant dateDecision;
  private final String justification;

  public Suspension(
      UUID id,
      UUID citoyenId,
      String motif,
      UUID operateurId,
      UUID superadminId,
      SuspensionStatus statut,
      Instant dateCreation,
      Instant dateDecision,
      String justification
  ) {
    this.id = Objects.requireNonNull(id, "id");
    this.citoyenId = Objects.requireNonNull(citoyenId, "citoyenId");
    this.motif = motif == null ? "" : motif;
    this.operateurId = Objects.requireNonNull(operateurId, "operateurId");
    this.superadminId = superadminId;
    this.statut = Objects.requireNonNull(statut, "statut");
    this.dateCreation = Objects.requireNonNull(dateCreation, "dateCreation");
    this.dateDecision = dateDecision;
    this.justification = justification == null ? "" : justification;
  }

  public UUID id() {
    return id;
  }

  public UUID citoyenId() {
    return citoyenId;
  }

  public String motif() {
    return motif;
  }

  public UUID operateurId() {
    return operateurId;
  }

  public UUID superadminId() {
    return superadminId;
  }

  public SuspensionStatus statut() {
    return statut;
  }

  public Instant dateCreation() {
    return dateCreation;
  }

  public Instant dateDecision() {
    return dateDecision;
  }

  public String justification() {
    return justification;
  }
}

