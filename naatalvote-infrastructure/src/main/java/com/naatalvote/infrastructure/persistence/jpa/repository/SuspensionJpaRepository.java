package com.naatalvote.infrastructure.persistence.jpa.repository;

import com.naatalvote.infrastructure.persistence.jpa.entity.SuspensionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspensionJpaRepository extends JpaRepository<SuspensionEntity, UUID> {}

