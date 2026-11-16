-- NAATALVOTE — préparation d'une démonstration (à exécuter manuellement, JAMAIS en production réelle).
-- Les données de démonstration (migrations V4 à V6) ont des dates fixes de 2026 : elles sont toutes closes.
-- Ce script recale les dates par rapport à maintenant ; l'ordonnanceur ajuste ensuite les statuts (≤ 30 s).
-- Usage : psql "$DATABASE_URL" -f scripts/demo_reset_dates.sql
BEGIN;
UPDATE election SET date_debut = now() - interval '1 hour', date_fin = now() + interval '3 days'
 WHERE titre IN ('Municipales Dakar Nord Demo 2026', 'Législatives Dakar Démo 2026');
UPDATE election SET date_debut = now() + interval '5 days',  date_fin = now() + interval '6 days'  WHERE titre = 'Regionales Thies Demo 2026';
UPDATE election SET date_debut = now() + interval '10 days', date_fin = now() + interval '11 days' WHERE titre = 'Municipales Ziguinchor Demo 2026';
UPDATE election SET date_debut = now() + interval '15 days', date_fin = now() + interval '16 days' WHERE titre = 'Municipales Saint-Louis Démo 2026';
UPDATE election SET date_debut = now() + interval '20 days', date_fin = now() + interval '21 days' WHERE titre = 'Legislatives Kaolack Demo 2026';
-- La présidentielle de démonstration reste dans le passé (élection clôturée avec résultats)
COMMIT;
SELECT titre, date_debut::date, date_fin::date FROM election ORDER BY date_debut;
