package com.naatalvote.application.auth.ports;

public interface OtpSenderPort {
  void sendOtp(String telephone, String otp);
}

