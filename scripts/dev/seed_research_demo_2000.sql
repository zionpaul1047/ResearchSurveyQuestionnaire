-- Local/PoC only: deterministic synthetic research responses for chart verification.
-- Safe to run repeatedly. DEMO- submission numbers and deterministic UUIDs prevent duplicates.

WITH generated AS (
    SELECT
        n,
        LEAST(29, FLOOR(30 * POWER((n - 1) / 2000.0, 1.35)))::int AS day_offset,
        CASE WHEN n % 20 = 0 THEN 'DRAFT' ELSE 'SUBMITTED' END AS status
    FROM GENERATE_SERIES(1, 2000) AS series(n)
), prepared AS (
    SELECT
        n,
        day_offset,
        status,
        CURRENT_TIMESTAMP - (day_offset || ' days')::interval - ((n % 24) || ' hours')::interval AS created_at,
        CASE
            WHEN status = 'SUBMITTED' THEN CURRENT_TIMESTAMP - (day_offset || ' days')::interval - ((n % 24) || ' hours')::interval + ((8 + n % 35) || ' minutes')::interval
            ELSE NULL
        END AS submitted_at
    FROM generated
)
INSERT INTO survey_submission (
    id, submission_number, status, survey_version,
    privacy_consent, contact_consent, voluntary_consent,
    participant_code, survey_method, survey_date,
    birth_date, over_55, female_at_birth, exclusion_disease,
    region, education, household_income, employed,
    height_cm, weight_kg, menopause_age, menopause_age_unknown,
    hormone_treatment, hormone_duration_years, hormone_duration_months, hormone_duration_unknown,
    product_experience, fracture_experience, meals_per_day,
    created_at, updated_at, submitted_at, version
)
SELECT
    MD5('rsq-demo-submission-' || n::text)::uuid,
    'DEMO-2026-' || LPAD(n::text, 4, '0'),
    status,
    '2.4-DEMO',
    TRUE,
    TRUE,
    'YES',
    'DEMO-P-' || LPAD(n::text, 4, '0'),
    CASE WHEN n % 5 = 0 THEN 'PHONE' ELSE 'ONLINE' END,
    CURRENT_DATE - day_offset,
    (CURRENT_DATE - ((55 + n % 26) || ' years')::interval - ((n % 330) || ' days')::interval)::date,
    'YES',
    CASE WHEN n % 20 = 0 THEN 'DECLINE' ELSE 'YES' END,
    'NONE',
    (ARRAY['서울', '부산', '대구', '인천', '광주', '대전', '울산', '기타'])[1 + MOD(n * 7, 8)],
    (ARRAY['고등학교 졸업', '전문대 졸업', '대학교 졸업', '대학원 이상'])[1 + MOD(n * 3, 4)],
    (ARRAY['200만원 미만', '200~399만원', '400~599만원', '600만원 이상'])[1 + MOD(n * 5, 4)],
    CASE WHEN n % 3 = 0 THEN 'NO' ELSE 'YES' END,
    148 + MOD(n * 7, 25),
    45 + MOD(n * 11, 40),
    44 + MOD(n, 14),
    FALSE,
    CASE WHEN n % 4 = 0 THEN '과거 사용' ELSE '사용 안 함' END,
    CASE WHEN n % 4 = 0 THEN MOD(n, 8) ELSE NULL END,
    CASE WHEN n % 4 = 0 THEN MOD(n, 12) ELSE NULL END,
    FALSE,
    CASE
        WHEN n % 10 < 6 THEN 'REGULAR'
        WHEN n % 10 < 8 THEN 'SAMPLE_ONLY'
        WHEN n % 10 = 8 THEN 'NEVER'
        ELSE 'UNKNOWN'
    END,
    CASE WHEN n % 10 < 3 THEN 'YES' WHEN n % 10 < 9 THEN 'NO' ELSE 'UNKNOWN' END,
    (ARRAY['2', '3', '4'])[1 + MOD(n, 3)],
    created_at,
    COALESCE(submitted_at, created_at),
    submitted_at,
    0
FROM prepared
ON CONFLICT (submission_number) DO NOTHING;

WITH foods(sort_order, food_code, food_name) AS (
    VALUES
        (1, 'RICE', '쌀밥'),
        (2, 'MIXED_RICE', '잡곡밥(현미밥, 보리밥 등)'),
        (3, 'BEAN_RICE', '콩밥')
), demo_submissions AS (
    SELECT n, MD5('rsq-demo-submission-' || n::text)::uuid AS submission_id
    FROM GENERATE_SERIES(1, 2000) AS series(n)
    WHERE n % 20 <> 0
)
INSERT INTO food_answer (id, submission_id, food_code, food_name, frequency, amount)
SELECT
    MD5('rsq-demo-food-' || demo.n::text || '-' || food.sort_order::text)::uuid,
    demo.submission_id,
    food.food_code,
    food.food_name,
    (ARRAY['거의 안 먹음', '월 1회', '월 2~3회', '주 1회', '주 2~4회', '주 5~6회', '일 1회', '일 2회', '일 3회'])[1 + MOD(demo.n + food.sort_order * 3, 9)],
    CASE WHEN MOD(demo.n + food.sort_order * 3, 9) = 0 THEN NULL ELSE (ARRAY['0.5', '1', '1.5'])[1 + MOD(demo.n + food.sort_order, 3)] END
FROM demo_submissions demo
CROSS JOIN foods food
JOIN survey_submission submission ON submission.id = demo.submission_id
ON CONFLICT (id) DO NOTHING;

SELECT
    COUNT(*) FILTER (WHERE submission_number LIKE 'DEMO-2026-%') AS demo_total,
    COUNT(*) FILTER (WHERE submission_number LIKE 'DEMO-2026-%' AND status = 'SUBMITTED') AS demo_submitted,
    COUNT(*) FILTER (WHERE submission_number LIKE 'DEMO-2026-%' AND status = 'DRAFT') AS demo_draft
FROM survey_submission;
