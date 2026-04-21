package com.naatalvote.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("inmemory")
class ModulesWorkflowTest {
  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper om;

  @Test
  void shouldCoverMainModulesAndPreventDoubleVote() throws Exception {
    mvc.perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));

    LoginSession admin = loginWithOtp("10219951003000028", "+221770000002");
    LoginSession operator = loginWithOtp("20320010721000039", "+221770000003");
    LoginSession superadmin = loginWithOtp("10419881201000040", "+221770000004");
    LoginSession citizen = loginWithOtp("20520031130000073", "+221770000007");

    // Admin module: create election and candidate with photo + PDF program
    Instant start = Instant.now().plus(5, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    Instant end = start.plus(1, ChronoUnit.DAYS);
    String createElectionPayload = """
        {
          "titre":"Municipales Test Workflow",
          "description":"Election de test integration modules",
          "type":"MUNICIPALE",
          "date_debut":"%s",
          "date_fin":"%s",
          "admin_id":"%s",
          "region":"Dakar",
          "total_electeurs":32000
        }
        """.formatted(start, end, admin.userId());

    MvcResult electionCreateRes = mvc.perform(post("/api/v1/elections")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createElectionPayload))
        .andExpect(status().isCreated())
        .andReturn();
    String electionId = readBody(electionCreateRes).get("id").asText();

    mvc.perform(put("/api/v1/elections/{id}", electionId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"region":"Dakar Plateau","total_electeurs":35000}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    MvcResult candidateCreateRes = mvc.perform(post("/api/v1/candidats")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "election_id":"%s",
                  "nom":"Ndiaye",
                  "prenom":"Aissatou",
                  "parti_politique":"Coalition Horizon",
                  "biographie":"Candidate de test. Priorite jeunesse. Priorite emploi local.",
                  "photo_url":"https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                  "programme_url":"https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
                }
                """.formatted(electionId)))
        .andExpect(status().isCreated())
        .andReturn();
    String candidateId = readBody(candidateCreateRes).get("id").asText();

    mvc.perform(get("/api/v1/candidats/{id}", candidateId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.photo_url").value("https://images.unsplash.com/photo-1494790108377-be9c29b29330"))
        .andExpect(jsonPath("$.programme_url").value("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));

    mvc.perform(get("/api/v1/admin/elections/{id}/stats", electionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.election_id").value(electionId));

    mvc.perform(post("/api/v1/admin/elections/{id}/close", electionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // Operator + superadmin modules
    MvcResult alertsRes = mvc.perform(get("/api/v1/operateur/alertes"))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode alerts = readBody(alertsRes);
    assertTrue(alerts.isArray());
    assertFalse(alerts.isEmpty());
    String alertId = alerts.get(0).get("id").asText();

    mvc.perform(put("/api/v1/operateur/alertes/{id}", alertId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"statut":"EN_ANALYSE","operateur_id":"%s","description":"Analyse manuelle en cours"}
                """.formatted(operator.userId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    MvcResult recommendRes = mvc.perform(post("/api/v1/operateur/suspensions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"citoyen_id":"%s","motif":"Test anti-fraude workflow","operateur_id":"%s"}
                """.formatted(citizen.userId(), operator.userId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andReturn();
    String suspensionId = readBody(recommendRes).get("id").asText();

    mvc.perform(put("/api/v1/superadmin/suspensions/{id}/decision", suspensionId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"statut":"APPROUVEE","superadmin_id":"%s","justification":"Decision test workflow"}
                """.formatted(superadmin.userId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    mvc.perform(get("/api/v1/superadmin/users"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/v1/superadmin/logs"))
        .andExpect(status().isOk());

    // Vote module + anti double vote
    MvcResult electionsRes = mvc.perform(get("/api/v1/elections"))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode elections = readBody(electionsRes);
    JsonNode electionInProgress = null;
    for (JsonNode e : elections) {
      if ("EN_COURS".equals(e.path("statut").asText())) {
        electionInProgress = e;
        break;
      }
    }
    assertNotNull(electionInProgress, "Aucune élection EN_COURS trouvée");
    String electionEnCoursId = electionInProgress.get("id").asText();

    MvcResult openCandidatesRes = mvc.perform(get("/api/v1/elections/{id}/candidats", electionEnCoursId))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode openCandidates = readBody(openCandidatesRes);
    assertTrue(openCandidates.isArray());
    assertFalse(openCandidates.isEmpty());
    String openCandidateId = openCandidates.get(0).get("id").asText();

    String votePayload = """
        {"election_id":"%s","candidat_id":"%s","citoyen_id":"%s"}
        """.formatted(electionEnCoursId, openCandidateId, citizen.userId());

    MvcResult firstVoteRes = mvc.perform(post("/api/v1/votes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(votePayload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andReturn();
    String confirmation = readBody(firstVoteRes).get("confirmation_number").asText();
    assertTrue(confirmation.startsWith("V-"));

    mvc.perform(get("/api/v1/votes/verify/{token}", confirmation))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.election_id").value(electionEnCoursId))
        .andExpect(jsonPath("$.candidat_id").value(openCandidateId));

    mvc.perform(post("/api/v1/votes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(votePayload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Vote multiple interdit"));
  }

  private LoginSession loginWithOtp(String cni, String phone) throws Exception {
    MvcResult loginRes = mvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"cni":"%s","telephone":"%s"}
                """.formatted(cni, phone)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.requiresOtp").value(true))
        .andReturn();
    JsonNode loginBody = readBody(loginRes);
    String otp = loginBody.path("otp").asText();
    assertFalse(otp.isBlank(), "OTP debug absent, vérifier naatalvote.auth.debug-return-otp");

    MvcResult verifyRes = mvc.perform(post("/api/v1/auth/otp/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"cni":"%s","telephone":"%s","otp":"%s"}
                """.formatted(cni, phone, otp)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andReturn();
    JsonNode verifyBody = readBody(verifyRes);
    return new LoginSession(
        verifyBody.path("token").asText(),
        verifyBody.path("user").path("id").asText()
    );
  }

  private JsonNode readBody(MvcResult result) throws Exception {
    return om.readTree(result.getResponse().getContentAsString());
  }

  private record LoginSession(String token, String userId) {}
}
