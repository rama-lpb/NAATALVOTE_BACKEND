package com.naatalvote.infrastructure.persistence.jpa.repository;

import com.naatalvote.infrastructure.persistence.jpa.entity.CandidateEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateJpaRepository extends JpaRepository<CandidateEntity, UUID> {
  List<CandidateEntity> findByElectionId(UUID electionId);
}

