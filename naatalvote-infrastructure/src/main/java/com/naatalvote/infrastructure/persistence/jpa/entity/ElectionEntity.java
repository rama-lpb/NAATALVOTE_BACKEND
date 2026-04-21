package com.naatalvote.infrastructure.persistence.jpa.entity;

import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.domain.election.ElectionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "election")
public class ElectionEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "titre", nullable = false)
  private String titre;

  @Column(name = "description", nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "type_election", nullable = false)
  private ElectionType type;

  @Column(name = "date_debut", nullable = false)
  private Instant dateDebut;

  @Column(name = "date_fin", nullable = false)
  private Instant dateFin;

  @Enumerated(EnumType.STRING)
  @Column(name = "statut", nullable = false)
  private ElectionStatus statut;

  @Column(name = "admin_id", nullable = false)
  private UUID adminId;

  @Column(name = "region", nullable = false)
  private String region = "";

  @Column(name = "total_electeurs", nullable = false)
  private int totalElecteurs = 0;

  @Column(name = "votes_count", nullable = false)
  private int votesCount = 0;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitre() {
    return titre;
  }

  public void setTitre(String titre) {
    this.titre = titre;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ElectionType getType() {
    return type;
  }

  public void setType(ElectionType type) {
    this.type = type;
  }

  public Instant getDateDebut() {
    return dateDebut;
  }

  public void setDateDebut(Instant dateDebut) {
    this.dateDebut = dateDebut;
  }

  public Instant getDateFin() {
    return dateFin;
  }

  public void setDateFin(Instant dateFin) {
    this.dateFin = dateFin;
  }

  public ElectionStatus getStatut() {
    return statut;
  }

  public void setStatut(ElectionStatus statut) {
    this.statut = statut;
  }

  public UUID getAdminId() {
    return adminId;
  }

  public void setAdminId(UUID adminId) {
    this.adminId = adminId;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public int getTotalElecteurs() {
    return totalElecteurs;
  }

  public void setTotalElecteurs(int totalElecteurs) {
    this.totalElecteurs = totalElecteurs;
  }

  public int getVotesCount() {
    return votesCount;
  }

  public void setVotesCount(int votesCount) {
    this.votesCount = votesCount;
  }
}
