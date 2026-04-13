package com.naatalvote.api.citizen;

import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.domain.auth.User;
import com.naatalvote.domain.vote.TraceVote;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CitizenController {
  private final UserRepositoryPort users;
  private final TraceVoteRepositoryPort traces;

  public CitizenController(UserRepositoryPort users, TraceVoteRepositoryPort traces) {
    this.users = users;
    this.traces = traces;
  }

  @GetMapping("/api/v1/citoyen/{id}/profile")
  public UserDto profile(@PathVariable("id") UUID id) {
    User u = users.findById(id).orElseThrow();
    return UserDto.from(u);
  }

  @GetMapping("/api/v1/citoyen/{id}/vote-status")
  public Object voteStatus(@PathVariable("id") UUID id, @RequestParam(name = "electionId", required = false) UUID electionId) {
    if (electionId != null) {
      TraceVote tv = traces.findByCitoyenIdAndElectionId(id, electionId).orElse(null);
      if (tv == null) return Map.of("citoyen_id", id.toString(), "election_id", electionId.toString(), "a_vote", false);
      return Map.of("citoyen_id", id.toString(), "election_id", electionId.toString(), "a_vote", tv.aVote(), "horodatage", tv.horodatage());
    }
    return traces.findByCitoyenId(id).stream().map(TraceVoteDto::from).toList();
  }

  @GetMapping("/api/v1/citoyen/{id}/history")
  public List<TraceVoteDto> history(@PathVariable("id") UUID id) {
    return traces.findByCitoyenId(id).stream().map(TraceVoteDto::from).toList();
  }

  public record TraceVoteDto(String id, String election_id, boolean a_vote, String horodatage) {
    static TraceVoteDto from(TraceVote tv) {
      return new TraceVoteDto(tv.id().toString(), tv.electionId().toString(), tv.aVote(), tv.horodatage() == null ? null : tv.horodatage().toString());
    }
  }

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
  ) {
    static UserDto from(User user) {
      return new UserDto(
          user.id().toString(),
          user.cni(),
          user.nom(),
          user.prenom(),
          user.email(),
          user.telephones(),
          user.dateNaissance(),
          user.adresse(),
          user.roles().stream().map(Enum::name).toList()
      );
    }
  }
}
