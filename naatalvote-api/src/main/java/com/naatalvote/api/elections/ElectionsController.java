package com.naatalvote.api.elections;

import com.naatalvote.application.election.ElectionService;
import com.naatalvote.domain.election.Candidate;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ElectionsController {
  private final ElectionService elections;

  public ElectionsController(ElectionService elections) {
    this.elections = elections;
  }

  @GetMapping("/api/v1/elections")
  public List<ElectionDto> list() {
    return elections.listElections().stream().map(ElectionDto::from).toList();
  }

  @GetMapping("/api/v1/elections/{id}")
  public ElectionDto get(@PathVariable("id") UUID id) {
    return ElectionDto.from(elections.getElection(id));
  }

  @GetMapping("/api/v1/elections/{id}/candidats")
  public List<CandidateDto> listCandidates(@PathVariable("id") UUID id) {
    return elections.listCandidates(id).stream().map(CandidateDto::from).toList();
  }

  @PostMapping("/api/v1/elections")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@RequestBody CreateElectionRequest req) {
    UUID id = elections.createElection(new ElectionService.CreateElectionCommand(
        UUID.fromString(req.admin_id()),
        req.titre(),
        req.description(),
        ElectionType.valueOf(req.type()),
        Instant.parse(req.date_debut()),
        Instant.parse(req.date_fin())
    ));
    return Map.of("id", id.toString());
  }

  @PutMapping("/api/v1/elections/{id}")
  public Map<String, Object> update(@PathVariable("id") UUID id, @RequestBody UpdateElectionRequest req) {
    elections.updateElection(id, new ElectionService.UpdateElectionCommand(
        req.titre(),
        req.description(),
        req.type() == null ? null : ElectionType.valueOf(req.type()),
        req.date_debut() == null ? null : Instant.parse(req.date_debut()),
        req.date_fin() == null ? null : Instant.parse(req.date_fin())
    ));
    return Map.of("success", true);
  }

  @PostMapping("/api/v1/elections/{id}/publish")
  public Map<String, Object> publish(@PathVariable("id") UUID id) {
    elections.publishElection(id);
    return Map.of("success", true);
  }

  public record CreateElectionRequest(
      String titre,
      String description,
      String type,
      String date_debut,
      String date_fin,
      String admin_id
  ) {}

  public record UpdateElectionRequest(
      String titre,
      String description,
      String type,
      String date_debut,
      String date_fin
  ) {}

  public record ElectionDto(
      String id,
      String titre,
      String type,
      String statut,
      String date_debut,
      String date_fin,
      String admin_id
  ) {
    static ElectionDto from(Election e) {
      return new ElectionDto(
          e.id().toString(),
          e.titre(),
          e.type().name(),
          e.statut().name(),
          e.dateDebut().toString(),
          e.dateFin().toString(),
          e.adminId().toString()
      );
    }
  }

  public record CandidateDto(
      String id,
      String election_id,
      String nom,
      String prenom,
      String parti_politique,
      String biographie,
      String photo_url,
      String programme_url
  ) {
    static CandidateDto from(Candidate c) {
      return new CandidateDto(
          c.id().toString(),
          c.electionId().toString(),
          c.nom(),
          c.prenom(),
          c.partiPolitique(),
          c.biographie(),
          c.photoUrl(),
          c.programmeUrl()
      );
    }
  }
}
