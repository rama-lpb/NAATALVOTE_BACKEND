package com.naatalvote.infrastructure.persistence.jpa.repository;

import com.naatalvote.infrastructure.persistence.jpa.entity.ElectionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectionJpaRepository extends JpaRepository<ElectionEntity, UUID> {}

