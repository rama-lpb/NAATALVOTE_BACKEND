-- Extended fixtures for demo videos (admin/operator/superadmin scenarios)
-- Safe to run on top of V4 thanks to ON CONFLICT clauses.

-- Additional citizens for richer fraud/suspension demos
INSERT INTO utilisateur (id, numero_cni, nom, prenom, email, date_naissance, adresse) VALUES
  ('66666666-6666-6666-6666-666666666666', '20620020415000051', 'FALL', 'Khady', 'khady.fall@example.com', '2002-04-15', 'Dakar'),
  ('77777777-7777-7777-7777-777777777777', '10719780630000062', 'DIALLO', 'Ousmane', 'ousmane.diallo@example.com', '1978-06-30', 'Kaolack'),
  ('88888888-8888-8888-8888-888888888888', '20819921115000074', 'CISSE', 'Mamadou', 'mamadou.cisse@example.com', '1992-11-15', 'Louga')
ON CONFLICT (numero_cni) DO NOTHING;

INSERT INTO utilisateur_role (utilisateur_id, role) VALUES
  ('66666666-6666-6666-6666-666666666666', 'CITOYEN'),
  ('77777777-7777-7777-7777-777777777777', 'CITOYEN'),
  ('88888888-8888-8888-8888-888888888888', 'CITOYEN')
ON CONFLICT (utilisateur_id, role) DO NOTHING;

INSERT INTO utilisateur_telephone (utilisateur_id, telephone) VALUES
  ('66666666-6666-6666-6666-666666666666', '+221770000008'),
  ('77777777-7777-7777-7777-777777777777', '+221770000009'),
  ('88888888-8888-8888-8888-888888888888', '+221770000010')
ON CONFLICT (utilisateur_id, telephone) DO NOTHING;

