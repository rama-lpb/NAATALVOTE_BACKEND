package com.naatalvote.infrastructure.persistence.jpa.repository;

import com.naatalvote.infrastructure.persistence.jpa.entity.TraceVoteEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraceVoteJpaRepository extends JpaRepository<TraceVoteEntity, UUID> {
  Optional<TraceVoteEntity> findByCitoyenIdAndElectionId(UUID citoyenId, UUID electionId);
  List<TraceVoteEntity> findByCitoyenId(UUID citoyenId);
}

