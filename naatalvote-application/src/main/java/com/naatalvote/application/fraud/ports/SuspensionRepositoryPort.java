package com.naatalvote.application.fraud.ports;

import com.naatalvote.domain.fraud.Suspension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuspensionRepositoryPort {
  Suspension save(Suspension suspension);
  Optional<Suspension> findById(UUID id);
  List<Suspension> findAll();
}

