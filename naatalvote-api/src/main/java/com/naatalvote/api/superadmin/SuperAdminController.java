package com.naatalvote.api.superadmin;

import com.naatalvote.application.superadmin.SuperAdminService;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.domain.auth.UserRole;
import com.naatalvote.domain.fraud.Suspension;
import com.naatalvote.domain.fraud.SuspensionStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SuperAdminController {
  private final SuperAdminService superAdmin;

  public SuperAdminController(SuperAdminService superAdmin) {
    this.superAdmin = superAdmin;
  }

  @GetMapping("/api/v1/superadmin/users")
  public List<Map<String, Object>> listUsers() {
    return superAdmin.listUsers().stream().map(u -> Map.<String, Object>of(
        "id", u.id().toString(),
        "cni", u.cni(),
        "nom", u.nom(),
        "prenom", u.prenom(),
        "email", u.email(),
        "telephones", u.telephones(),
        "date_naissance", u.dateNaissance(),
        "adresse", u.adresse(),
        "roles", u.roles().stream().map(Enum::name).toList()
    )).toList();
  }

  @PutMapping("/api/v1/superadmin/users/{id}/roles")
  public Map<String, Object> updateRoles(@PathVariable("id") UUID id, @RequestBody UpdateRolesRequest req) {
    List<UserRole> roles = req.roles().stream().map(UserRole::valueOf).toList();
    superAdmin.updateRoles(id, roles);
    return Map.of("success", true);
  }

  @GetMapping("/api/v1/superadmin/logs")
  public List<Map<String, Object>> listLogs() {
    return superAdmin.listLogs().stream().map(SuperAdminController::toDto).toList();
  }

  @GetMapping("/api/v1/superadmin/suspensions")
  public List<Map<String, Object>> listSuspensions() {
    return superAdmin.listSuspensions().stream().map(SuperAdminController::toDto).toList();
  }

  @PutMapping("/api/v1/superadmin/suspensions/{id}/decision")
  public Map<String, Object> decide(@PathVariable("id") UUID id, @RequestBody DecideSuspensionRequest req) {
    SuspensionStatus status = SuspensionStatus.valueOf(req.statut());
    UUID superadminId = req.superadmin_id() == null ? null : UUID.fromString(req.superadmin_id());
    superAdmin.decideSuspension(id, new com.naatalvote.application.superadmin.SuperAdminService.SuspensionDecision(
        superadminId,
        status,
        req.justification() == null ? "" : req.justification()
    ));
    return Map.of("success", true);
  }

  private static Map<String, Object> toDto(ActionLog l) {
    return Map.of(
        "id", l.id().toString(),
        "type_action", l.type().name(),
        "utilisateur_id", l.utilisateurId().toString(),
        "description", l.description(),
        "horodatage", l.horodatage().toString(),
        "adresse_ip", l.adresseIp(),
        "signature_cryptographique", l.signature()
    );
  }

  private static Map<String, Object> toDto(Suspension s) {
    return Map.of(
        "id", s.id().toString(),
        "citoyen_id", s.citoyenId().toString(),
        "motif", s.motif(),
        "operateur_id", s.operateurId().toString(),
        "superadmin_id", s.superadminId() == null ? null : s.superadminId().toString(),
        "statut", s.statut().name(),
        "date_creation", s.dateCreation().toString(),
        "date_decision", s.dateDecision() == null ? null : s.dateDecision().toString(),
        "justification", s.justification()
    );
  }

  public record UpdateRolesRequest(List<String> roles) {}

  public record DecideSuspensionRequest(String statut, String superadmin_id, String justification) {}
}
