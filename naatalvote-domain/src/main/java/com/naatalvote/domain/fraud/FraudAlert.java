package com.naatalvote.domain.fraud;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class FraudAlert {
  private final UUID id;
  private final FraudType type;
  private final UUID citoyenId;
  private final UUID electionId;
  private final String description;
  private final FraudAlertStatus statut;
  private final Instant dateDetection;
  private final UUID operateurId;
  private final Instant dateTraitement;
  private final String ip;

  public FraudAlert(
      UUID id,
      FraudType type,
      UUID citoyenId,
      UUID electionId,
      String description,
      FraudAlertStatus statut,
      Instant dateDetection,
      UUID operateurId,
      Instant dateTraitement,
      String ip
  ) {
    this.id = Objects.requireNonNull(id, "id");
    this.type = Objects.requireNonNull(type, "type");
    this.citoyenId = citoyenId;
    this.electionId = electionId;
    this.description = description == null ? "" : description;
    this.statut = Objects.requireNonNull(statut, "statut");
    this.dateDetection = Objects.requireNonNull(dateDetection, "dateDetection");
    this.operateurId = operateurId;
    this.dateTraitement = dateTraitement;
    this.ip = ip;
  }

  public UUID id() {
    return id;
  }

  public FraudType type() {
    return type;
  }

  public UUID citoyenId() {
    return citoyenId;
  }

  public UUID electionId() {
    return electionId;
  }

  public String description() {
    return description;
  }

  public FraudAlertStatus statut() {
    return statut;
  }

  public Instant dateDetection() {
    return dateDetection;
  }

  public UUID operateurId() {
    return operateurId;
  }

  public Instant dateTraitement() {
    return dateTraitement;
  }

  public String ip() {
    return ip;
  }
}

