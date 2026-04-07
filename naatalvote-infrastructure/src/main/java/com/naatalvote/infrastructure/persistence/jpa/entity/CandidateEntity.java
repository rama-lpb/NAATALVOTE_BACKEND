package com.naatalvote.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "candidat")
public class CandidateEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "election_id", nullable = false)
  private UUID electionId;

  @Column(name = "nom", nullable = false)
  private String nom;

  @Column(name = "prenom", nullable = false)
  private String prenom;

  @Column(name = "parti_politique", nullable = false)
  private String partiPolitique;

  @Column(name = "biographie", nullable = false)
  private String biographie;

  @Column(name = "photo_url", nullable = false)
  private String photoUrl;

  @Column(name = "programme_url", nullable = false)
  private String programmeUrl;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getElectionId() {
    return electionId;
  }

  public void setElectionId(UUID electionId) {
    this.electionId = electionId;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public String getPrenom() {
    return prenom;
  }

  public void setPrenom(String prenom) {
    this.prenom = prenom;
  }

  public String getPartiPolitique() {
    return partiPolitique;
  }

  public void setPartiPolitique(String partiPolitique) {
    this.partiPolitique = partiPolitique;
  }

  public String getBiographie() {
    return biographie;
  }

  public void setBiographie(String biographie) {
    this.biographie = biographie;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public String getProgrammeUrl() {
    return programmeUrl;
  }

  public void setProgrammeUrl(String programmeUrl) {
    this.programmeUrl = programmeUrl;
  }
}

