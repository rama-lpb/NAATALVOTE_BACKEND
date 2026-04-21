-- Ensure citizen-facing live elections exist and are votable for demos.

-- Keep this election open around 2026-04-21 for vote tests.
UPDATE election
SET date_debut = '2026-04-20T08:00:00Z',
    date_fin = '2026-04-30T18:00:00Z',
    statut = 'EN_COURS'
WHERE id = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';

-- Additional live election with candidates for citizen scenarios.
INSERT INTO election (
  id, titre, description, type_election, date_debut, date_fin, statut, admin_id, region, total_electeurs, votes_count
) VALUES
  ('99999999-9999-9999-9999-999999999999', 'Municipales Dakar Nord Demo 2026', 'Scrutin en cours pour tests citoyens en video', 'MUNICIPALE', '2026-04-21T07:00:00Z', '2026-04-29T18:00:00Z', 'EN_COURS', '22222222-2222-2222-2222-222222222222', 'Dakar Nord', 73000, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO candidat (
  id, election_id, nom, prenom, parti_politique, biographie, photo_url, programme_url, votes_count, color
) VALUES
  ('91919191-9191-9191-9191-919191919191', '99999999-9999-9999-9999-999999999999', 'NDAO', 'Awa', 'Mouvement Citoyen', 'Candidate 13', '', '', 0, '#1abc9c'),
  ('92929292-9292-9292-9292-929292929292', '99999999-9999-9999-9999-999999999999', 'NIANG', 'Omar', 'Union Progressiste', 'Candidate 14', '', '', 0, '#e67e22'),
  ('93939393-9393-9393-9393-939393939393', '99999999-9999-9999-9999-999999999999', 'FAYE', 'Khadija', 'Alternative Verte', 'Candidate 15', '', '', 0, '#8e44ad')
ON CONFLICT (id) DO NOTHING;
