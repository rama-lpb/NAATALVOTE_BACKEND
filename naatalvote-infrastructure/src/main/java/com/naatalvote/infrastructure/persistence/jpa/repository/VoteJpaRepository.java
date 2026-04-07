package com.naatalvote.infrastructure.persistence.jpa.repository;

import com.naatalvote.infrastructure.persistence.jpa.entity.VoteEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteJpaRepository extends JpaRepository<VoteEntity, UUID> {
  Optional<VoteEntity> findByTokenAnonyme(UUID token);
  List<VoteEntity> findByElectionId(UUID electionId);
}

