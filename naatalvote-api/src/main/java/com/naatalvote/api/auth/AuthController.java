package com.naatalvote.api.auth;

import com.naatalvote.application.auth.AuthService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/api/v1/auth/login")
  public AuthService.LoginResponse login(@RequestBody AuthService.LoginRequest req) {
    return auth.login(req);
  }

  @PostMapping("/api/v1/auth/otp/send")
  public Map<String, Object> send(@RequestBody LoginRequest req) {
    var res = auth.sendOtp(new AuthService.LoginRequest(req.cni(), req.telephone()));
    return Map.of("success", res.success(), "message", res.message());
  }

  @PostMapping("/api/v1/auth/otp/verify")
  @ResponseStatus(HttpStatus.OK)
  public AuthService.VerifyOtpResponse verify(@RequestBody AuthService.VerifyOtpRequest req) {
    return auth.verifyOtp(req);
  }

  @PostMapping("/api/v1/auth/logout")
  public Map<String, Object> logout() {
    return Map.of("success", true);
  }

  public record LoginRequest(String cni, String telephone) {}
}
