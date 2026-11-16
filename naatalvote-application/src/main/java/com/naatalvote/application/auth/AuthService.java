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
  static final int MAX_OTP_ATTEMPTS = 5;

  private final UserRepositoryPort users;
  private final CitizenRegistryPort citizenRegistry;
  private final OtpStorePort otpStore;
  private final OtpSenderPort otpSender;
  private final TokenIssuerPort tokenIssuer;
  private final ActionLogRepositoryPort actionLogs;
  private final OtpGenerator otpGenerator;
  private final boolean debugReturnOtp;
  private final com.naatalvote.application.fraud.ports.SuspensionRepositoryPort suspensions;

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
    this.suspensions = null;
  }

  /** Variante avec contrôle des suspensions : un citoyen dont la suspension est APPROUVEE ne peut plus se connecter. */
  public AuthService(
      UserRepositoryPort users,
      CitizenRegistryPort citizenRegistry,
      OtpStorePort otpStore,
      OtpSenderPort otpSender,
      TokenIssuerPort tokenIssuer,
      ActionLogRepositoryPort actionLogs,
      OtpGenerator otpGenerator,
      boolean debugReturnOtp,
      com.naatalvote.application.fraud.ports.SuspensionRepositoryPort suspensions
  ) {
    this.users = Objects.requireNonNull(users, "users");
    this.citizenRegistry = Objects.requireNonNull(citizenRegistry, "citizenRegistry");
    this.otpStore = Objects.requireNonNull(otpStore, "otpStore");
    this.otpSender = Objects.requireNonNull(otpSender, "otpSender");
    this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer");
    this.actionLogs = Objects.requireNonNull(actionLogs, "actionLogs");
    this.otpGenerator = Objects.requireNonNull(otpGenerator, "otpGenerator");
    this.debugReturnOtp = debugReturnOtp;
    this.suspensions = suspensions;
  }

  private boolean isSuspended(User user) {
    if (suspensions == null) return false;
    return suspensions.findAll().stream().anyMatch(s -> s.citoyenId().equals(user.id())
        && s.statut() == com.naatalvote.domain.fraud.SuspensionStatus.APPROUVEE);
  }

  public LoginResponse login(LoginRequest req) {
    if (isBlank(req.cni()) || isBlank(req.telephone())) return new LoginResponse(false, "CNI et téléphone requis", false, "");
    User user = ensureUserLoaded(req.cni());
    if (user == null) return new LoginResponse(false, "CNI inconnu", false, "");
    if (isSuspended(user)) return new LoginResponse(false, "Compte suspendu", false, "");
    String phone = resolvePhone(user, req.telephone());
    if (phone == null) return new LoginResponse(false, "Téléphone non associé à ce compte", false, "");

    String otp = otpGenerator.generate();
    otpStore.put(otpKey(req.cni(), phone), otp, OTP_TTL);
    otpStore.remove(attemptsKey(req.cni(), phone));
    otpSender.sendOtp(phone, otp);
    actionLogs.append(demoLog(ActionType.OTP_SEND, user.id(), "OTP envoyé"));
    return new LoginResponse(true, "OTP envoyé", true, debugReturnOtp ? otp : "");
  }

  public SendOtpResponse sendOtp(LoginRequest req) {
    if (isBlank(req.cni()) || isBlank(req.telephone())) return new SendOtpResponse(false, "CNI et téléphone requis");
    User user = ensureUserLoaded(req.cni());
    String phone = user == null ? null : resolvePhone(user, req.telephone());
    if (phone == null) {
      return new SendOtpResponse(false, "Utilisateur introuvable");
    }
    String otp = otpGenerator.generate();
    otpStore.put(otpKey(req.cni(), phone), otp, OTP_TTL);
    otpStore.remove(attemptsKey(req.cni(), phone));
    otpSender.sendOtp(phone, otp);
    actionLogs.append(demoLog(ActionType.OTP_SEND, user.id(), "OTP envoyé"));
    return new SendOtpResponse(true, "OTP envoyé");
  }

  public VerifyOtpResponse verifyOtp(VerifyOtpRequest req) {
    if (isBlank(req.cni()) || isBlank(req.telephone())) return new VerifyOtpResponse(false, "", null, "CNI et téléphone requis");
    User user = ensureUserLoaded(req.cni());
    String phone = user == null ? null : resolvePhone(user, req.telephone());
    if (phone == null) {
      return new VerifyOtpResponse(false, "", null, "Utilisateur introuvable");
    }

    String key = otpKey(req.cni(), phone);
    String expected = otpStore.get(key);
    if (expected == null || !expected.equals(req.otp())) {
      int attempts = incrementAttempts(req.cni(), phone);
      actionLogs.append(demoLog(ActionType.OTP_VERIFY, user.id(), "OTP invalide (tentative " + attempts + ")"));
      if (attempts >= MAX_OTP_ATTEMPTS) {
        // Au-delà de 5 erreurs, le code en cours est invalidé : il faut en demander un nouveau.
        otpStore.remove(key);
        return new VerifyOtpResponse(false, "", null, "Trop de tentatives : demandez un nouveau code");
      }
      return new VerifyOtpResponse(false, "", null, "Code OTP invalide");
    }

    otpStore.remove(key);
    otpStore.remove(attemptsKey(req.cni(), phone));
    if (isSuspended(user)) {
      return new VerifyOtpResponse(false, "", null, "Compte suspendu");
    }
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
    if (isBlank(cni)) return new LookupPhonesResponse(false, List.of(), "CNI requis");
    User user = ensureUserLoaded(cni);
    if (user == null) return new LookupPhonesResponse(false, List.of(), "CNI inconnu");
    // Endpoint public : les numéros sont renvoyés masqués pour ne pas exposer de données personnelles.
    return new LookupPhonesResponse(true, user.telephones().stream().map(AuthService::maskPhone).toList(), "OK");
  }

  /** Masque un numéro : conserve l'indicatif et les 2 derniers chiffres (ex. +22177******01). */
  public static String maskPhone(String phone) {
    if (phone == null) return "";
    String p = phone.trim();
    if (p.length() <= 8) return p;
    return p.substring(0, 6) + "*".repeat(p.length() - 8) + p.substring(p.length() - 2);
  }

  /** Accepte le numéro complet ou sa forme masquée (renvoyée par lookup) ; retourne le numéro réel. */
  private static String resolvePhone(User user, String provided) {
    String p = provided.trim();
    for (String real : user.telephones()) {
      if (real.equals(p) || maskPhone(real).equals(p)) return real;
    }
    return null;
  }

  private int incrementAttempts(String cni, String phone) {
    String k = attemptsKey(cni, phone);
    String cur = otpStore.get(k);
    int n = (cur == null ? 0 : Integer.parseInt(cur)) + 1;
    otpStore.put(k, Integer.toString(n), OTP_TTL);
    return n;
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static String otpKey(String cni, String telephone) {
    return cni.trim() + ":" + telephone.trim();
  }

  private static String attemptsKey(String cni, String telephone) {
    return "attempts:" + otpKey(cni, telephone);
  }

  private static ActionLog demoLog(ActionType type, UUID userId, String description) {
    return new ActionLog(Ids.newUuid(), type, userId, description, Instant.now(), null, null);
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
