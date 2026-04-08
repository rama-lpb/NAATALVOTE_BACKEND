package com.naatalvote.domain.election;

import com.naatalvote.domain.common.DomainException;
import java.util.Objects;
import java.util.UUID;

public final class Candidate {
  private final UUID id;
  private final UUID electionId;
  private final String nom;
  private final String prenom;
  private final String partiPolitique;
  private final String biographie;
  private final String photoUrl;
  private final String programmeUrl;
  private final int votesCount;
  private final String color;

  public Candidate(
      UUID id,
      UUID electionId,
      String nom,
      String prenom,
      String partiPolitique,
      String biographie,
      String photoUrl,
      String programmeUrl
  ) {
    this(id, electionId, nom, prenom, partiPolitique, biographie, photoUrl, programmeUrl, 0, "");
  }

  public Candidate(
      UUID id,
      UUID electionId,
      String nom,
      String prenom,
      String partiPolitique,
      String biographie,
      String photoUrl,
      String programmeUrl,
      int votesCount,
      String color
  ) {
    this.id = Objects.requireNonNull(id, "id");
    this.electionId = Objects.requireNonNull(electionId, "electionId");
    this.nom = requireNonBlank(nom, "nom");
    this.prenom = requireNonBlank(prenom, "prenom");
    this.partiPolitique = partiPolitique == null ? "" : partiPolitique;
    this.biographie = biographie == null ? "" : biographie;
    this.photoUrl = photoUrl == null ? "" : photoUrl;
    this.programmeUrl = programmeUrl == null ? "" : programmeUrl;
    this.votesCount = votesCount;
    this.color = color == null ? "" : color;
  }

  public UUID id() {
    return id;
  }

  public UUID electionId() {
    return electionId;
  }

  public String nom() {
    return nom;
  }

  public String prenom() {
    return prenom;
  }

  public String partiPolitique() {
    return partiPolitique;
  }

  public String biographie() {
    return biographie;
  }

  public String photoUrl() {
    return photoUrl;
  }

  public String programmeUrl() {
    return programmeUrl;
  }

  public int votesCount() {
    return votesCount;
  }

  public String color() {
    return color;
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.trim().isEmpty()) {
      throw new DomainException(field + " est requis");
    }
    return value.trim();
  }
}

