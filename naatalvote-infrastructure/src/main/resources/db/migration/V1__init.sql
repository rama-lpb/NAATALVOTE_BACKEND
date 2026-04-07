-- NAATALVOTE Secure - Schema initial (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS utilisateur (
  id UUID PRIMARY KEY,
  numero_cni VARCHAR(50) UNIQUE NOT NULL,
  nom VARCHAR(100) NOT NULL,
  prenom VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS utilisateur_role (
  utilisateur_id UUID NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
  role VARCHAR(30) NOT NULL,
  PRIMARY KEY (utilisateur_id, role)
);

CREATE TABLE IF NOT EXISTS utilisateur_telephone (
  utilisateur_id UUID NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
  telephone VARCHAR(30) NOT NULL,
  PRIMARY KEY (utilisateur_id, telephone)
);

CREATE TABLE IF NOT EXISTS election (
  id UUID PRIMARY KEY,
  titre VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  type_election VARCHAR(20) NOT NULL,
  date_debut TIMESTAMP NOT NULL,
  date_fin TIMESTAMP NOT NULL,
  statut VARCHAR(20) NOT NULL,
  admin_id UUID NOT NULL,
  CONSTRAINT chk_dates CHECK (date_fin > date_debut)
);

CREATE TABLE IF NOT EXISTS candidat (
  id UUID PRIMARY KEY,
  election_id UUID NOT NULL REFERENCES election(id) ON DELETE CASCADE,
  nom VARCHAR(100) NOT NULL,
  prenom VARCHAR(100) NOT NULL,
  parti_politique VARCHAR(200) NOT NULL,
  biographie TEXT NOT NULL,
  photo_url VARCHAR(500) NOT NULL,
  programme_url VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS vote (
  id UUID PRIMARY KEY,
  token_anonyme UUID UNIQUE NOT NULL,
  candidat_id UUID NOT NULL REFERENCES candidat(id),
  election_id UUID NOT NULL REFERENCES election(id),
  horodatage TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_vote_token ON vote(token_anonyme);
CREATE INDEX IF NOT EXISTS idx_vote_election ON vote(election_id);

CREATE TABLE IF NOT EXISTS trace_vote (
  id UUID PRIMARY KEY,
  citoyen_id UUID NOT NULL REFERENCES utilisateur(id),
  election_id UUID NOT NULL REFERENCES election(id),
  a_vote BOOLEAN NOT NULL DEFAULT TRUE,
  horodatage TIMESTAMP NULL,
  UNIQUE (citoyen_id, election_id)
);

CREATE TABLE IF NOT EXISTS fraude (
  id UUID PRIMARY KEY,
  type_fraude VARCHAR(50) NOT NULL,
  citoyen_id UUID NULL REFERENCES utilisateur(id),
  election_id UUID NULL REFERENCES election(id),
  description TEXT NOT NULL,
  statut VARCHAR(20) NOT NULL,
  date_detection TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  operateur_id UUID NULL REFERENCES utilisateur(id),
  date_traitement TIMESTAMP NULL,
  ip VARCHAR(45) NULL
);

CREATE TABLE IF NOT EXISTS suspension (
  id UUID PRIMARY KEY,
  citoyen_id UUID NOT NULL REFERENCES utilisateur(id),
  motif TEXT NOT NULL,
  operateur_id UUID NOT NULL REFERENCES utilisateur(id),
  superadmin_id UUID NULL REFERENCES utilisateur(id),
  statut VARCHAR(20) NOT NULL,
  date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  date_decision TIMESTAMP NULL,
  justification TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS action_log (
  id UUID PRIMARY KEY,
  type_action VARCHAR(50) NOT NULL,
  utilisateur_id UUID NOT NULL REFERENCES utilisateur(id),
  description TEXT NOT NULL,
  horodatage TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  adresse_ip VARCHAR(45) NULL,
  signature_cryptographique VARCHAR(512) NULL
);
CREATE INDEX IF NOT EXISTS idx_log_type ON action_log(type_action);
CREATE INDEX IF NOT EXISTS idx_log_user ON action_log(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_log_date ON action_log(horodatage DESC);

-- Append-only: block UPDATE/DELETE on action_log
CREATE OR REPLACE FUNCTION action_log_immutable() RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'action_log is append-only';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_action_log_no_update ON action_log;
CREATE TRIGGER trg_action_log_no_update
BEFORE UPDATE OR DELETE ON action_log
FOR EACH ROW EXECUTE FUNCTION action_log_immutable();

