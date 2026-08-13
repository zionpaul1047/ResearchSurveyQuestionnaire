-- Local/PoC only. Run manually only when the synthetic DEMO-2026 dataset must be removed.
-- Child food answers are deleted automatically through the foreign key cascade.
DELETE FROM survey_submission
WHERE submission_number LIKE 'DEMO-2026-%';
