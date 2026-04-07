package com.naatalvote.application.auth;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.OtpSenderPort;
import com.naatalvote.application.auth.ports.OtpStorePort;
import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.domain.audit.ActionType;
import com.naatalvote.domain.auth.User;
import com.naatalvote.domain.common.Ids;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AuthService {
  private static final Duration OTP_TTL = Duration.ofMinutes(5);

  private final UserRepositoryPort users;
  private final OtpStorePort otpStore;
  private final OtpSenderPort otpSender;
  private final TokenIssuerPort tokenIssuer;
  private final ActionLogRepositoryPort actionLogs;
  private final OtpGenerator otpGenerator;

  public AuthService(
      UserRepositoryPort users,
      OtpStorePort otpStore,
      OtpSenderPort otpSender,
      TokenIssuerPort tokenIssuer,
      ActionLogRepositoryPort actionLogs,
      OtpGenerator otpGenerator
  ) {
    this.users = Objects.requireNonNull(users, "users");
    this.otpStore = Objects.requireNonNull(otpStore, "otpStore");
    this.otpSender = Objects.requireNonNull(otpSender, "otpSender");
    this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer");
    this.actionLogs = Objects.requireNonNull(actionLogs, "actionLogs");
    this.otpGenerator = Objects.requireNonNull(otpGenerator, "otpGenerator");
  }

  public LoginResponse login(LoginRequest req) {
    User user = users.findByCni(req.cni()).orElse(null);
    if (user == null) return new LoginResponse(false, "CNI inconnu", false);
    if (!user.telephones().contains(req.telephone())) return new LoginResponse(false, "Téléphone non associé à ce compte", false);

    String otp = otpGenerator.generate();
    otpStore.put(otpKey(req.cni(), req.telephone()), otp, OTP_TTL);
    otpSender.sendOtp(req.telephone(), otp);
    actionLogs.append(demoLog(ActionType.OTP_SEND, user.id(), "OTP envoyé"));
    return new LoginResponse(true, "OTP envoyé", true);
  }

  public SendOtpResponse sendOtp(LoginRequest req) {
    User user = users.findByCni(req.cni()).orElse(null);
    if (user == null || !user.telephones().contains(req.telephone())) {
      return new SendOtpResponse(false, "Utilisateur introuvable");
    }
    String otp = otpGenerator.generate();
    otpStore.put(otpKey(req.cni(), req.telephone()), otp, OTP_TTL);
    otpSender.sendOtp(req.telephone(), otp);
    actionLogs.append(demoLog(ActionType.OTP_SEND, user.id(), "OTP envoyé"));
    return new SendOtpResponse(true, "OTP envoyé");
  }

  public VerifyOtpResponse verifyOtp(VerifyOtpRequest req) {
    User user = users.findByCni(req.cni()).orElse(null);
    if (user == null || !user.telephones().contains(req.telephone())) {
      return new VerifyOtpResponse(false, "", null, "Utilisateur introuvable");
    }

    String expected = otpStore.get(otpKey(req.cni(), req.telephone()));
    if (expected == null || !expected.equals(req.otp())) {
      actionLogs.append(demoLog(ActionType.OTP_VERIFY, user.id(), "OTP invalide"));
      return new VerifyOtpResponse(false, "", null, "Code OTP invalide");
    }

    otpStore.remove(otpKey(req.cni(), req.telephone()));
    String token = tokenIssuer.issueToken(user.id(), user.roles());
    actionLogs.append(demoLog(ActionType.LOGIN, user.id(), "Connexion réussie"));
    return new VerifyOtpResponse(true, token, UserDto.from(user), "OK");
  }

  public LogoutResponse logout(UUID userId) {
    if (userId != null) {
      actionLogs.append(demoLog(ActionType.LOGOUT, userId, "Déconnexion"));
    }
    return new LogoutResponse(true);
  }

  private static String otpKey(String cni, String telephone) {
    return cni.trim() + ":" + telephone.trim();
  }

  private static ActionLog demoLog(ActionType type, UUID userId, String description) {
    return new ActionLog(Ids.newUuid(), type, userId, description, Instant.now(), null, "HMAC-DEMO");
  }

  public record LoginRequest(String cni, String telephone) {}
  public record LoginResponse(boolean success, String message, boolean requiresOtp) {}
  public record SendOtpResponse(boolean success, String message) {}
  public record VerifyOtpRequest(String cni, String telephone, String otp) {}
  public record VerifyOtpResponse(boolean success, String token, UserDto user, String message) {}
  public record LogoutResponse(boolean success) {}

  public record UserDto(String id, String cni, String nom, String prenom, String email, List<String> roles) {
    static UserDto from(User user) {
      return new UserDto(
          user.id().toString(),
          user.cni(),
          user.nom(),
          user.prenom(),
          user.email(),
          user.roles().stream().map(Enum::name).toList()
      );
    }
  }
}

