-- Deterministic fixtures for local PostgreSQL work.
-- Idempotent inserts so this migration can run safely on existing demo DBs.

-- Users
INSERT INTO utilisateur (id, numero_cni, nom, prenom, email, date_naissance, adresse) VALUES
  ('11111111-1111-1111-1111-111111111111', '20119990214000017', 'DIOP', 'Awa', 'awa.diop@example.com', '1999-02-14', 'Dakar'),
  ('22222222-2222-2222-2222-222222222222', '10219951003000028', 'NDIAYE', 'Moussa', 'moussa.ndiaye@example.com', '1995-10-03', 'Thiès'),
  ('33333333-3333-3333-3333-333333333333', '20320010721000039', 'SARR', 'Fatou', 'fatou.sarr@example.com', '2001-07-21', 'Saint-Louis'),
  ('44444444-4444-4444-4444-444444444444', '10419881201000040', 'BA', 'Ibrahima', 'ibrahima.ba@example.com', '1988-12-01', 'Ziguinchor'),
  ('55555555-5555-5555-5555-555555555555', '20520031130000073', 'MBAYE', 'Mariama', 'mariama.mbaye@example.com', '2003-11-30', 'Dakar')
ON CONFLICT (numero_cni) DO NOTHING;

INSERT INTO utilisateur_role (utilisateur_id, role) VALUES
  ('11111111-1111-1111-1111-111111111111', 'CITOYEN'),
  ('22222222-2222-2222-2222-222222222222', 'CITOYEN'),
  ('22222222-2222-2222-2222-222222222222', 'ADMIN'),
  ('33333333-3333-3333-3333-333333333333', 'CITOYEN'),
  ('33333333-3333-3333-3333-333333333333', 'OPERATEUR'),
  ('44444444-4444-4444-4444-444444444444', 'CITOYEN'),
  ('44444444-4444-4444-4444-444444444444', 'SUPERADMIN'),
  ('55555555-5555-5555-5555-555555555555', 'CITOYEN')
ON CONFLICT (utilisateur_id, role) DO NOTHING;

INSERT INTO utilisateur_telephone (utilisateur_id, telephone) VALUES
  ('11111111-1111-1111-1111-111111111111', '+221770000001'),
  ('22222222-2222-2222-2222-222222222222', '+221770000002'),
  ('33333333-3333-3333-3333-333333333333', '+221770000003'),
  ('44444444-4444-4444-4444-444444444444', '+221770000004'),
  ('55555555-5555-5555-5555-555555555555', '+221770000007')
ON CONFLICT (utilisateur_id, telephone) DO NOTHING;

-- Elections
INSERT INTO election (
  id, titre, description, type_election, date_debut, date_fin, statut, admin_id, region, total_electeurs, votes_count
) VALUES
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'Présidentielle Démo 2026',
    'Scrutin de démonstration terminé',
    'PRESIDENTIELLE',
    '2026-03-01T08:00:00Z',
    '2026-03-02T18:00:00Z',
    'CLOTUREE',
    '22222222-2222-2222-2222-222222222222',
    'Nationale',
    500000,
    320000
  ),
  (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'Législatives Dakar Démo 2026',
    'Scrutin en cours pour tests de vote',
    'LEGISLATIVE',
    '2026-04-10T08:00:00Z',
    '2026-04-20T18:00:00Z',
    'EN_COURS',
    '22222222-2222-2222-2222-222222222222',
    'Dakar',
    120000,
    24000
  ),
  (
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'Municipales Saint-Louis Démo 2026',
    'Scrutin programmé pour tests admin',
    'MUNICIPALE',
    '2026-06-01T08:00:00Z',
    '2026-06-01T18:00:00Z',
    'PROGRAMMEE',
    '22222222-2222-2222-2222-222222222222',
    'Saint-Louis',
    45000,
    0
  )
ON CONFLICT (id) DO NOTHING;

-- Candidates
INSERT INTO candidat (
  id, election_id, nom, prenom, parti_politique, biographie, photo_url, programme_url, votes_count, color
) VALUES
  ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'DIOP', 'Aminata', 'Alliance Républicaine', 'Candidate 1', '', '', 180000, '#1f5a33'),
  ('a2a2a2a2-a2a2-a2a2-a2a2-a2a2a2a2a2a2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SOW', 'Ibrahima', 'Mouvement Citoyen', 'Candidate 2', '', '', 140000, '#2980b9'),
  ('b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'BA', 'Seydou', 'Union Progressiste', 'Candidate 3', '', '', 9000, '#1abc9c'),
  ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'GAYE', 'Ndeye', 'Rassemblement Uni', 'Candidate 4', '', '', 8000, '#e74c3c'),
  ('c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'DIALLO', 'Aissatou', 'Coalition Citoyenne', 'Candidate 5', '', '', 0, '#16a085')
ON CONFLICT (id) DO NOTHING;

-- Votes and traces (on EN_COURS election)
INSERT INTO vote (id, token_anonyme, candidat_id, election_id, horodatage) VALUES
  ('d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-04-12T10:00:00Z'),
  ('d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-04-12T10:05:00Z')
ON CONFLICT (id) DO NOTHING;

INSERT INTO trace_vote (id, citoyen_id, election_id, a_vote, horodatage) VALUES
  ('f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1', '11111111-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', TRUE, '2026-04-12T10:00:00Z'),
  ('f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2', '55555555-5555-5555-5555-555555555555', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', TRUE, '2026-04-12T10:05:00Z')
ON CONFLICT (citoyen_id, election_id) DO NOTHING;

-- Fraud alerts and suspensions
INSERT INTO fraude (
  id, type_fraude, citoyen_id, election_id, description, statut, date_detection, operateur_id, date_traitement, ip
) VALUES
  (
    '12121212-1212-1212-1212-121212121212',
    'VOTE_MULTIPLE',
    '55555555-5555-5555-5555-555555555555',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'Tentatives multiples détectées',
    'EN_ANALYSE',
    '2026-04-12T11:00:00Z',
    '33333333-3333-3333-3333-333333333333',
    NULL,
    '41.82.64.100'
  )
ON CONFLICT (id) DO NOTHING;

INSERT INTO suspension (
  id, citoyen_id, motif, operateur_id, superadmin_id, statut, date_creation, date_decision, justification
) VALUES
  (
    '34343434-3434-3434-3434-343434343434',
    '55555555-5555-5555-5555-555555555555',
    'Suspicion de vote multiple',
    '33333333-3333-3333-3333-333333333333',
    NULL,
    'EN_ATTENTE',
    '2026-04-12T12:00:00Z',
    NULL,
    ''
  )
ON CONFLICT (id) DO NOTHING;

-- Action log
INSERT INTO action_log (
  id, type_action, utilisateur_id, description, horodatage, adresse_ip, signature_cryptographique
) VALUES
  (
    '56565656-5656-5656-5656-565656565656',
    'CREATE_ELECTION',
    '22222222-2222-2222-2222-222222222222',
    'Fixture: création scrutin de démo',
    '2026-04-01T09:00:00Z',
    '127.0.0.1',
    'HMAC-FIXTURE'
  ),
  (
    '78787878-7878-7878-7878-787878787878',
    'SUSPENSION_RECOMMENDED',
    '33333333-3333-3333-3333-333333333333',
    'Fixture: suspension recommandée',
    '2026-04-12T12:00:00Z',
    '41.82.64.100',
    'HMAC-FIXTURE'
  )
ON CONFLICT (id) DO NOTHING;
