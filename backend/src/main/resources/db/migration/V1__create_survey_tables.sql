CREATE TABLE survey_submission (
    id UUID PRIMARY KEY,
    submission_number VARCHAR(40) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'SUBMITTED')),
    survey_version VARCHAR(20) NOT NULL,
    privacy_consent BOOLEAN NOT NULL DEFAULT FALSE,
    contact_consent BOOLEAN NOT NULL DEFAULT FALSE,
    voluntary_consent VARCHAR(20),
    birth_date DATE,
    over_55 VARCHAR(20),
    female_at_birth VARCHAR(20),
    exclusion_disease VARCHAR(30),
    region VARCHAR(50),
    education VARCHAR(100),
    household_income VARCHAR(100),
    employed VARCHAR(20),
    height_cm NUMERIC(5, 1),
    weight_kg NUMERIC(5, 1),
    menopause_age INTEGER,
    hormone_treatment VARCHAR(100),
    product_experience VARCHAR(30),
    fracture_experience VARCHAR(20),
    meals_per_day VARCHAR(30),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    submitted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE product_history (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES survey_submission(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    ingredients VARCHAR(500),
    total_intake_months INTEGER,
    average_frequency VARCHAR(100),
    currently_taking VARCHAR(20)
);

CREATE TABLE fracture_history (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES survey_submission(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL,
    occurred_year_month VARCHAR(7),
    fracture_site VARCHAR(100),
    fall_related VARCHAR(20),
    primary_cause VARCHAR(200),
    record_availability VARCHAR(200),
    treatment VARCHAR(100),
    timing_relative_to_product VARCHAR(200)
);

CREATE TABLE food_answer (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES survey_submission(id) ON DELETE CASCADE,
    food_code VARCHAR(50) NOT NULL,
    food_name VARCHAR(150) NOT NULL,
    frequency VARCHAR(50),
    amount VARCHAR(50)
);

CREATE TABLE attachment (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES survey_submission(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_submission_status_updated ON survey_submission(status, updated_at DESC);
CREATE INDEX idx_product_submission ON product_history(submission_id);
CREATE INDEX idx_fracture_submission ON fracture_history(submission_id);
CREATE INDEX idx_food_submission ON food_answer(submission_id);
CREATE INDEX idx_attachment_submission ON attachment(submission_id);
