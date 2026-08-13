ALTER TABLE survey_submission
    ADD COLUMN participant_code VARCHAR(100),
    ADD COLUMN survey_method VARCHAR(20),
    ADD COLUMN survey_method_other VARCHAR(100),
    ADD COLUMN survey_date DATE,
    ADD COLUMN contact_phone VARCHAR(30),
    ADD COLUMN contact_time VARCHAR(100),
    ADD COLUMN region_other VARCHAR(100),
    ADD COLUMN education_other VARCHAR(100),
    ADD COLUMN household_income_other VARCHAR(100),
    ADD COLUMN employment_other VARCHAR(100),
    ADD COLUMN menopause_age_unknown BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN hormone_duration_years INTEGER,
    ADD COLUMN hormone_duration_months INTEGER,
    ADD COLUMN hormone_duration_unknown BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN product_current_status VARCHAR(30),
    ADD COLUMN product_start_recall VARCHAR(30),
    ADD COLUMN product_start_year_month VARCHAR(7),
    ADD COLUMN product_start_year INTEGER,
    ADD COLUMN product_start_reasons VARCHAR(100),
    ADD COLUMN product_start_reason_other VARCHAR(500),
    ADD COLUMN product_priority_reasons VARCHAR(100),
    ADD COLUMN product_interruption_status VARCHAR(20),
    ADD COLUMN product_interruption_details VARCHAR(2000),
    ADD COLUMN evidence_period_type VARCHAR(30),
    ADD COLUMN evidence_start_year_month VARCHAR(7),
    ADD COLUMN evidence_end_year_month VARCHAR(7),
    ADD COLUMN fracture_total_count INTEGER,
    ADD COLUMN fracture_count_unknown BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE attachment
    DROP CONSTRAINT IF EXISTS attachment_category_check;

UPDATE attachment
SET category = 'PRODUCT_PHOTO'
WHERE category = 'PRODUCT_EVIDENCE';

ALTER TABLE attachment
    ADD CONSTRAINT attachment_category_check
    CHECK (category IN ('PURCHASE_EVIDENCE', 'PRODUCT_PHOTO'));
