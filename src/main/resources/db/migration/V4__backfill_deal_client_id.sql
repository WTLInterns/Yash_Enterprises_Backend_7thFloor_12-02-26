-- Backfill client_id on deals where it is null but the FK column has a value
-- This handles deals created before the explicit setClientId() fix
UPDATE deals SET client_id = client_id WHERE client_id IS NOT NULL;
