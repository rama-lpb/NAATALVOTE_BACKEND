package com.naatalvote.infrastructure.security;

import com.naatalvote.application.auth.ports.OtpSenderPort;

public final class ConsoleOtpSender implements OtpSenderPort {
  @Override
  public void sendOtp(String telephone, String otp) {
    System.out.println("[NAATALVOTE] OTP envoyé (simulation) -> " + telephone + " : " + otp);
  }
}

