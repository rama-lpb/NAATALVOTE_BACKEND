package com.naatalvote.infrastructure.bootstrap;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.domain.audit.ActionType;
import com.naatalvote.domain.auth.User;
import com.naatalvote.domain.auth.UserRole;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.election.Candidate;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.domain.election.ElectionType;
import com.naatalvote.domain.fraud.FraudAlert;
import com.naatalvote.domain.fraud.FraudAlertStatus;
import com.naatalvote.domain.fraud.FraudType;
import com.naatalvote.domain.fraud.Suspension;
import com.naatalvote.domain.fraud.SuspensionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements ApplicationRunner {
  private final UserRepositoryPort users;
  private final ElectionRepositoryPort elections;
  private final CandidateRepositoryPort candidates;
  private final FraudAlertRepositoryPort alerts;
  private final SuspensionRepositoryPort suspensions;
  private final ActionLogRepositoryPort logs;

  public DemoDataInitializer(
      UserRepositoryPort users,
      ElectionRepositoryPort elections,
      CandidateRepositoryPort candidates,
      FraudAlertRepositoryPort alerts,
      SuspensionRepositoryPort suspensions,
      ActionLogRepositoryPort logs
  ) {
    this.users = users;
    this.elections = elections;
    this.candidates = candidates;
    this.alerts = alerts;
    this.suspensions = suspensions;
    this.logs = logs;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!users.findAll().isEmpty()) {
      return;
    }
    User citizen = new User(
        Ids.newUuid(),
        "CNI-00001",
        "Diop",
        "Amadou",
        "amadou.diop@example.sn",
        List.of("+221770000001"),
        List.of(UserRole.CITOYEN)
    );
    User admin = new User(
        Ids.newUuid(),
        "CNI-ADMIN-1",
        "Ndiaye",
        "Awa",
        "awa.ndiaye@naatal.sn",
        List.of("+221770000010"),
        List.of(UserRole.ADMIN)
    );
    User operateur = new User(
        Ids.newUuid(),
        "CNI-OPER-1",
        "Fall",
        "Moussa",
        "moussa.fall@naatal.sn",
        List.of("+221770000011"),
        List.of(UserRole.OPERATEUR)
    );
    User superadmin = new User(
        Ids.newUuid(),
        "CNI-SUPER-1",
        "Sarr",
        "Fatou",
        "fatou.sarr@naatal.sn",
        List.of("+221770000012"),
        List.of(UserRole.SUPERADMIN)
    );

    users.save(citizen);
    users.save(admin);
    users.save(operateur);
    users.save(superadmin);

    Instant now = Instant.now();
    Election e1 = new Election(
        Ids.newUuid(),
        "Présidentielle 2026",
        "Élection présidentielle (démo)",
        ElectionType.PRESIDENTIELLE,
        now.plusSeconds(3600 * 24 * 10),
        now.plusSeconds(3600 * 24 * 12),
        ElectionStatus.PROGRAMMEE,
        admin.id()
    );
    elections.save(e1);

    candidates.save(new Candidate(Ids.newUuid(), e1.id(), "Diop", "Awa", "Alliance République", "Biographie courte", "/photos/awa.png", "/programmes/awa.pdf"));
    candidates.save(new Candidate(Ids.newUuid(), e1.id(), "Sow", "Ibrahima", "Mouvement Citoyen", "Biographie courte", "/photos/ibra.png", "/programmes/ibra.pdf"));

    FraudAlert a1 = new FraudAlert(
        Ids.newUuid(),
        FraudType.CONNEXION_ANORMALE,
        citizen.id(),
        e1.id(),
        "Connexions/déconnexions rapides détectées",
        FraudAlertStatus.NOUVELLE,
        now,
        null,
        null,
        "41.208.10.5"
    );
    alerts.save(a1);

    Suspension s1 = new Suspension(
        Ids.newUuid(),
        citizen.id(),
        "Tentative de vote multiple",
        operateur.id(),
        null,
        SuspensionStatus.EN_ATTENTE,
        now,
        null,
        ""
    );
    suspensions.save(s1);

    appendLog(ActionType.USER_CREATED, superadmin.id(), "Initialisation des comptes (démo)");
    appendLog(ActionType.CREATE_ELECTION, admin.id(), "Programmation de l’élection " + e1.titre());
  }

  private void appendLog(ActionType type, UUID userId, String description) {
    logs.append(new ActionLog(
        Ids.newUuid(),
        type,
        userId,
        description,
        Instant.now(),
        null,
        "HMAC-DEMO"
    ));
  }
}
