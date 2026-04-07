package com.naatalvote.application.fraud.ports;

import com.naatalvote.domain.fraud.FraudAlert;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudAlertRepositoryPort {
  FraudAlert save(FraudAlert alert);
  Optional<FraudAlert> findById(UUID id);
  List<FraudAlert> findAll();
}

