package com.naatalvote.api.candidates;

import com.naatalvote.api.common.DtoAssembler;
import com.naatalvote.application.election.ElectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CandidatesController {
  private final ElectionService elections;

  public CandidatesController(ElectionService elections) {
    this.elections = elections;
  }

  @GetMapping("/api/v1/candidats")
  public List<DtoAssembler.CandidateDto> list() {
    return elections.listAllCandidates().stream()
        .map(DtoAssembler::toCandidateDto)
        .toList();
  }

  @GetMapping("/api/v1/candidats/paged")
  public PagedCandidatesResponse listPaged(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    var result = elections.listCandidatesPaged(page, size);
    List<DtoAssembler.CandidateDto> content = result.content().stream()
        .map(DtoAssembler::toCandidateDto)
        .toList();
    return new PagedCandidatesResponse(content, result.page(), result.pageSize(), result.total(), result.totalPages());
  }

  @GetMapping("/api/v1/candidats/{id}")
  public DtoAssembler.CandidateDto get(@PathVariable("id") UUID id) {
    return DtoAssembler.toCandidateDto(elections.getCandidate(id));
  }

  @PostMapping("/api/v1/candidats")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@Valid @RequestBody CreateCandidateRequest req) {
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
      @NotBlank(message = "election_id requis") String election_id,
      @NotBlank(message = "Le nom est requis") String nom,
      @NotBlank(message = "Le prénom est requis") String prenom,
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

  public record PagedCandidatesResponse(
      List<DtoAssembler.CandidateDto> content,
      int page,
      int pageSize,
      long total,
      int totalPages
  ) {}
}