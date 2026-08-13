CREATE INDEX idx_submission_analytics_submitted_at
    ON survey_submission(submitted_at DESC)
    WHERE status = 'SUBMITTED';
