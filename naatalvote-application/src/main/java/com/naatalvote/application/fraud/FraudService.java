package com.naatalvote.application.fraud;

import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.fraud.FraudAlert;
import com.naatalvote.domain.fraud.FraudAlertStatus;
import com.naatalvote.domain.fraud.Suspension;
import com.naatalvote.domain.fraud.SuspensionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class FraudService {
  private final FraudAlertRepositoryPort alerts;
  private final SuspensionRepositoryPort suspensions;

  public FraudService(FraudAlertRepositoryPort alerts, SuspensionRepositoryPort suspensions) {
    this.alerts = Objects.requireNonNull(alerts, "alerts");
    this.suspensions = Objects.requireNonNull(suspensions, "suspensions");
  }

  public List<FraudAlert> listAlerts() {
    return alerts.findAll();
  }

  public FraudAlert treatAlert(UUID alertId, TreatAlertCommand cmd) {
    FraudAlert a = alerts.findById(alertId).orElseThrow();
    FraudAlert next = new FraudAlert(
        a.id(),
        a.type(),
        a.citoyenId(),
        a.electionId(),
        cmd.description() == null ? a.description() : cmd.description(),
        cmd.statut(),
        a.dateDetection(),
        cmd.operateurId(),
        Instant.now(),
        a.ip()
    );
    return alerts.save(next);
  }

  public List<Suspension> listSuspensions() {
    return suspensions.findAll();
  }

  public Suspension recommendSuspension(RecommendSuspensionCommand cmd) {
    Suspension s = new Suspension(
        Ids.newUuid(),
        cmd.citoyenId(),
        cmd.motif(),
        cmd.operateurId(),
        null,
        SuspensionStatus.EN_ATTENTE,
        Instant.now(),
        null,
        ""
    );
    return suspensions.save(s);
  }

  public record TreatAlertCommand(FraudAlertStatus statut, UUID operateurId, String description) {}
  public record RecommendSuspensionCommand(UUID citoyenId, String motif, UUID operateurId) {}
}

