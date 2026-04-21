package com.naatalvote.domain.audit;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ActionLog {
  private final UUID id;
  private final ActionType type;
  private final UUID utilisateurId;
  private final String description;
  private final Instant horodatage;
  private final String adresseIp;
  private final String signature;

  public ActionLog(
      UUID id,
      ActionType type,
      UUID utilisateurId,
      String description,
      Instant horodatage,
      String adresseIp,
      String signature
  ) {
    this.id = Objects.requireNonNull(id, "id");
    this.type = Objects.requireNonNull(type, "type");
    this.utilisateurId = Objects.requireNonNull(utilisateurId, "utilisateurId");
    this.description = description == null ? "" : description;
    this.horodatage = Objects.requireNonNull(horodatage, "horodatage");
    this.adresseIp = adresseIp;
    this.signature = signature;
  }

  public UUID id() {
    return id;
  }

  public ActionType type() {
    return type;
  }

  public UUID utilisateurId() {
    return utilisateurId;
  }

  public String description() {
    return description;
  }

  public Instant horodatage() {
    return horodatage;
  }

  public String adresseIp() {
    return adresseIp;
  }

  public String signature() {
    return signature;
  }
}
