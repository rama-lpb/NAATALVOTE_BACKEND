-- Add missing user fields (kept optional/blank for existing rows)
ALTER TABLE utilisateur
  ADD COLUMN IF NOT EXISTS date_naissance VARCHAR(30) NOT NULL DEFAULT '';

ALTER TABLE utilisateur
  ADD COLUMN IF NOT EXISTS adresse TEXT NOT NULL DEFAULT '';

