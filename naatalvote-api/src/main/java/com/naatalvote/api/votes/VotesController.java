package com.naatalvote.api.votes;

import com.naatalvote.api.common.DtoAssembler;
import com.naatalvote.application.vote.VoteService;
import com.naatalvote.domain.common.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VotesController {
  private final VoteService voteService;
  private final ConcurrentHashMap<String, UUID> tokenByConfirmation = new ConcurrentHashMap<>();

  public VotesController(VoteService voteService) {
    this.voteService = voteService;
  }

  @PostMapping("/api/v1/votes")
  @ResponseStatus(HttpStatus.CREATED)
  public CastVoteResponse cast(@Valid @RequestBody CastVoteRequest req, @AuthenticationPrincipal Jwt jwt) {
    try {
      UUID electionId = UUID.fromString(req.election_id());
      UUID candidatId = UUID.fromString(req.candidat_id());
      UUID citoyenId = req.citoyen_id() != null && !req.citoyen_id().isBlank()
          ? UUID.fromString(req.citoyen_id())
          : (jwt == null ? null : UUID.fromString(jwt.getSubject()));
      if (citoyenId == null) {
        throw new DomainException("citoyen_id requis (ou JWT requis)");
      }
      UUID token = req.token_anonyme() == null || req.token_anonyme().isBlank() ? null : UUID.fromString(req.token_anonyme());

      VoteService.CastVoteResult res = voteService.cast(new VoteService.CastVoteCommand(electionId, candidatId, citoyenId, token));
      String confirmation = ConfirmationUtil.confirmationFor(res.tokenAnonyme());
      tokenByConfirmation.put(confirmation, res.tokenAnonyme());
      return new CastVoteResponse(true, confirmation, res.horodatage().toString());
    } catch (IllegalArgumentException e) {
      throw new DomainException("Format UUID invalide");
    }
  }

  @GetMapping("/api/v1/votes/verify/{token}")
  public Map<String, Object> verify(@PathVariable("token") String token) {
    UUID tokenUuid = parseToken(token);
    VoteService.VerifyResult res = voteService.verify(tokenUuid);
    return Map.of(
        "success", true,
        "election_id", res.electionId().toString(),
        "candidat_id", res.candidatId().toString(),
        "horodatage", res.horodatage().toString()
    );
  }

  @GetMapping("/api/v1/votes/election/{id}/results")
  public DtoAssembler.VoteResultDto results(@PathVariable("id") UUID electionId) {
    VoteService.Results res = voteService.results(electionId);
    List<DtoAssembler.VoteResultLineDto> lines = res.results().stream()
        .map(l -> new DtoAssembler.VoteResultLineDto(l.candidatId().toString(), l.votes(), l.percent()))
        .toList();
    return new DtoAssembler.VoteResultDto(res.electionId().toString(), res.totalVotes(), lines);
  }

  private UUID parseToken(String token) {
    if (token.startsWith("V-")) {
      UUID t = tokenByConfirmation.get(token);
      if (t == null) throw new DomainException("Token inconnu");
      return t;
    }
    return UUID.fromString(token);
  }

  public record CastVoteRequest(
      @NotBlank(message = "election_id requis") String election_id,
      @NotBlank(message = "candidat_id requis") String candidat_id,
      String token_anonyme,
      String citoyen_id
  ) {}

  public record CastVoteResponse(boolean success, String confirmation_number, String horodatage) {}

  static final class ConfirmationUtil {
    static String confirmationFor(UUID token) {
      String suffix = token.toString().replace("-", "").substring(0, 4).toUpperCase();
      int year = Instant.now().atZone(java.time.ZoneOffset.UTC).getYear();
      return "V-" + year + "-" + suffix;
    }
  }
}