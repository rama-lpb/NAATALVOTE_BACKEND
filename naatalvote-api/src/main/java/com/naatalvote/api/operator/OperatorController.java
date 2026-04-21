package com.naatalvote.api.operator;

import com.naatalvote.application.fraud.FraudService;
import com.naatalvote.domain.fraud.FraudAlert;
import com.naatalvote.domain.fraud.FraudAlertStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @PostMapping({"/api/v1/operateur/suspensions", "/api/v1/security/suspensions"})
  public Map<String, Object> recommendSuspension(@RequestBody RecommendSuspensionRequest req) {
    var s = fraud.recommendSuspension(new FraudService.RecommendSuspensionCommand(
        UUID.fromString(req.citoyen_id()),
        req.motif(),
        UUID.fromString(req.operateur_id())
    ));
    return Map.of(
        "success", true,
        "id", s.id().toString(),
        "statut", s.statut().name()
    );
  }

  private static Map<String, Object> toDto(FraudAlert a) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", a.id().toString());
    dto.put("type_fraude", a.type().name());
    dto.put("citoyen_id", a.citoyenId() == null ? null : a.citoyenId().toString());
    dto.put("election_id", a.electionId() == null ? null : a.electionId().toString());
    dto.put("description", a.description());
    dto.put("statut", a.statut().name());
    dto.put("date_detection", a.dateDetection().toString());
    dto.put("operateur_id", a.operateurId() == null ? null : a.operateurId().toString());
    dto.put("date_traitement", a.dateTraitement() == null ? null : a.dateTraitement().toString());
    dto.put("ip", a.ip());
    return dto;
  }

  public record TreatAlertRequest(String statut, String operateur_id, String description) {}
  public record RecommendSuspensionRequest(String citoyen_id, String motif, String operateur_id) {}
}
