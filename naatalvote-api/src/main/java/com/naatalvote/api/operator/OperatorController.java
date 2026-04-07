package com.naatalvote.api.operator;

import com.naatalvote.application.fraud.FraudService;
import com.naatalvote.domain.fraud.FraudAlert;
import com.naatalvote.domain.fraud.FraudAlertStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OperatorController {
  private final FraudService fraud;

  public OperatorController(FraudService fraud) {
    this.fraud = fraud;
  }

  @GetMapping({"/api/v1/operateur/alertes", "/api/v1/security/alertes"})
  public List<Map<String, Object>> listAlerts() {
    return fraud.listAlerts().stream().map(OperatorController::toDto).toList();
  }

  @PutMapping({"/api/v1/operateur/alertes/{id}", "/api/v1/security/alertes/{id}"})
  public Map<String, Object> treat(@PathVariable("id") UUID id, @RequestBody TreatAlertRequest req) {
    fraud.treatAlert(id, new com.naatalvote.application.fraud.FraudService.TreatAlertCommand(
        FraudAlertStatus.valueOf(req.statut()),
        req.operateur_id() == null ? null : UUID.fromString(req.operateur_id()),
        req.description()
    ));
    return Map.of("success", true);
  }

  @GetMapping({"/api/v1/operateur/suspensions", "/api/v1/security/suspensions"})
  public List<Map<String, Object>> listSuspensions() {
    return fraud.listSuspensions().stream().map(s -> Map.<String, Object>of(
        "id", s.id().toString(),
        "citoyen_id", s.citoyenId().toString(),
        "motif", s.motif(),
        "operateur_id", s.operateurId().toString(),
        "statut", s.statut().name(),
        "date_creation", s.dateCreation().toString()
    )).toList();
  }

  private static Map<String, Object> toDto(FraudAlert a) {
    return Map.of(
        "id", a.id().toString(),
        "type_fraude", a.type().name(),
        "citoyen_id", a.citoyenId() == null ? null : a.citoyenId().toString(),
        "election_id", a.electionId() == null ? null : a.electionId().toString(),
        "description", a.description(),
        "statut", a.statut().name(),
        "date_detection", a.dateDetection().toString(),
        "operateur_id", a.operateurId() == null ? null : a.operateurId().toString(),
        "date_traitement", a.dateTraitement() == null ? null : a.dateTraitement().toString(),
        "ip", a.ip()
    );
  }

  public record TreatAlertRequest(String statut, String operateur_id, String description) {}
}
