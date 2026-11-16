package com.naatalvote.api.votes;

import com.naatalvote.api.security.CurrentUser;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

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
  private final TransactionTemplate tx;

  public VotesController(VoteService voteService, ObjectProvider<PlatformTransactionManager> txManager) {
    this.voteService = voteService;
    PlatformTransactionManager tm = txManager.getIfAvailable();
    // Profil postgres : la trace et le bulletin sont écrits dans UNE transaction (tout ou rien).
    // Profil inmemory : pas de gestionnaire de transactions, exécution directe.
    this.tx = tm == null ? null : new TransactionTemplate(tm);
  }

  @PostMapping("/api/v1/votes")
  @ResponseStatus(HttpStatus.CREATED)
  public CastVoteResponse cast(@Valid @RequestBody CastVoteRequest req, @AuthenticationPrincipal Jwt jwt) {
    try {
      UUID electionId = UUID.fromString(req.election_id());
      UUID candidatId = UUID.fromString(req.candidat_id());
      // L'électeur est celui du jeton signé ; un citoyen_id différent dans le corps est refusé (usurpation).
      UUID citoyenId = CurrentUser.idOr(jwt, req.citoyen_id());
      if (citoyenId == null) {
        throw new DomainException("citoyen_id requis (ou JWT requis)");
      }
      if (jwt != null && req.citoyen_id() != null && !req.citoyen_id().isBlank()
          && !UUID.fromString(req.citoyen_id()).equals(citoyenId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez voter que pour vous-même");
      }
      UUID token = req.token_anonyme() == null || req.token_anonyme().isBlank() ? null : UUID.fromString(req.token_anonyme());

      VoteService.CastVoteCommand cmd = new VoteService.CastVoteCommand(electionId, candidatId, citoyenId, token);
      VoteService.CastVoteResult res;
      try {
        res = tx == null ? voteService.cast(cmd) : tx.execute(st -> voteService.cast(cmd));
      } catch (DataIntegrityViolationException e) {
        // Requête concurrente du même électeur : la contrainte d'unicité de trace_vote a joué.
        throw new DomainException("Vote multiple interdit");
      }
      String confirmation = ConfirmationUtil.confirmationFor(res.tokenAnonyme());
      tokenByConfirmation.put(confirmation, res.tokenAnonyme());
      return new CastVoteResponse(true, confirmation, res.horodatage().toString(), res.tokenAnonyme().toString());
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

  /** token_anonyme : reçu permettant la vérification même après un redémarrage du serveur. */
  public record CastVoteResponse(boolean success, String confirmation_number, String horodatage, String token_anonyme) {}

  static final class ConfirmationUtil {
    static String confirmationFor(UUID token) {
      // 12 caractères hexadécimaux (au lieu de 4) : collisions entre reçus improbables
      String suffix = token.toString().replace("-", "").substring(0, 12).toUpperCase();
      int year = Instant.now().atZone(java.time.ZoneOffset.UTC).getYear();
      return "V-" + year + "-" + suffix;
    }
  }
}