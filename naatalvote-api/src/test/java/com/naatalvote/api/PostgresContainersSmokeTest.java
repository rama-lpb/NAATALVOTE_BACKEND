package com.naatalvote.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "naatalvote.jwt.allow-ephemeral=true")
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
class PostgresContainersSmokeTest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

  @Autowired private MockMvc mvc;
  @Autowired private com.fasterxml.jackson.databind.ObjectMapper json;
  @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;
  @Autowired private com.naatalvote.application.auth.ports.UserRepositoryPort users;
  @Autowired private com.naatalvote.application.auth.ports.TokenIssuerPort tokens;
  @Autowired private com.naatalvote.application.auth.ports.OtpStorePort otps;
  @Autowired private com.naatalvote.application.election.ports.ElectionRepositoryPort elections;
  @Autowired private com.naatalvote.application.election.ports.CandidateRepositoryPort candidates;
  @Autowired private com.naatalvote.application.fraud.ports.SuspensionRepositoryPort suspensions;

  @org.springframework.test.context.DynamicPropertySource
  static void props(org.springframework.test.context.DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  @Test
  void healthIsOk() throws Exception {
    mvc.perform(get("/api/v1/health"))
        .andExpect(status().isOk());
  }

  @Test
  void migrationsAreAppliedAndRedisExpiresOtp() throws Exception {
    org.junit.jupiter.api.Assertions.assertTrue(jdbc.queryForObject(
        "select count(*) from flyway_schema_history where success", Integer.class) >= 6);
    String key = java.util.UUID.randomUUID().toString();
    otps.put(key, "123456", java.time.Duration.ofMillis(100));
    org.junit.jupiter.api.Assertions.assertEquals("123456", otps.get(key));
    Thread.sleep(200);
    org.junit.jupiter.api.Assertions.assertNull(otps.get(key));
  }

  @Test
  void citizenCannotUseAdministratorRoutesOrAnotherIdentity() throws Exception {
    var f = fixture();
    mvc.perform(get("/api/v1/superadmin/users").header("Authorization", "Bearer " + f.token()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isUnauthorized());
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/votes")
        .header("Authorization", "Bearer " + f.token()).contentType("application/json")
        .content(payload(f, java.util.UUID.randomUUID(), null))).andExpect(status().isForbidden());
  }

  @Test
  void approvedSuspensionRejectsAnAlreadyIssuedToken() throws Exception {
    var f = fixture();
    mvc.perform(get("/api/v1/elections").header("Authorization", "Bearer " + f.token())).andExpect(status().isOk());
    suspensions.save(new com.naatalvote.domain.fraud.Suspension(java.util.UUID.randomUUID(), f.user(),
        "Test", f.user(), f.user(), com.naatalvote.domain.fraud.SuspensionStatus.APPROUVEE,
        java.time.Instant.now(), java.time.Instant.now(), "Test de révocation"));
    mvc.perform(get("/api/v1/elections").header("Authorization", "Bearer " + f.token()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void receiptDoesNotRevealCandidateAndDuplicateTokenRollsBackParticipation() throws Exception {
    var f = fixture();
    String result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/votes")
        .header("Authorization", "Bearer " + f.token()).contentType("application/json")
        .content(payload(f, f.user(), null))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String token = json.readTree(result).get("token_anonyme").asText();
    mvc.perform(get("/api/v1/votes/verify/" + token).header("Authorization", "Bearer " + f.token()))
        .andExpect(status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.candidat_id").doesNotExist());
    var other = newUser();
    String otherToken = tokens.issueToken(other, java.util.List.of(com.naatalvote.domain.auth.UserRole.CITOYEN));
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/votes")
        .header("Authorization", "Bearer " + otherToken).contentType("application/json")
        .content(payload(f, other, token))).andExpect(status().isConflict());
    org.junit.jupiter.api.Assertions.assertEquals(0, jdbc.queryForObject(
        "select count(*) from trace_vote where citoyen_id=? and election_id=?", Integer.class, other, f.election()));
  }

  @Test
  void twentyConcurrentRequestsCreateExactlyOneBallot() throws Exception {
    var f = fixture();
    var start = new java.util.concurrent.CountDownLatch(1);
    var pool = java.util.concurrent.Executors.newFixedThreadPool(20);
    try {
      var jobs = new java.util.ArrayList<java.util.concurrent.Future<Integer>>();
      for (int i = 0; i < 20; i++) jobs.add(pool.submit(() -> {
        start.await();
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/votes")
            .header("Authorization", "Bearer " + f.token()).contentType("application/json")
            .content(payload(f, f.user(), null))).andReturn().getResponse().getStatus();
      }));
      start.countDown();
      var statuses = new java.util.ArrayList<Integer>();
      for (var job : jobs) statuses.add(job.get(30, java.util.concurrent.TimeUnit.SECONDS));
      org.junit.jupiter.api.Assertions.assertEquals(1, statuses.stream().filter(s -> s == 201).count());
      org.junit.jupiter.api.Assertions.assertEquals(19, statuses.stream().filter(s -> s == 409).count());
      org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject("select count(*) from vote where election_id=?", Integer.class, f.election()));
      org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject("select count(*) from trace_vote where election_id=?", Integer.class, f.election()));
    } finally { pool.shutdownNow(); }
  }

  private java.util.UUID newUser() {
    var id = java.util.UUID.randomUUID();
    users.save(new com.naatalvote.domain.auth.User(id, id.toString(), "Test", "Fictif", "test@example.invalid",
        java.util.List.of("+221770000099"), java.util.List.of(com.naatalvote.domain.auth.UserRole.CITOYEN)));
    return id;
  }

  private Fixture fixture() {
    var user = newUser(); var election = java.util.UUID.randomUUID(); var candidate = java.util.UUID.randomUUID();
    var now = java.time.Instant.now();
    elections.save(new com.naatalvote.domain.election.Election(election, "Scrutin de test", "Fictif",
        com.naatalvote.domain.election.ElectionType.MUNICIPALE, now.minusSeconds(60), now.plusSeconds(3600),
        com.naatalvote.domain.election.ElectionStatus.EN_COURS, user));
    candidates.save(new com.naatalvote.domain.election.Candidate(candidate, election, "Test", "Candidat", "Test", "", "", ""));
    return new Fixture(user, election, candidate, tokens.issueToken(user, java.util.List.of(com.naatalvote.domain.auth.UserRole.CITOYEN)));
  }

  private String payload(Fixture f, java.util.UUID user, String token) throws Exception {
    var data = new java.util.HashMap<String, String>();
    data.put("election_id", f.election().toString()); data.put("candidat_id", f.candidate().toString());
    data.put("citoyen_id", user.toString()); if (token != null) data.put("token_anonyme", token);
    return json.writeValueAsString(data);
  }
  private record Fixture(java.util.UUID user, java.util.UUID election, java.util.UUID candidate, String token) {}
}
