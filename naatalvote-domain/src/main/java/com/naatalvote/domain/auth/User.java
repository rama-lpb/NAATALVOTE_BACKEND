package com.naatalvote.domain.auth;

import com.naatalvote.domain.common.DomainException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class User {
  private final UUID id;
  private final String cni;
  private final String nom;
  private final String prenom;
  private final String email;
  private final List<String> telephones;
  private final List<UserRole> roles;
  private final String dateNaissance;
  private final String adresse;

  public User(
      UUID id,
      String cni,
      String nom,
      String prenom,
      String email,
      List<String> telephones,
      List<UserRole> roles
  ) {
    this(id, cni, nom, prenom, email, telephones, roles, "", "");
  }

  public User(
      UUID id,
      String cni,
      String nom,
      String prenom,
      String email,
      List<String> telephones,
      List<UserRole> roles,
      String dateNaissance,
      String adresse
  ) {
    this.id = Objects.requireNonNull(id, "id");
    this.cni = requireNonBlank(cni, "cni");
    this.nom = requireNonBlank(nom, "nom");
    this.prenom = requireNonBlank(prenom, "prenom");
    this.email = email == null ? "" : email;
    this.telephones = List.copyOf(telephones == null ? List.of() : telephones);
    this.roles = List.copyOf(Objects.requireNonNull(roles, "roles"));
    if (this.roles.isEmpty()) {
      throw new DomainException("roles ne peut pas être vide");
    }
    this.dateNaissance = dateNaissance == null ? "" : dateNaissance;
    this.adresse = adresse == null ? "" : adresse;
  }

  public UUID id() {
    return id;
  }

  public String cni() {
    return cni;
  }

  public String nom() {
    return nom;
  }

  public String prenom() {
    return prenom;
  }

  public String email() {
    return email;
  }

  public List<String> telephones() {
    return telephones;
  }

  public List<UserRole> roles() {
    return roles;
  }

  public String dateNaissance() {
    return dateNaissance;
  }

  public String adresse() {
    return adresse;
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.trim().isEmpty()) {
      throw new DomainException(field + " est requis");
    }
    return value.trim();
  }
}

