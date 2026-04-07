package com.naatalvote.application.auth.usecases;

public interface LoginUseCase {
  LoginResponse login(LoginRequest request);

  record LoginRequest(String cni, String telephone) {}

  record LoginResponse(boolean success, String message, boolean requiresOtp) {}
}

