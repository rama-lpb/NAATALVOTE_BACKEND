package com.naatalvote.infrastructure.persistence.jpa.entity;

import com.naatalvote.domain.audit.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "action_log")
public class ActionLogEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type_action", nullable = false)
  private ActionType type;

  @Column(name = "utilisateur_id", nullable = false)
  private UUID utilisateurId;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "horodatage", nullable = false)
  private Instant horodatage;

  @Column(name = "adresse_ip")
  private String adresseIp;

  @Column(name = "signature_cryptographique")
  private String signature;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ActionType getType() {
    return type;
  }

  public void setType(ActionType type) {
    this.type = type;
  }

  public UUID getUtilisateurId() {
    return utilisateurId;
  }

  public void setUtilisateurId(UUID utilisateurId) {
    this.utilisateurId = utilisateurId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getHorodatage() {
    return horodatage;
  }

  public void setHorodatage(Instant horodatage) {
    this.horodatage = horodatage;
  }

  public String getAdresseIp() {
    return adresseIp;
  }

  public void setAdresseIp(String adresseIp) {
    this.adresseIp = adresseIp;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }
}

