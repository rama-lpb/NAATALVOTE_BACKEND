package com.naatalvote.api.candidates;

import com.naatalvote.application.election.ElectionService;
import com.naatalvote.domain.election.Candidate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CandidatesController {
  private final ElectionService elections;

  public CandidatesController(ElectionService elections) {
    this.elections = elections;
  }

  @GetMapping("/api/v1/candidats")
  public List<CandidateDto> list() {
    return elections.listAllCandidates().stream().map(CandidateDto::from).toList();
  }

  @GetMapping("/api/v1/candidats/{id}")
  public CandidateDto get(@PathVariable("id") UUID id) {
    return CandidateDto.from(elections.getCandidate(id));
  }

  @PostMapping("/api/v1/candidats")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@RequestBody CreateCandidateRequest req) {
    UUID id = elections.createCandidate(new ElectionService.CreateCandidateCommand(
        UUID.fromString(req.election_id()),
        req.nom(),
        req.prenom(),
        req.parti_politique(),
        req.biographie(),
        req.photo_url(),
        req.programme_url()
    ));
    return Map.of("id", id.toString());
  }

  @PutMapping("/api/v1/candidats/{id}")
  public Map<String, Object> update(@PathVariable("id") UUID id, @RequestBody UpdateCandidateRequest req) {
    elections.updateCandidate(id, new ElectionService.UpdateCandidateCommand(
        req.nom(),
        req.prenom(),
        req.parti_politique(),
        req.biographie(),
        req.photo_url(),
        req.programme_url()
    ));
    return Map.of("success", true);
  }

  @DeleteMapping("/api/v1/candidats/{id}")
  public Map<String, Object> delete(@PathVariable("id") UUID id) {
    elections.deleteCandidate(id);
    return Map.of("success", true);
  }

  public record CreateCandidateRequest(
      String election_id,
      String nom,
      String prenom,
      String parti_politique,
      String biographie,
      String photo_url,
      String programme_url
  ) {}

  public record UpdateCandidateRequest(
      String election_id,
      String nom,
      String prenom,
      String parti_politique,
      String biographie,
      String photo_url,
      String programme_url
  ) {}

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
