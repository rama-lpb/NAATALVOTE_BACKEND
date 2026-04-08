package com.naatalvote.api.admin;

import com.naatalvote.application.admin.AdminService;
import com.naatalvote.domain.election.Election;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
  private final AdminService admin;

  public AdminController(AdminService admin) {
    this.admin = admin;
  }

  @GetMapping("/api/v1/admin/elections")
  public List<Map<String, Object>> list(@RequestParam(name = "adminId", required = false) UUID adminId) {
    return admin.listAdminElections(adminId).stream()
        .map(e -> Map.<String, Object>of(
            "id", e.id().toString(),
            "titre", e.titre(),
            "statut", e.statut().name(),
            "date_debut", e.dateDebut().toString(),
            "date_fin", e.dateFin().toString()
        ))
        .toList();
  }

  @GetMapping("/api/v1/admin/elections/{id}/stats")
  public Map<String, Object> stats(@PathVariable("id") UUID electionId) {
    AdminService.ElectionStats stats = admin.getElectionStats(electionId);
    return Map.of(
        "election_id", stats.electionId().toString(),
        "titre", stats.titre(),
        "total_votes", stats.totalVotes(),
        "participation_rate", stats.participationRate(),
        "candidate_votes", stats.candidateVotes().stream().map(c -> Map.of("candidat_id", c.candidatId().toString(), "votes", c.votes())).toList(),
        "statut", stats.statut()
    );
  }

  @PostMapping("/api/v1/admin/elections/{id}/close")
  public Map<String, Object> close(@PathVariable("id") UUID electionId) {
    admin.closeElection(electionId);
    return Map.of("success", true);
  }
}
