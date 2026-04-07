package com.naatalvote.application.auth.ports;

import com.naatalvote.domain.auth.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
  Optional<User> findByCni(String cni);
  Optional<User> findById(UUID id);
  List<User> findAll();
  User save(User user);
}

