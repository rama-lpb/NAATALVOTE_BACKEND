package com.naatalvote.api.votes;

import com.naatalvote.application.vote.VoteService;
import com.naatalvote.domain.vote.Vote;
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

  // mapping confirmation_number -> token UUID (démo)
  private final ConcurrentHashMap<String, UUID> tokenByConfirmation = new ConcurrentHashMap<>();

  public VotesController(VoteService voteService) {
    this.voteService = voteService;
  }

  @PostMapping("/api/v1/votes")
  @ResponseStatus(HttpStatus.CREATED)
  public CastVoteResponse cast(@RequestBody CastVoteRequest req, @AuthenticationPrincipal Jwt jwt) {
    UUID electionId = UUID.fromString(req.election_id());
    UUID candidatId = UUID.fromString(req.candidat_id());
    UUID citoyenId = req.citoyen_id() != null && !req.citoyen_id().isBlank()
        ? UUID.fromString(req.citoyen_id())
        : (jwt == null ? null : UUID.fromString(jwt.getSubject()));
    if (citoyenId == null) {
      throw new IllegalArgumentException("citoyen_id requis (ou JWT requis)");
    }
    UUID token = req.token_anonyme() == null || req.token_anonyme().isBlank() ? null : UUID.fromString(req.token_anonyme());

    VoteService.CastVoteResult res = voteService.cast(new VoteService.CastVoteCommand(electionId, candidatId, citoyenId, token));
    String confirmation = ConfirmationUtil.confirmationFor(res.tokenAnonyme());
    tokenByConfirmation.put(confirmation, res.tokenAnonyme());
    return new CastVoteResponse(true, confirmation, res.horodatage().toString());
  }

  @GetMapping("/api/v1/votes/verify/{token}")
  public Map<String, Object> verify(@PathVariable("token") String token) {
    UUID tokenUuid = parseToken(token);
    Vote v = voteService.verifyToken(tokenUuid);
    return Map.of(
        "success", true,
        "election_id", v.electionId().toString(),
        "candidat_id", v.candidatId().toString(),
        "horodatage", v.horodatage().toString()
    );
  }

  @GetMapping("/api/v1/votes/election/{id}/results")
  public Map<String, Object> results(@PathVariable("id") UUID electionId) {
    VoteService.Results res = voteService.results(electionId);
    List<Map<String, Object>> lines = res.results().stream().map(l -> Map.<String, Object>of(
        "candidat_id", l.candidatId().toString(),
        "votes", l.votes(),
        "percent", l.percent()
    )).toList();
    return Map.of("election_id", res.electionId().toString(), "total_votes", res.totalVotes(), "results", lines);
  }

  private UUID parseToken(String token) {
    if (token.startsWith("V-")) {
      UUID t = tokenByConfirmation.get(token);
      if (t == null) throw new IllegalArgumentException("Token inconnu");
      return t;
    }
    return UUID.fromString(token);
  }

  public record CastVoteRequest(String election_id, String candidat_id, String token_anonyme, String citoyen_id) {}

  public record CastVoteResponse(boolean success, String confirmation_number, String horodatage) {}

  static final class ConfirmationUtil {
    static String confirmationFor(UUID token) {
      // stable-ish demo confirmation derived from UUID
      String suffix = token.toString().replace("-", "").substring(0, 4).toUpperCase();
      int year = Instant.now().atZone(java.time.ZoneOffset.UTC).getYear();
      return "V-" + year + "-" + suffix;
    }
  }
}
