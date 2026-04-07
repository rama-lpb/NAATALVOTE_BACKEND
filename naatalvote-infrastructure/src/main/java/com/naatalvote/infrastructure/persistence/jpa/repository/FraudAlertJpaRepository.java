package com.naatalvote.infrastructure.persistence.jpa.repository;

import com.naatalvote.infrastructure.persistence.jpa.entity.FraudAlertEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudAlertJpaRepository extends JpaRepository<FraudAlertEntity, UUID> {}

