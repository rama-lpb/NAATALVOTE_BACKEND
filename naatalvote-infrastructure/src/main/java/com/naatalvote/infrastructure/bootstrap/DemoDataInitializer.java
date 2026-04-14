package com.naatalvote.infrastructure.bootstrap;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.domain.audit.ActionLog;
import com.naatalvote.domain.audit.ActionType;
import com.naatalvote.domain.auth.User;
import com.naatalvote.domain.auth.UserRole;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.election.Candidate;
import com.naatalvote.domain.election.Election;
import com.naatalvote.domain.election.ElectionStatus;
import com.naatalvote.domain.election.ElectionType;
import com.naatalvote.domain.fraud.FraudAlert;
import com.naatalvote.domain.fraud.FraudAlertStatus;
import com.naatalvote.domain.fraud.FraudType;
import com.naatalvote.domain.fraud.Suspension;
import com.naatalvote.domain.fraud.SuspensionStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds realistic demo data at startup (in-memory profile).
 * Uses the same CNIs as appdaff-sim/fixtures/users.json so that
 * logging in with a fixture CNI retrieves the pre-seeded user
 * and all related data (elections, alerts, etc.).
 */
@Component
public class DemoDataInitializer implements ApplicationRunner {

  private final UserRepositoryPort users;
  private final ElectionRepositoryPort elections;
  private final CandidateRepositoryPort candidates;
  private final FraudAlertRepositoryPort alerts;
  private final SuspensionRepositoryPort suspensions;
  private final ActionLogRepositoryPort logs;

  public DemoDataInitializer(
      UserRepositoryPort users,
      ElectionRepositoryPort elections,
      CandidateRepositoryPort candidates,
      FraudAlertRepositoryPort alerts,
      SuspensionRepositoryPort suspensions,
      ActionLogRepositoryPort logs
  ) {
    this.users = users;
    this.elections = elections;
    this.candidates = candidates;
    this.alerts = alerts;
    this.suspensions = suspensions;
    this.logs = logs;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!users.findAll().isEmpty()) {
      return; // Already seeded
    }

    Instant now = Instant.now();

    // ── Users (CNIs match fixtures/users.json) ────────────────────────────────
    // Comptes avec rôles fonctionnels (CNIs = fixtures/users.json)
    User citoyen1  = saveUser("20119990214000017", "DIOP",    "Awa",       "awa.diop@example.com",         List.of("+221770000001"), List.of(UserRole.CITOYEN),                        "1999-02-14", "Dakar, Sénégal");
    User admin     = saveUser("10219951003000028", "NDIAYE",  "Moussa",    "moussa.ndiaye@example.com",    List.of("+221770000002", "+221760000002"), List.of(UserRole.CITOYEN, UserRole.ADMIN),       "1995-10-03", "Thiès, Sénégal");
    User operateur = saveUser("20320010721000039", "SARR",    "Fatou",     "fatou.sarr@example.com",       List.of("+221770000003"), List.of(UserRole.CITOYEN, UserRole.OPERATEUR),    "2001-07-21", "Saint-Louis, Sénégal");
    User superadmin= saveUser("10419881201000040", "BA",      "Ibrahima",  "ibrahima.ba@example.com",      List.of("+221770000004"), List.of(UserRole.CITOYEN, UserRole.SUPERADMIN),  "1988-12-01", "Ziguinchor, Sénégal");