-- More scheduled elections (not yet voted) for admin tests
INSERT INTO election (
  id, titre, description, type_election, date_debut, date_fin, statut, admin_id, region, total_electeurs, votes_count
) VALUES
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Regionales Thies Demo 2026', 'Election programmee non encore ouverte', 'REGIONALE', '2026-05-25T08:00:00Z', '2026-05-26T18:00:00Z', 'PROGRAMMEE', '22222222-2222-2222-2222-222222222222', 'Thies', 98000, 0),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Municipales Ziguinchor Demo 2026', 'Election programmee pour test ajout candidats', 'MUNICIPALE', '2026-07-10T08:00:00Z', '2026-07-11T18:00:00Z', 'PROGRAMMEE', '22222222-2222-2222-2222-222222222222', 'Ziguinchor', 67000, 0),
  ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Legislatives Kaolack Demo 2026', 'Election programmee prete pour campagnes', 'LEGISLATIVE', '2026-08-15T08:00:00Z', '2026-08-16T18:00:00Z', 'PROGRAMMEE', '22222222-2222-2222-2222-222222222222', 'Kaolack', 81000, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO candidat (
  id, election_id, nom, prenom, parti_politique, biographie, photo_url, programme_url, votes_count, color
) VALUES
  ('c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'KANE', 'Hamidou', 'Force du Peuple', 'Candidate 6', '', '', 0, '#2c3e50'),
  ('d1c1d1c1-d1c1-d1c1-d1c1-d1c1d1c1d1c1', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'THIAM', 'Modou', 'Parti National', 'Candidate 7', '', '', 0, '#f39c12'),
  ('d2c2d2c2-d2c2-d2c2-d2c2-d2c2d2c2d2c2', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'SARR', 'Yaye', 'Parti Republicain', 'Candidate 8', '', '', 0, '#8e44ad'),
  ('e1c1e1c1-e1c1-e1c1-e1c1-e1c1e1c1e1c1', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'SECK', 'Adja', 'Coalition Locale', 'Candidate 9', '', '', 0, '#16a085'),
  ('e2c2e2c2-e2c2-e2c2-e2c2-e2c2e2c2e2c2', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'LY', 'Fatoumata', 'Alternative Verte', 'Candidate 10', '', '', 0, '#27ae60'),
  ('f1c1f1c1-f1c1-f1c1-f1c1-f1c1f1c1f1c1', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'DIOUF', 'Babacar', 'Rassemblement Uni', 'Candidate 11', '', '', 0, '#c0392b'),
  ('f2c2f2c2-f2c2-f2c2-f2c2-f2c2f2c2f2c2', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 'TOURE', 'Rokhaya', 'Union Progressiste', 'Candidate 12', '', '', 0, '#3498db')
ON CONFLICT (id) DO NOTHING;

-- More vote traces on the in-progress election
INSERT INTO vote (id, token_anonyme, candidat_id, election_id, horodatage) VALUES
  ('d3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3', 'e3e3e3e3-e3e3-e3e3-e3e3-e3e3e3e3e3e3', 'b1b1b1b1-b1b1-b1b1-b1b1-b1b1b1b1b1b1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-04-20T09:10:00Z'),
  ('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'e4e4e4e4-e4e4-e4e4-e4e4-e4e4e4e4e4e4', 'b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-04-20T09:25:00Z')
ON CONFLICT (id) DO NOTHING;

INSERT INTO trace_vote (id, citoyen_id, election_id, a_vote, horodatage) VALUES
  ('f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3', '66666666-6666-6666-6666-666666666666', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', TRUE, '2026-04-20T09:10:00Z'),
  ('f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4', '77777777-7777-7777-7777-777777777777', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', TRUE, '2026-04-20T09:25:00Z')
ON CONFLICT (citoyen_id, election_id) DO NOTHING;

-- Rich fraud pipeline: new + in analysis + resolved
INSERT INTO fraude (
  id, type_fraude, citoyen_id, election_id, description, statut, date_detection, operateur_id, date_traitement, ip
) VALUES
  ('13131313-1313-1313-1313-131313131313', 'IP_SUSPECTE', '66666666-6666-6666-6666-666666666666', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Connexion via reseau Tor signale', 'NOUVELLE', '2026-04-21T09:10:00Z', NULL, NULL, '185.220.101.22'),
  ('14141414-1414-1414-1414-141414141414', 'CNI_INVALIDE', '77777777-7777-7777-7777-777777777777', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Numero CNI absent du registre de verification', 'NOUVELLE', '2026-04-21T09:45:00Z', NULL, NULL, '196.207.3.88'),
  ('15151515-1515-1515-1515-151515151515', 'PATTERN_SUSPECT', '88888888-8888-8888-8888-888888888888', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Pattern de connexion rapide puis vote annule', 'RESOLUE', '2026-04-16T08:20:00Z', '33333333-3333-3333-3333-333333333333', '2026-04-16T09:40:00Z', '41.77.33.201'),
  ('16161616-1616-1616-1616-161616161616', 'VOTE_MULTIPLE', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Alerte historique deja traitee', 'RESOLUE', '2026-04-12T13:00:00Z', '33333333-3333-3333-3333-333333333333', '2026-04-12T14:30:00Z', '41.208.10.5')
ON CONFLICT (id) DO NOTHING;

INSERT INTO suspension (
  id, citoyen_id, motif, operateur_id, superadmin_id, statut, date_creation, date_decision, justification
) VALUES
  ('35353535-3535-3535-3535-353535353535', '66666666-6666-6666-6666-666666666666', 'Recidive de connexions IP suspectes', '33333333-3333-3333-3333-333333333333', NULL, 'EN_ATTENTE', '2026-04-21T10:20:00Z', NULL, ''),
  ('36363636-3636-3636-3636-363636363636', '77777777-7777-7777-7777-777777777777', 'Dossier CNI invalide confirme', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', 'APPROUVEE', '2026-04-15T11:00:00Z', '2026-04-16T15:30:00Z', 'Verification superadmin validee'),
  ('37373737-3737-3737-3737-373737373737', '88888888-8888-8888-8888-888888888888', 'Signalement pattern suspect finalement non confirme', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', 'REJETEE', '2026-04-14T10:00:00Z', '2026-04-14T16:45:00Z', 'Fausse alerte apres controle manuel')
ON CONFLICT (id) DO NOTHING;

-- Action logs for admin creation history timeline
INSERT INTO action_log (
  id, type_action, utilisateur_id, description, horodatage, adresse_ip, signature_cryptographique
) VALUES
  ('57575757-5757-5757-5757-575757575757', 'CREATE_CANDIDATE', '22222222-2222-2222-2222-222222222222', 'Ajout candidat Aminata DIOP - Alliance Republicaine', '2026-02-25T10:00:00Z', '127.0.0.1', 'HMAC-FIXTURE'),
  ('58585858-5858-5858-5858-585858585858', 'CREATE_ELECTION', '22222222-2222-2222-2222-222222222222', 'Programmation election: Legislatives Dakar Demo 2026', '2026-04-01T09:30:00Z', '127.0.0.1', 'HMAC-FIXTURE'),
  ('59595959-5959-5959-5959-595959595959', 'CREATE_CANDIDATE', '22222222-2222-2222-2222-222222222222', 'Ajout candidat Seydou BA - Union Progressiste', '2026-04-01T11:20:00Z', '127.0.0.1', 'HMAC-FIXTURE'),
  ('5a5a5a5a-5a5a-5a5a-5a5a-5a5a5a5a5a5a', 'CREATE_ELECTION', '22222222-2222-2222-2222-222222222222', 'Programmation election: Municipales Saint-Louis Demo 2026', '2026-04-18T08:10:00Z', '41.82.10.11', 'HMAC-FIXTURE'),
  ('5b5b5b5b-5b5b-5b5b-5b5b-5b5b5b5b5b5b', 'CREATE_CANDIDATE', '22222222-2222-2222-2222-222222222222', 'Ajout candidat Aissatou DIALLO - Coalition Citoyenne', '2026-04-18T08:35:00Z', '41.82.10.11', 'HMAC-FIXTURE'),
  ('5c5c5c5c-5c5c-5c5c-5c5c-5c5c5c5c5c5c', 'CREATE_ELECTION', '22222222-2222-2222-2222-222222222222', 'Programmation election: Regionales Thies Demo 2026', '2026-04-20T14:22:00Z', '41.82.10.11', 'HMAC-FIXTURE'),
  ('5d5d5d5d-5d5d-5d5d-5d5d-5d5d5d5d5d5d', 'CREATE_ELECTION', '22222222-2222-2222-2222-222222222222', 'Programmation election: Municipales Ziguinchor Demo 2026', '2026-04-20T14:30:00Z', '41.82.10.11', 'HMAC-FIXTURE'),
  ('5e5e5e5e-5e5e-5e5e-5e5e-5e5e5e5e5e5e', 'CREATE_ELECTION', '22222222-2222-2222-2222-222222222222', 'Programmation election: Legislatives Kaolack Demo 2026', '2026-04-20T14:38:00Z', '41.82.10.11', 'HMAC-FIXTURE')
ON CONFLICT (id) DO NOTHING;
