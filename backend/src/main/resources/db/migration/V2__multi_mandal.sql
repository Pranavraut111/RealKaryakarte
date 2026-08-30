-- =============================================================================
-- Migration V2: Multi-Mandal Support
-- =============================================================================

-- 1. Rename mandal_settings to mandals
ALTER TABLE mandal_settings RENAME TO mandals;

-- 2. Add invite_code to mandals
ALTER TABLE mandals ADD COLUMN invite_code VARCHAR(50) UNIQUE;

-- 3. Add mandal_id to tables
ALTER TABLE users ADD COLUMN mandal_id BIGINT REFERENCES mandals(id);
ALTER TABLE contributions ADD COLUMN mandal_id BIGINT REFERENCES mandals(id);
ALTER TABLE expenses ADD COLUMN mandal_id BIGINT REFERENCES mandals(id);
ALTER TABLE notices ADD COLUMN mandal_id BIGINT REFERENCES mandals(id);

-- 4. Set existing records to the default mandal (id = 1)
UPDATE users SET mandal_id = 1 WHERE mandal_id IS NULL;
UPDATE contributions SET mandal_id = 1 WHERE mandal_id IS NULL;
UPDATE expenses SET mandal_id = 1 WHERE mandal_id IS NULL;
UPDATE notices SET mandal_id = 1 WHERE mandal_id IS NULL;
UPDATE mandals SET invite_code = 'MANDAL-DEFAULT' WHERE id = 1;

-- 5. Add NOT NULL constraints now that data is populated
ALTER TABLE contributions ALTER COLUMN mandal_id SET NOT NULL;
ALTER TABLE expenses ALTER COLUMN mandal_id SET NOT NULL;
ALTER TABLE notices ALTER COLUMN mandal_id SET NOT NULL;
-- Note: users.mandal_id is left nullable for users who just signed up and haven't joined a mandal yet.

-- 6. Add Indexes for performance
CREATE INDEX idx_users_mandal ON users(mandal_id);
CREATE INDEX idx_contributions_mandal ON contributions(mandal_id);
CREATE INDEX idx_expenses_mandal ON expenses(mandal_id);
CREATE INDEX idx_notices_mandal ON notices(mandal_id);
