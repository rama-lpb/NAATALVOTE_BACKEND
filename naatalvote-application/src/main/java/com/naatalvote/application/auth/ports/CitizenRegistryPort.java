package com.naatalvote.application.auth.ports;

import java.util.List;
import java.util.Optional;

/**
 * External registry (AppDAFF simulation): fetch citizen/user identity data by CNI.
 */
public interface CitizenRegistryPort {
  Optional<CitizenRecord> findByCni(String cni);

  record CitizenRecord(
      String cni,
      String nom,
      String prenom,
      String email,
      List<String> telephones,
      String date_naissance,
      String adresse,
      List<String> roles
  ) {}
}

