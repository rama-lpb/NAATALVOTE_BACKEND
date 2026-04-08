package com.naatalvote.application.election.ports;

import com.naatalvote.domain.election.Election;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ElectionRepositoryPort {
  Election save(Election election);
  Optional<Election> findById(UUID id);
  List<Election> findAll();
  List<Election> findAllPaged(int offset, int limit);
  long count();
}

