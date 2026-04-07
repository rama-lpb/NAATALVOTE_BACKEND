package com.naatalvote.infrastructure.persistence.jpa.entity;

import com.naatalvote.domain.fraud.FraudAlertStatus;
import com.naatalvote.domain.fraud.FraudType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraude")
public class FraudAlertEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type_fraude", nullable = false)
  private FraudType type;

  @Column(name = "citoyen_id")
  private UUID citoyenId;

  @Column(name = "election_id")
  private UUID electionId;

  @Column(name = "description", nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "statut", nullable = false)
  private FraudAlertStatus statut;

  @Column(name = "date_detection", nullable = false)
  private Instant dateDetection;

  @Column(name = "operateur_id")
  private UUID operateurId;

  @Column(name = "date_traitement")
  private Instant dateTraitement;

  @Column(name = "ip")
  private String ip;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public FraudType getType() {
    return type;
  }

  public void setType(FraudType type) {
    this.type = type;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public FraudAlertStatus getStatut() {
    return statut;
  }

  public void setStatut(FraudAlertStatus statut) {
    this.statut = statut;
  }

  public Instant getDateDetection() {
    return dateDetection;
  }

  public void setDateDetection(Instant dateDetection) {
    this.dateDetection = dateDetection;
  }

  public UUID getOperateurId() {
    return operateurId;
  }

  public void setOperateurId(UUID operateurId) {
    this.operateurId = operateurId;
  }

  public Instant getDateTraitement() {
    return dateTraitement;
  }

  public void setDateTraitement(Instant dateTraitement) {
    this.dateTraitement = dateTraitement;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }
}

