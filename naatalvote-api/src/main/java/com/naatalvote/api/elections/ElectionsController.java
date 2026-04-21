package com.naatalvote.api.elections;

import com.naatalvote.api.common.DtoAssembler;
import com.naatalvote.application.election.ElectionService;
import com.naatalvote.domain.common.DomainException;
import com.naatalvote.domain.election.ElectionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ElectionsController {
  private final ElectionService elections;

  public ElectionsController(ElectionService elections) {
    this.elections = elections;
  }

  @GetMapping("/api/v1/elections")
  public List<DtoAssembler.ElectionDto> list() {
    return elections.listElections().stream()
        .map(DtoAssembler::toElectionDto)
        .toList();
  }

  @GetMapping("/api/v1/elections/paged")
  public PagedElectionsResponse listPaged(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    var result = elections.listElectionsPaged(page, size);
    List<DtoAssembler.ElectionDto> content = result.content().stream()
        .map(DtoAssembler::toElectionDto)
        .toList();
    return new PagedElectionsResponse(content, result.page(), result.pageSize(), result.total(), result.totalPages());
  }

  @GetMapping("/api/v1/elections/{id}")
  public DtoAssembler.ElectionDto get(@PathVariable("id") UUID id) {
    return DtoAssembler.toElectionDto(elections.getElection(id));
  }

  @GetMapping("/api/v1/elections/{id}/candidats")
  public List<DtoAssembler.CandidateDto> listCandidates(@PathVariable("id") UUID id) {
    return elections.listCandidates(id).stream()
        .map(DtoAssembler::toCandidateDto)
        .toList();
  }

  @PostMapping("/api/v1/elections")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@Valid @RequestBody CreateElectionRequest req) {
    try {
      ElectionType type = ElectionType.valueOf(req.type());
      UUID id = elections.createElection(new ElectionService.CreateElectionCommand(
          UUID.fromString(req.admin_id()),
          req.titre(),
          req.description(),
          type,
          Instant.parse(req.date_debut()),
          Instant.parse(req.date_fin()),
          req.region(),
          req.total_electeurs()
      ));
      return Map.of("id", id.toString());
    } catch (IllegalArgumentException e) {
      throw new DomainException("Type d'élection invalide ou format de date invalide");
    }
  }

  @PutMapping("/api/v1/elections/{id}")
  public Map<String, Object> update(@PathVariable("id") UUID id, @Valid @RequestBody UpdateElectionRequest req) {
    try {
      ElectionType type = req.type() == null ? null : ElectionType.valueOf(req.type());
      elections.updateElection(id, new ElectionService.UpdateElectionCommand(
          req.titre(),
          req.description(),
          type,
          req.date_debut() == null ? null : Instant.parse(req.date_debut()),
          req.date_fin() == null ? null : Instant.parse(req.date_fin()),
          req.region(),
          req.total_electeurs()
      ));
      return Map.of("success", true);
    } catch (IllegalArgumentException e) {
      throw new DomainException("Type d'élection invalide ou format de date invalide");
    }
  }

  @PostMapping("/api/v1/elections/{id}/publish")
  public Map<String, Object> publish(@PathVariable("id") UUID id) {
    elections.publishElection(id);
    return Map.of("success", true);
  }

  public record CreateElectionRequest(
      @NotBlank(message = "Le titre est requis") String titre,
      String description,
      @NotNull(message = "Le type est requis") String type,
      @NotBlank(message = "La date de début est requise") String date_debut,
      @NotBlank(message = "La date de fin est requise") String date_fin,
      @NotBlank(message = "L'ID admin est requis") String admin_id,
      String region,
      int total_electeurs
  ) {}

  public record UpdateElectionRequest(
      String titre,
      String description,
      String type,
      String date_debut,
      String date_fin,
      String region,
      Integer total_electeurs
  ) {}

  public record PagedElectionsResponse(
      List<DtoAssembler.ElectionDto> content,
      int page,
      int pageSize,
      long total,
      int totalPages
  ) {}
}
