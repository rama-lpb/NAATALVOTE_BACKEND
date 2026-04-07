package com.naatalvote.application.superadmin;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.domain.auth.User;
import com.naatalvote.domain.auth.UserRole;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.fraud.Suspension;
import com.naatalvote.domain.fraud.SuspensionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SuperAdminService {
  private final UserRepositoryPort users;
  private final ActionLogRepositoryPort logs;
  private final SuspensionRepositoryPort suspensions;

  public SuperAdminService(UserRepositoryPort users, ActionLogRepositoryPort logs, SuspensionRepositoryPort suspensions) {
    this.users = Objects.requireNonNull(users, "users");
    this.logs = Objects.requireNonNull(logs, "logs");
    this.suspensions = Objects.requireNonNull(suspensions, "suspensions");
  }

  public List<User> listUsers() {
    return users.findAll();
  }

  public void updateRoles(UUID userId, List<UserRole> roles) {
    User u = users.findById(userId).orElseThrow();
    users.save(new User(u.id(), u.cni(), u.nom(), u.prenom(), u.email(), u.telephones(), roles));
    logs.append(new ActionLog(Ids.newUuid(), com.naatalvote.domain.audit.ActionType.USER_ROLES_UPDATED, userId, "Rôles mis à jour", Instant.now(), null, "HMAC-DEMO"));
  }

  public List<ActionLog> listLogs() {
    return logs.findAll();
  }

  public List<Suspension> listSuspensions() {
    return suspensions.findAll();
  }

  public Suspension decideSuspension(UUID suspensionId, SuspensionDecision cmd) {
    Suspension s = suspensions.findById(suspensionId).orElseThrow();
    Suspension next = new Suspension(
        s.id(),
        s.citoyenId(),
        s.motif(),
        s.operateurId(),
        cmd.superadminId(),
        cmd.statut(),
        s.dateCreation(),
        Instant.now(),
        cmd.justification()
    );
    suspensions.save(next);
    logs.append(new ActionLog(Ids.newUuid(), com.naatalvote.domain.audit.ActionType.SUSPENSION_DECIDED, cmd.superadminId(), "Décision suspension: " + cmd.statut().name(), Instant.now(), null, "HMAC-DEMO"));
    return next;
  }

  public record SuspensionDecision(UUID superadminId, SuspensionStatus statut, String justification) {}
}

