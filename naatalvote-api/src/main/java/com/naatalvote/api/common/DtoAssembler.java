package com.naatalvote.api.common;

import com.naatalvote.domain.election.Candidate;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.domain.vote.Vote;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DtoAssembler {

  public static ElectionDto toElectionDto(Election e) {
    return new ElectionDto(
        e.id().toString(),
        e.titre(),
        e.description(),
        e.type().name(),
        e.statut().name(),
        e.dateDebut().toString(),
        e.dateFin().toString(),
        e.adminId().toString(),
        e.candidatIds().stream().map(UUID::toString).toList(),
        e.region() != null ? e.region() : "",
        e.totalElecteurs(),
        e.votesCount()
    );
  }

  public static CandidateDto toCandidateDto(Candidate c) {
    return new CandidateDto(
        c.id().toString(),
        c.electionId().toString(),
        c.nom(),
        c.prenom(),
        c.partiPolitique(),
        c.biographie(),
        c.photoUrl(),
        c.programmeUrl(),
        c.votesCount(),
        c.color()
    );
  }

  public static VoteResultDto toVoteResultDto(UUID electionId, int total, List<VoteResultLineDto> lines) {
    return new VoteResultDto(electionId.toString(), total, lines);
  }

  public record ElectionDto(
      String id,
      String titre,
      String description,
      String type,
      String statut,
      String date_debut,
      String date_fin,
      String admin_id,
      List<String> candidat_ids,
      String region,
      int total_electeurs,
      int votes_count
  ) {}

  public record CandidateDto(
      String id,
      String election_id,
      String nom,
      String prenom,
      String parti_politique,
      String biographie,
      String photo_url,
      String programme_url,
      int votes_count,
      String color
  ) {}

  public record VoteResultDto(
      String election_id,
      int total_votes,
      List<VoteResultLineDto> results
  ) {}

  public record VoteResultLineDto(
      String candidat_id,
      int votes,
      double percent
  ) {}

  public record UserDto(
      String id,
      String cni,
      String nom,
      String prenom,
      String email,
      List<String> telephones,
      String date_naissance,
      String adresse,
      List<String> roles
  ) {}

  public record FraudAlertDto(
      String id,
      String type_fraude,
      String citoyen_id,
      String election_id,
      String description,
      String statut,
      String date_detection,
      String operateur_id,
      String date_traitement,
      String ip
  ) {}

  public record SuspensionDto(
      String id,
      String citoyen_id,
      String motif,
      String operateur_id,
      String superadmin_id,
      String statut,
      String date_creation,
      String date_decision,
      String justification
  ) {}
}