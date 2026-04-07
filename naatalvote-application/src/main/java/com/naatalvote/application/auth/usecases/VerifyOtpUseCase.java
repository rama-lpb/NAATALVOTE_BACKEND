package com.naatalvote.application.auth.usecases;

import com.naatalvote.domain.auth.UserRole;
import java.util.List;
import java.util.UUID;

public interface VerifyOtpUseCase {
  VerifyOtpResponse verify(VerifyOtpRequest request);

  record VerifyOtpRequest(String cni, String telephone, String otp) {}

  record VerifyOtpResponse(boolean success, String token, UserDto user, String message) {}

  record UserDto(UUID id, String cni, String nom, String prenom, String email, List<UserRole> roles) {}
}

