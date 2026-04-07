package com.naatalvote.infrastructure.scheduler;

import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.infrastructure.persistence.jpa.repository.ElectionJpaRepository;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Profile("postgres")
public class ElectionLifecycleScheduler {
  private final ElectionJpaRepository elections;

  public ElectionLifecycleScheduler(ElectionJpaRepository elections) {
    this.elections = elections;
  }

  @Scheduled(fixedDelayString = "PT30S")
  public void tick() {
    Instant now = Instant.now();
    elections.findAll().forEach(e -> {
      ElectionStatus next;
      if (now.isBefore(e.getDateDebut())) {
        next = ElectionStatus.PROGRAMMEE;
      } else if (now.isBefore(e.getDateFin())) {
        next = ElectionStatus.EN_COURS;
      } else {
        next = ElectionStatus.CLOTUREE;
      }
      if (e.getStatut() != next) {
        e.setStatut(next);
        elections.save(e);
      }
    });
  }
}

