package com.naatalvote.application.auth;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.OtpSenderPort;
import com.naatalvote.application.auth.ports.OtpStorePort;
import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.application.auth.ports.CitizenRegistryPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.domain.audit.ActionType;
import com.naatalvote.domain.auth.User;
import com.naatalvote.domain.auth.UserRole;
import com.naatalvote.domain.common.Ids;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AuthService {
  private static final Duration OTP_TTL = Duration.ofMinutes(5);

  private final UserRepositoryPort users;
  private final CitizenRegistryPort citizenRegistry;
  private final OtpStorePort otpStore;
  private final OtpSenderPort otpSender;
  private final TokenIssuerPort tokenIssuer;
  private final ActionLogRepositoryPort actionLogs;
  private final OtpGenerator otpGenerator;
  private final boolean debugReturnOtp;

  public AuthService(
      UserRepositoryPort users,
      CitizenRegistryPort citizenRegistry,
      OtpStorePort otpStore,
      OtpSenderPort otpSender,
      TokenIssuerPort tokenIssuer,
      ActionLogRepositoryPort actionLogs,
      OtpGenerator otpGenerator,
      boolean debugReturnOtp
  ) {
    this.users = Objects.requireNonNull(users, "users");
    this.citizenRegistry = Objects.requireNonNull(citizenRegistry, "citizenRegistry");
    this.otpStore = Objects.requireNonNull(otpStore, "otpStore");
    this.otpSender = Objects.requireNonNull(otpSender, "otpSender");
    this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer");
    this.actionLogs = Objects.requireNonNull(actionLogs, "actionLogs");
    this.otpGenerator = Objects.requireNonNull(otpGenerator, "otpGenerator");
    this.debugReturnOtp = debugReturnOtp;
  }

  public LoginResponse login(LoginRequest req) {
    User user = ensureUserLoaded(req.cni());
    if (user == null) return new LoginResponse(false, "CNI inconnu", false, "");
    if (!user.telephones().contains(req.telephone())) return new LoginResponse(false, "Téléphone non associé à ce compte", false, "");

    String otp = otpGenerator.generate();
    otpStore.put(otpKey(req.cni(), req.telephone()), otp, OTP_TTL);
    otpSender.sendOtp(req.telephone(), otp);
    actionLogs.append(demoLog(ActionType.OTP_SEND, user.id(), "OTP envoyé"));
    return new LoginResponse(true, "OTP envoyé", true, debugReturnOtp ? otp : "");
  }

  public SendOtpResponse sendOtp(LoginRequest req) {
    User user = ensureUserLoaded(req.cni());
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
    User user = ensureUserLoaded(req.cni());
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

  public LookupPhonesResponse lookupPhones(String cni) {
    User user = ensureUserLoaded(cni);
    if (user == null) return new LookupPhonesResponse(false, List.of(), "CNI inconnu");
    return new LookupPhonesResponse(true, user.telephones(), "OK");
  }

  private static String otpKey(String cni, String telephone) {
    return cni.trim() + ":" + telephone.trim();
  }

  private static ActionLog demoLog(ActionType type, UUID userId, String description) {
    return new ActionLog(Ids.newUuid(), type, userId, description, Instant.now(), null, "HMAC-DEMO");
  }

  private User ensureUserLoaded(String cni) {
    User existing = users.findByCni(cni).orElse(null);
    if (existing != null) return existing;

    try {
      CitizenRegistryPort.CitizenRecord rec = citizenRegistry.findByCni(cni).orElse(null);
      if (rec == null) return null;

      List<UserRole> roles = (rec.roles() == null ? List.<String>of() : rec.roles()).stream()
          .filter(Objects::nonNull)
          .map(String::trim)
          .filter(s -> !s.isBlank())
          .map(r -> {
            try {
              return UserRole.valueOf(r);
            } catch (IllegalArgumentException e) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .toList();
      if (roles.isEmpty()) {
        roles = List.of(UserRole.CITOYEN);
      } else if (!roles.contains(UserRole.CITOYEN)) {
        roles = java.util.stream.Stream.concat(
            java.util.stream.Stream.of(UserRole.CITOYEN),
            roles.stream()
        ).distinct().toList();
      }

      User created = new User(
          Ids.newUuid(),
          rec.cni(),
          rec.nom(),
          rec.prenom(),
          rec.email() == null ? "" : rec.email(),
          rec.telephones() == null ? List.of() : rec.telephones(),
          roles,
          rec.date_naissance() == null ? "" : rec.date_naissance(),
          rec.adresse() == null ? "" : rec.adresse()
      );
      return users.save(created);
    } catch (Exception ignored) {
      // If external registry is unavailable, keep previous behavior (user not found).
      return null;
    }
  }

  public record LoginRequest(String cni, String telephone) {}
  public record LoginResponse(boolean success, String message, boolean requiresOtp, String otp) {}
  public record SendOtpResponse(boolean success, String message) {}
  public record VerifyOtpRequest(String cni, String telephone, String otp) {}
  public record VerifyOtpResponse(boolean success, String token, UserDto user, String message) {}
  public record LogoutResponse(boolean success) {}
  public record LookupPhonesResponse(boolean success, List<String> telephones, String message) {}

  public record UserDto(
      String id,
      String cni,
      String nom,
      String prenom,
      String email,
      List<String> telephones,
      String date_naissance,
      String adresse,
      List<String> roles
  ) {
    static UserDto from(User user) {
      return new UserDto(
          user.id().toString(),
          user.cni(),
          user.nom(),
          user.prenom(),
          user.email(),
          user.telephones(),
          user.dateNaissance(),
          user.adresse(),
          user.roles().stream().map(Enum::name).toList()
      );
    }
  }
}
