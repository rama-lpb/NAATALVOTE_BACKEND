package com.naatalvote.infrastructure.persistence.memory;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.domain.audit.ActionLog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryActionLogRepository implements ActionLogRepositoryPort {
  private final CopyOnWriteArrayList<ActionLog> store = new CopyOnWriteArrayList<>();

  @Override
  public ActionLog append(ActionLog log) {
    store.add(log);
    return log;
  }

  @Override
  public List<ActionLog> findAll() {
    return store.stream()
        .sorted(Comparator.comparing(ActionLog::horodatage).reversed())
        .toList();
  }

  @Override
  public List<ActionLog> findByUserId(UUID userId) {
    List<ActionLog> out = new ArrayList<>();
    for (ActionLog l : store) {
      if (l.utilisateurId().equals(userId)) out.add(l);
    }
    out.sort(Comparator.comparing(ActionLog::horodatage).reversed());
    return out;
  }
}