    // Citoyens supplémentaires (pour alimenter alertes, suspensions, votes)
    User citoyen2  = saveUser("20120020415000051", "FALL",    "Khady",     "khady.fall@example.com",       List.of("+221770000005"), List.of(UserRole.CITOYEN), "2002-04-15", "Dakar, Sénégal");
    User citoyen3  = saveUser("10519780630000062", "DIALLO",  "Ousmane",   "ousmane.diallo@example.com",   List.of("+221770000006"), List.of(UserRole.CITOYEN), "1978-06-30", "Kaolack, Sénégal");
    User citoyen4  = saveUser("20619921115000073", "MBAYE",   "Mariama",   "mariama.mbaye@example.com",    List.of("+221770000007"), List.of(UserRole.CITOYEN), "1992-11-15", "Louga, Sénégal");
    User citoyen5  = saveUser("10720050320000084", "CISSE",   "Mamadou",   "mamadou.cisse@example.com",    List.of("+221770000008"), List.of(UserRole.CITOYEN), "2005-03-20", "Ziguinchor, Sénégal");
    User citoyen6  = saveUser("20820031210000095", "TOURE",   "Rokhaya",   "rokhaya.toure@example.com",    List.of("+221770000009"), List.of(UserRole.CITOYEN), "2003-12-10", "Thiès, Sénégal");
    User citoyen7  = saveUser("10919960508000106", "GUEYE",   "Pape",      "pape.gueye@example.com",       List.of("+221770000010"), List.of(UserRole.CITOYEN), "1996-05-08", "Dakar, Sénégal");
    User citoyen8  = saveUser("21019850820000117", "SECK",    "Adja",      "adja.seck@example.com",        List.of("+221770000011"), List.of(UserRole.CITOYEN), "1985-08-20", "Saint-Louis, Sénégal");
    User citoyen9  = saveUser("10219720312000128", "DIOUF",   "Babacar",   "babacar.diouf@example.com",    List.of("+221770000012"), List.of(UserRole.CITOYEN), "1972-03-12", "Ziguinchor, Sénégal");
    User citoyen10 = saveUser("20320001104000139", "LY",      "Fatoumata", "fatoumata.ly@example.com",     List.of("+221770000013"), List.of(UserRole.CITOYEN), "2000-11-04", "Kaolack, Sénégal");
    // Opérateur secondaire
    User operateur2= saveUser("10420010105000084", "WADE",    "Serigne",   "serigne.wade@example.com",     List.of("+221770000014"), List.of(UserRole.CITOYEN, UserRole.OPERATEUR),   "2001-01-05", "Dakar, Sénégal");

    // ── Elections ─────────────────────────────────────────────────────────────

    // Election 1 : Terminée (il y a 30 jours)
    Election elecTerminee = new Election(
        Ids.newUuid(),
        "Présidentielle 2025",
        "Élection présidentielle nationale, tour unique.",
        ElectionType.PRESIDENTIELLE,
        now.minus(40, ChronoUnit.DAYS),
        now.minus(10, ChronoUnit.DAYS),
        ElectionStatus.CLOTUREE,
        admin.id(),
        List.of(),
        "Nationale",
        4_200_000,
        1_842_500
    );
    elections.save(elecTerminee);

    saveCandidateWithVotes(elecTerminee.id(), "DIOP",  "Aminata",   "Alliance Républicaine", "Ancienne ministre de l'Économie, réformiste.", 680_000, "#1f5a33");
    saveCandidateWithVotes(elecTerminee.id(), "SOW",   "Ibrahima",  "Mouvement Citoyen",     "Leader syndical, défenseur des droits sociaux.", 520_000, "#2980b9");
    saveCandidateWithVotes(elecTerminee.id(), "NDIAYE","Cheikh",    "Parti Démocratique",    "Avocat et militant des droits de l'homme.",     420_000, "#e67e22");
    saveCandidateWithVotes(elecTerminee.id(), "FALL",  "Rokhaya",   "Alternative Verte",     "Environnementaliste et économiste.",             222_500, "#8e44ad");

    // Election 2 : En cours (commencée hier, termine dans 5 jours)
    Election elecEnCours = new Election(
        Ids.newUuid(),
        "Législatives Dakar 2026",
        "Élection des députés de la région de Dakar.",
        ElectionType.LEGISLATIVE,
        now.minus(1, ChronoUnit.DAYS),
        now.plus(5, ChronoUnit.DAYS),
        ElectionStatus.EN_COURS,
        admin.id(),
        List.of(),
        "Dakar",
        820_000,
        134_200
    );
    elections.save(elecEnCours);

    saveCandidateWithVotes(elecEnCours.id(), "BA",     "Seydou",   "Union Progressiste",  "Ingénieur civil, promoteur de l'infrastructure.",  45_000, "#1abc9c");
    saveCandidateWithVotes(elecEnCours.id(), "GAYE",   "Ndéye",    "Rassemblement Uni",   "Femme d'affaires, présidente d'une PME.",          38_200, "#e74c3c");
    saveCandidateWithVotes(elecEnCours.id(), "THIAM",  "Modou",    "Parti National",      "Médecin de campagne engagé en santé publique.",    51_000, "#f39c12");

