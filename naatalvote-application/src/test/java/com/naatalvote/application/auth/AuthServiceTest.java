package com.naatalvote.application.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.*;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.domain.auth.User;
import com.naatalvote.domain.auth.UserRole;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests des règles d'authentification (ajoutés lors de l'audit E221). */
class AuthServiceTest {
  static final String CNI = "20119990214000017";
  static final String TEL = "+221770000001";
  final Map<String, String> store = new HashMap<>();
  final List<ActionLog> logs = new ArrayList<>();
  final List<String> sent = new ArrayList<>();
  AuthService auth;
  User user;

  @BeforeEach
  void setUp() {
    user = new User(UUID.randomUUID(), CNI, "Ndiaye", "Awa", "a@x.sn", List.of(TEL), List.of(UserRole.CITOYEN));
    UserRepositoryPort users = new UserRepositoryPort() {
      public Optional<User> findByCni(String c) { return c.equals(CNI) ? Optional.of(user) : Optional.empty(); }
      public Optional<User> findById(UUID id) { return Optional.of(user); }
      public List<User> findAll() { return List.of(user); }
      public User save(User u) { return u; }
    };
    OtpStorePort otpStore = new OtpStorePort() {
      public void put(String k, String v, Duration ttl) { store.put(k, v); }
      public String get(String k) { return store.get(k); }
      public void remove(String k) { store.remove(k); }
    };
    ActionLogRepositoryPort actionLogs = new ActionLogRepositoryPort() {
      public ActionLog append(ActionLog l) { logs.add(l); return l; }
      public List<ActionLog> findAll() { return logs; }
      public List<ActionLog> findByUserId(UUID id) { return logs; }
    };
    auth = new AuthService(users, cni -> Optional.empty(), otpStore, (t, otp) -> sent.add(t + ":" + otp),
        (id, roles) -> "jwt-" + id, actionLogs, () -> "123456", false);
  }

  @Test
  void otpIsSingleUse() {
    assertTrue(auth.login(new AuthService.LoginRequest(CNI, TEL)).success());
    assertTrue(auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "123456")).success());
    assertFalse(auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "123456")).success(), "un OTP déjà utilisé doit être refusé");
  }

  @Test
  void otpIsNotReturnedWhenDebugDisabled() {
    assertEquals("", auth.login(new AuthService.LoginRequest(CNI, TEL)).otp());
  }

  @Test
  void otpIsInvalidatedAfterFiveFailedAttempts() {
    auth.login(new AuthService.LoginRequest(CNI, TEL));
    for (int i = 1; i <= 4; i++) {
      assertEquals("Code OTP invalide", auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "000000")).message());
    }
    assertTrue(auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "000000")).message().startsWith("Trop de tentatives"));
    // même le bon code est désormais refusé : il faut en redemander un
    assertFalse(auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "123456")).success());
  }

  @Test
  void newLoginResetsAttemptCounter() {
    auth.login(new AuthService.LoginRequest(CNI, TEL));
    for (int i = 0; i < 4; i++) auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "000000"));
    auth.login(new AuthService.LoginRequest(CNI, TEL));
    assertTrue(auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "123456")).success());
  }

  @Test
  void lookupReturnsMaskedPhonesAndLoginAcceptsMaskedForm() {
    var res = auth.lookupPhones(CNI);
    assertEquals(List.of("+22177*****01"), res.telephones());
    assertTrue(auth.login(new AuthService.LoginRequest(CNI, "+22177*****01")).success());
    assertEquals(List.of(TEL + ":123456"), sent, "l'OTP est envoyé au numéro réel");
  }

  @Test
  void rejectsUnknownPhoneAndBlankInputs() {
    assertFalse(auth.login(new AuthService.LoginRequest(CNI, "+221779999999")).success());
    assertFalse(auth.login(new AuthService.LoginRequest(null, TEL)).success());
    assertFalse(auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, null, "123456")).success());
  }

  @Test
  void approvedSuspensionBlocksLogin() {
    var sus = new com.naatalvote.domain.fraud.Suspension(UUID.randomUUID(), user.id(), "fraude", UUID.randomUUID(), UUID.randomUUID(),
        com.naatalvote.domain.fraud.SuspensionStatus.APPROUVEE, java.time.Instant.now(), java.time.Instant.now(), "ok");
    com.naatalvote.application.fraud.ports.SuspensionRepositoryPort repo = new com.naatalvote.application.fraud.ports.SuspensionRepositoryPort() {
      public com.naatalvote.domain.fraud.Suspension save(com.naatalvote.domain.fraud.Suspension s) { return s; }
      public Optional<com.naatalvote.domain.fraud.Suspension> findById(UUID id) { return Optional.of(sus); }
      public List<com.naatalvote.domain.fraud.Suspension> findAll() { return List.of(sus); }
    };
    UserRepositoryPort users = new UserRepositoryPort() {
      public Optional<User> findByCni(String c) { return Optional.of(user); }
      public Optional<User> findById(UUID id) { return Optional.of(user); }
      public List<User> findAll() { return List.of(user); }
      public User save(User u) { return u; }
    };
    AuthService guarded = new AuthService(users, c -> Optional.empty(), new OtpStorePort() {
      public void put(String k, String v, Duration t) { store.put(k, v); }
      public String get(String k) { return store.get(k); }
      public void remove(String k) { store.remove(k); }
    }, (t, o) -> {}, (id, r) -> "jwt", new ActionLogRepositoryPort() {
      public ActionLog append(ActionLog l) { return l; }
      public List<ActionLog> findAll() { return List.of(); }
      public List<ActionLog> findByUserId(UUID id) { return List.of(); }
    }, () -> "123456", false, repo);
    var res = guarded.login(new AuthService.LoginRequest(CNI, TEL));
    assertFalse(res.success());
    assertEquals("Compte suspendu", res.message());
  }

  @Test
  void journalEntriesCarryNoFakeSignature() {
    auth.login(new AuthService.LoginRequest(CNI, TEL));
    auth.verifyOtp(new AuthService.VerifyOtpRequest(CNI, TEL, "123456"));
    assertFalse(logs.isEmpty());
    logs.forEach(l -> assertNull(l.signature(), "aucune signature factice ne doit être écrite"));
  }
}
