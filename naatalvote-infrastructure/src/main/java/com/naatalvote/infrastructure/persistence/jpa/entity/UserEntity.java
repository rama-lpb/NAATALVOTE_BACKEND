package com.naatalvote.infrastructure.persistence.jpa.entity;

import com.naatalvote.domain.auth.UserRole;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "utilisateur")
public class UserEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "numero_cni", nullable = false, unique = true)
  private String cni;

  @Column(name = "nom", nullable = false)
  private String nom;

  @Column(name = "prenom", nullable = false)
  private String prenom;

  @Column(name = "email", nullable = false)
  private String email;

  @ElementCollection
  @CollectionTable(name = "utilisateur_telephone", joinColumns = @JoinColumn(name = "utilisateur_id"))
  @Column(name = "telephone", nullable = false)
  private List<String> telephones = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "utilisateur_role", joinColumns = @JoinColumn(name = "utilisateur_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private List<UserRole> roles = new ArrayList<>();

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCni() {
    return cni;
  }

  public void setCni(String cni) {
    this.cni = cni;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<String> getTelephones() {
    return telephones;
  }

  public void setTelephones(List<String> telephones) {
    this.telephones = telephones;
  }

  public List<UserRole> getRoles() {
    return roles;
  }

  public void setRoles(List<UserRole> roles) {
    this.roles = roles;
  }
}