    // Election 3 : Programmée (dans 3 semaines)
    Election elecProg = new Election(
        Ids.newUuid(),
        "Municipales Saint-Louis 2026",
        "Élection du conseil municipal de Saint-Louis.",
        ElectionType.MUNICIPALE,
        now.plus(21, ChronoUnit.DAYS),
        now.plus(22, ChronoUnit.DAYS),
        ElectionStatus.PROGRAMMEE,
        admin.id(),
        List.of(),
        "Saint-Louis",
        215_000,
        0
    );
    elections.save(elecProg);

    saveCandidateWithVotes(elecProg.id(), "DIALLO", "Aissatou",  "Coalition Citoyenne", "Enseignante militante pour l'éducation.", 0, "#16a085");
    saveCandidateWithVotes(elecProg.id(), "KANE",   "Hamidou",   "Force du Peuple",     "Commerçant leader associatif local.",     0, "#2c3e50");
    saveCandidateWithVotes(elecProg.id(), "SARR",   "Yaye",      "Parti Républicain",   "Juriste spécialisée en droit foncier.",   0, "#c0392b");

    // ── Fraud Alerts ──────────────────────────────────────────────────────────

    // ── Fraud Alerts ──────────────────────────────────────────────────────────

    // NOUVELLE — alertes fraîches non encore assignées
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.VOTE_MULTIPLE,   citoyen2.id(),  elecEnCours.id(),  "3 tentatives de vote depuis 2 terminaux différents en 8 min",         FraudAlertStatus.NOUVELLE,   now.minus(2, ChronoUnit.HOURS),   null,         null,                              "41.208.10.5"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.IP_SUSPECTE,     citoyen3.id(),  elecEnCours.id(),  "IP provenant d'un réseau tor exit-node répertorié",                   FraudAlertStatus.NOUVELLE,   now.minus(55, ChronoUnit.MINUTES),null,         null,                              "185.220.101.23"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.CNI_INVALIDE,    citoyen4.id(),  elecEnCours.id(),  "Format CNI invalide lors de la vérification DAFF",                    FraudAlertStatus.NOUVELLE,   now.minus(30, ChronoUnit.MINUTES),null,         null,                              "41.82.64.100"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.PATTERN_SUSPECT, citoyen5.id(),  elecTerminee.id(), "Séquence de connexions/déconnexions rapide",                          FraudAlertStatus.NOUVELLE,   now.minus(4, ChronoUnit.HOURS),   null,         null,                              "196.207.3.88"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.VOTE_MULTIPLE,   citoyen6.id(),  elecEnCours.id(),  "Vote soumis 2 fois via API mobile et web simultanément",              FraudAlertStatus.NOUVELLE,   now.minus(18, ChronoUnit.MINUTES),null,         null,                              "196.207.44.12"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.IP_SUSPECTE,     citoyen7.id(),  elecEnCours.id(),  "Connexion depuis un datacenter AWS non résidentiel (us-east-1)",      FraudAlertStatus.NOUVELLE,   now.minus(10, ChronoUnit.MINUTES),null,         null,                              "54.192.0.88"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.CNI_INVALIDE,    citoyen8.id(),  elecTerminee.id(), "CNI présenté non référencé dans la base DGAE",                        FraudAlertStatus.NOUVELLE,   now.minus(1, ChronoUnit.HOURS),   null,         null,                              "41.82.192.5"));

    // EN_ANALYSE — prises en charge (opérateur principal)
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.VOTE_MULTIPLE,   citoyen1.id(),  elecTerminee.id(), "Double vote détecté sur 2 bureaux différents",                        FraudAlertStatus.EN_ANALYSE, now.minus(6, ChronoUnit.HOURS),   operateur.id(),null,                              "41.208.55.12"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.IP_SUSPECTE,     citoyen2.id(),  elecTerminee.id(), "Connexion depuis une IP étrangère non déclarée",                      FraudAlertStatus.EN_ANALYSE, now.minus(8, ChronoUnit.HOURS),   operateur.id(),null,                              "92.118.160.44"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.PATTERN_SUSPECT, citoyen9.id(),  elecEnCours.id(),  "Rythme de soumission anormal — 12 tentatives en 3 min",               FraudAlertStatus.EN_ANALYSE, now.minus(3, ChronoUnit.HOURS),   operateur.id(),null,                              "41.77.33.201"));
    // EN_ANALYSE — prises en charge (opérateur secondaire)
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.CNI_INVALIDE,    citoyen10.id(), elecEnCours.id(),  "Numéro CNI appartenant à un citoyen décédé selon registre DGAE",      FraudAlertStatus.EN_ANALYSE, now.minus(5, ChronoUnit.HOURS),   operateur2.id(),null,                             "41.82.201.10"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.VOTE_MULTIPLE,   citoyen6.id(),  elecTerminee.id(), "3 votes détectés avec le même jeton anonyme (collision UUID)",         FraudAlertStatus.EN_ANALYSE, now.minus(12, ChronoUnit.HOURS),  operateur2.id(),null,                             "196.207.12.44"));

    // RESOLUE
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.PATTERN_SUSPECT, citoyen3.id(),  elecTerminee.id(), "Comportement suspect — aucune irrégularité confirmée",                FraudAlertStatus.RESOLUE,    now.minus(2, ChronoUnit.DAYS),    operateur.id(), now.minus(1, ChronoUnit.DAYS),  "196.207.3.55"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.CNI_INVALIDE,    citoyen4.id(),  elecTerminee.id(), "CNI expiré — dossier transmis à la DGAE",                             FraudAlertStatus.RESOLUE,    now.minus(3, ChronoUnit.DAYS),    operateur.id(), now.minus(2, ChronoUnit.DAYS),  "41.82.64.101"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.IP_SUSPECTE,     citoyen5.id(),  elecTerminee.id(), "Fausse alerte — IP d'un VPN professionnel autorisé",                  FraudAlertStatus.RESOLUE,    now.minus(5, ChronoUnit.DAYS),    operateur.id(), now.minus(4, ChronoUnit.DAYS),  "185.220.50.11"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.VOTE_MULTIPLE,   citoyen7.id(),  elecTerminee.id(), "Double tentative confirmée comme bug client mobile — ticket ouvert", FraudAlertStatus.RESOLUE,    now.minus(6, ChronoUnit.DAYS),    operateur2.id(),now.minus(5, ChronoUnit.DAYS),  "41.208.10.90"));
    alerts.save(new FraudAlert(Ids.newUuid(), FraudType.PATTERN_SUSPECT, citoyen8.id(),  elecTerminee.id(), "Connexions rapides dues à une mauvaise connexion réseau, non suspectes",FraudAlertStatus.RESOLUE,  now.minus(8, ChronoUnit.DAYS),    operateur2.id(),now.minus(7, ChronoUnit.DAYS),  "196.207.3.100"));

    // ── Suspensions ───────────────────────────────────────────────────────────

    suspensions.save(new Suspension(Ids.newUuid(), citoyen2.id(),
        "Vote multiple confirmé lors de la Présidentielle 2025",
        operateur.id(), null, SuspensionStatus.EN_ATTENTE,
        now.minus(1, ChronoUnit.DAYS), null, ""));

    suspensions.save(new Suspension(Ids.newUuid(), citoyen6.id(),
        "Collision UUID et vote multiple simultané — fraude avérée",
        operateur2.id(), null, SuspensionStatus.EN_ATTENTE,
        now.minus(10, ChronoUnit.HOURS), null, ""));

    suspensions.save(new Suspension(Ids.newUuid(), citoyen3.id(),
        "IP suspecte et profil incohérent avec l'adresse déclarée",
        operateur.id(), superadmin.id(), SuspensionStatus.APPROUVEE,
        now.minus(5, ChronoUnit.DAYS), now.minus(4, ChronoUnit.DAYS),
        "Profil vérifié — suspension confirmée"));

    suspensions.save(new Suspension(Ids.newUuid(), citoyen9.id(),
        "Rythme de soumission anormal, investigation terminée",
        operateur.id(), superadmin.id(), SuspensionStatus.APPROUVEE,
        now.minus(10, ChronoUnit.DAYS), now.minus(9, ChronoUnit.DAYS),
        "Comportement automatisé confirmé, compte suspendu 30 jours"));

    suspensions.save(new Suspension(Ids.newUuid(), citoyen4.id(),
        "CNI signalée invalide lors de la vérification",
        operateur.id(), superadmin.id(), SuspensionStatus.REJETEE,
        now.minus(7, ChronoUnit.DAYS), now.minus(6, ChronoUnit.DAYS),
        "Vérification manuelle : CNI valide. Fausse alerte."));

    suspensions.save(new Suspension(Ids.newUuid(), citoyen5.id(),
        "IP suspecte — VPN professionnel signalé par erreur",
        operateur.id(), superadmin.id(), SuspensionStatus.REJETEE,
        now.minus(12, ChronoUnit.DAYS), now.minus(11, ChronoUnit.DAYS),
        "VPN d'entreprise autorisé — aucune fraude détectée."));

    // ── Action Logs ───────────────────────────────────────────────────────────

    log(ActionType.USER_CREATED,           superadmin.id(),  now.minus(60, ChronoUnit.DAYS),  "Initialisation de la plateforme NaatalVote");
    log(ActionType.USER_CREATED,           superadmin.id(),  now.minus(60, ChronoUnit.DAYS),  "Création compte ADMIN : Moussa NDIAYE");
    log(ActionType.USER_CREATED,           superadmin.id(),  now.minus(59, ChronoUnit.DAYS),  "Création compte OPERATEUR : Fatou SARR");
    log(ActionType.USER_CREATED,           superadmin.id(),  now.minus(59, ChronoUnit.DAYS),  "Création compte OPERATEUR : Serigne WADE");
    log(ActionType.CREATE_ELECTION,        admin.id(),       now.minus(45, ChronoUnit.DAYS),  "Programmation élection : Présidentielle 2025");
    log(ActionType.CREATE_CANDIDATE,       admin.id(),       now.minus(44, ChronoUnit.DAYS),  "Ajout candidat Aminata DIOP — Alliance Républicaine");
    log(ActionType.CREATE_CANDIDATE,       admin.id(),       now.minus(44, ChronoUnit.DAYS),  "Ajout candidat Ibrahima SOW — Mouvement Citoyen");
    log(ActionType.CREATE_CANDIDATE,       admin.id(),       now.minus(44, ChronoUnit.DAYS),  "Ajout candidat Cheikh NDIAYE — Parti Démocratique");
    log(ActionType.CREATE_CANDIDATE,       admin.id(),       now.minus(44, ChronoUnit.DAYS),  "Ajout candidat Rokhaya FALL — Alternative Verte");
    log(ActionType.LOGIN,                  admin.id(),       now.minus(15, ChronoUnit.DAYS),  "Connexion admin depuis 41.82.10.11");
    log(ActionType.LOGIN,                  citoyen1.id(),    now.minus(15, ChronoUnit.DAYS),  "Connexion citoyen depuis 41.82.64.200");
    log(ActionType.CAST_VOTE,              citoyen1.id(),    now.minus(15, ChronoUnit.DAYS),  "Vote anonyme enregistré — Présidentielle 2025");
    log(ActionType.CAST_VOTE,              citoyen2.id(),    now.minus(15, ChronoUnit.DAYS),  "Vote anonyme enregistré — Présidentielle 2025");
    log(ActionType.CAST_VOTE,              citoyen3.id(),    now.minus(14, ChronoUnit.DAYS),  "Vote anonyme enregistré — Présidentielle 2025");
    log(ActionType.CAST_VOTE,             citoyen4.id(),     now.minus(14, ChronoUnit.DAYS),  "Vote anonyme enregistré — Présidentielle 2025");
    log(ActionType.CAST_VOTE,             citoyen5.id(),     now.minus(13, ChronoUnit.DAYS),  "Vote anonyme enregistré — Présidentielle 2025");
    log(ActionType.FRAUD_ALERT_CREATED,    operateur.id(),   now.minus(6,  ChronoUnit.HOURS), "Alerte VOTE_MULTIPLE créée — citoyen " + citoyen1.id().toString().substring(0, 8));
    log(ActionType.FRAUD_ALERT_CREATED,    operateur.id(),   now.minus(8,  ChronoUnit.HOURS), "Alerte IP_SUSPECTE créée — citoyen " + citoyen2.id().toString().substring(0, 8));
    log(ActionType.FRAUD_ALERT_CREATED,    operateur2.id(),  now.minus(5,  ChronoUnit.HOURS), "Alerte CNI_INVALIDE créée — citoyen " + citoyen10.id().toString().substring(0, 8));
    log(ActionType.FRAUD_ALERT_TREATED,    operateur.id(),   now.minus(1,  ChronoUnit.DAYS),  "Alerte résolue : PATTERN_SUSPECT — aucune action requise");
    log(ActionType.FRAUD_ALERT_TREATED,    operateur.id(),   now.minus(2,  ChronoUnit.DAYS),  "Alerte résolue : CNI_INVALIDE — dossier DGAE");
    log(ActionType.FRAUD_ALERT_TREATED,    operateur2.id(),  now.minus(5,  ChronoUnit.DAYS),  "Alerte résolue : IP_SUSPECTE — VPN autorisé");
    log(ActionType.SUSPENSION_RECOMMENDED, operateur.id(),   now.minus(1,  ChronoUnit.DAYS),  "Suspension recommandée — citoyen " + citoyen2.id().toString().substring(0, 8));
    log(ActionType.SUSPENSION_RECOMMENDED, operateur2.id(),  now.minus(10, ChronoUnit.HOURS), "Suspension recommandée — citoyen " + citoyen6.id().toString().substring(0, 8));
    log(ActionType.SUSPENSION_DECIDED,     superadmin.id(),  now.minus(4,  ChronoUnit.DAYS),  "Suspension APPROUVEE — citoyen " + citoyen3.id().toString().substring(0, 8));
    log(ActionType.SUSPENSION_DECIDED,     superadmin.id(),  now.minus(9,  ChronoUnit.DAYS),  "Suspension APPROUVEE — citoyen " + citoyen9.id().toString().substring(0, 8));
    log(ActionType.SUSPENSION_DECIDED,     superadmin.id(),  now.minus(6,  ChronoUnit.DAYS),  "Suspension REJETEE — citoyen " + citoyen4.id().toString().substring(0, 8));
    log(ActionType.SUSPENSION_DECIDED,     superadmin.id(),  now.minus(11, ChronoUnit.DAYS),  "Suspension REJETEE — citoyen " + citoyen5.id().toString().substring(0, 8));
    log(ActionType.AUDIT_EXPORT,           superadmin.id(),  now.minus(9,  ChronoUnit.DAYS),  "Export audit CSV — période 2025-01-01 / 2025-12-31");
    log(ActionType.AUDIT_EXPORT,           superadmin.id(),  now.minus(30, ChronoUnit.DAYS),  "Export audit JSON — Présidentielle 2025 complète");
    log(ActionType.CREATE_ELECTION,        admin.id(),       now.minus(2,  ChronoUnit.DAYS),  "Programmation élection : Législatives Dakar 2026");
    log(ActionType.CREATE_ELECTION,        admin.id(),       now.minus(1,  ChronoUnit.DAYS),  "Programmation élection : Municipales Saint-Louis 2026");
    log(ActionType.USER_ROLES_UPDATED,     superadmin.id(),  now.minus(3,  ChronoUnit.DAYS),  "Mise à jour rôles : Serigne WADE → OPERATEUR");
    log(ActionType.LOGIN,                  admin.id(),       now.minus(2,  ChronoUnit.HOURS),  "Connexion admin depuis 41.82.10.11");
    log(ActionType.LOGIN,                  operateur.id(),   now.minus(3,  ChronoUnit.HOURS),  "Connexion opérateur depuis 196.207.3.1");
    log(ActionType.LOGIN,                  operateur2.id(),  now.minus(1,  ChronoUnit.HOURS),  "Connexion opérateur depuis 41.77.22.5");
    log(ActionType.LOGIN,                  superadmin.id(),  now.minus(30, ChronoUnit.MINUTES),"Connexion superadmin depuis 196.207.0.1");
    log(ActionType.OTP_SEND,               citoyen1.id(),    now.minus(15, ChronoUnit.DAYS),  "OTP envoyé — +221770000001");
    log(ActionType.OTP_VERIFY,             citoyen1.id(),    now.minus(15, ChronoUnit.DAYS),  "OTP validé — connexion réussie");
    log(ActionType.LOGOUT,                 citoyen1.id(),    now.minus(15, ChronoUnit.DAYS),  "Déconnexion après vote");
  }

  // ── Private helpers ────────────────────────────────────────────────────────

  private User saveUser(String cni, String nom, String prenom, String email,
      List<String> telephones, List<UserRole> roles, String dateNaissance, String adresse) {
    User u = new User(Ids.newUuid(), cni, nom, prenom, email, telephones, roles, dateNaissance, adresse);
    return users.save(u);
  }

  private Candidate saveCandidateWithVotes(UUID electionId, String nom, String prenom,
      String parti, String bio, int votesCount, String color) {
    Candidate c = new Candidate(Ids.newUuid(), electionId, nom, prenom, parti, bio, "", "", votesCount, color);
    return candidates.save(c);
  }

  private void log(ActionType type, UUID userId, Instant at, String description) {
    logs.append(new ActionLog(Ids.newUuid(), type, userId, description, at, null, "HMAC-DEMO"));
  }
}
