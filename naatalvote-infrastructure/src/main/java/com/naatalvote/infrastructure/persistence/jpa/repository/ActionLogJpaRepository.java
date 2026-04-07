package com.naatalvote.infrastructure.persistence.jpa.repository;

import com.naatalvote.infrastructure.persistence.jpa.entity.ActionLogEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogJpaRepository extends JpaRepository<ActionLogEntity, UUID> {
  List<ActionLogEntity> findByUtilisateurIdOrderByHorodatageDesc(UUID userId);
  List<ActionLogEntity> findAllByOrderByHorodatageDesc();
}

