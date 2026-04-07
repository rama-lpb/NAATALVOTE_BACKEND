package com.naatalvote.domain.election;

import com.naatalvote.domain.common.DomainException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Election {
  private final UUID id;
  private final String titre;
  private final String description;
  private final ElectionType type;
  private final Instant dateDebut;
  private final Instant dateFin;
  private final ElectionStatus statut;
  private final UUID adminId;

  public Election(
      UUID id,
      String titre,
      String description,
      ElectionType type,
      Instant dateDebut,
      Instant dateFin,
      ElectionStatus statut,
      UUID adminId
  ) {
    this.id = Objects.requireNonNull(id, "id");
    this.titre = requireNonBlank(titre, "titre");
    this.description = description == null ? "" : description;
    this.type = Objects.requireNonNull(type, "type");
    this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut");
    this.dateFin = Objects.requireNonNull(dateFin, "dateFin");
    if (!dateFin.isAfter(dateDebut)) {
      throw new DomainException("dateFin doit être > dateDebut");
    }
    this.statut = Objects.requireNonNull(statut, "statut");
    this.adminId = Objects.requireNonNull(adminId, "adminId");
  }

  public UUID id() {
    return id;
  }

  public String titre() {
    return titre;
  }

  public String description() {
    return description;
  }

  public ElectionType type() {
    return type;
  }

  public Instant dateDebut() {
    return dateDebut;
  }

  public Instant dateFin() {
    return dateFin;
  }

  public ElectionStatus statut() {
    return statut;
  }

  public UUID adminId() {
    return adminId;
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.trim().isEmpty()) {
      throw new DomainException(field + " est requis");
    }
    return value.trim();
  }
}

