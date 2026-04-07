package com.naatalvote.application.audit.ports;

import com.naatalvote.domain.audit.ActionLog;
import java.util.List;
import java.util.UUID;

public interface ActionLogRepositoryPort {
  ActionLog append(ActionLog log);
  List<ActionLog> findAll();
  List<ActionLog> findByUserId(UUID userId);
}

