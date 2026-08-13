package com.researchsurvey.questionnaire.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminAnalyticsRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdminAnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OverviewRaw loadOverview() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE status = 'SUBMITTED') AS submitted,
                       COUNT(*) FILTER (WHERE status = 'DRAFT') AS draft,
                       AVG(EXTRACT(EPOCH FROM (submitted_at - created_at))) FILTER (WHERE status = 'SUBMITTED') AS average_seconds
                FROM survey_submission
                """);
        return new OverviewRaw(number(row.get("total")), number(row.get("submitted")), number(row.get("draft")), nullableNumber(row.get("average_seconds")));
    }

    public List<MetricRaw> loadDailySubmissions() {
        return metrics("""
                WITH days AS (
                    SELECT GENERATE_SERIES(
                        (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date - 29,
                        (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date,
                        INTERVAL '1 day'
                    )::date AS day
                )
                SELECT TO_CHAR(days.day, 'YYYY-MM-DD') AS label, COUNT(submission.id) AS value
                FROM days
                LEFT JOIN survey_submission submission
                    ON submission.status = 'SUBMITTED'
                   AND (submission.submitted_at AT TIME ZONE 'Asia/Seoul')::date = days.day
                GROUP BY days.day
                ORDER BY days.day
                """);
    }

    public List<MetricRaw> loadAgeGroups() {
        return metrics("""
                WITH ages AS (
                    SELECT EXTRACT(YEAR FROM AGE(COALESCE(survey_date, submitted_at::date), birth_date))::int AS age
                    FROM survey_submission
                    WHERE status = 'SUBMITTED' AND birth_date IS NOT NULL
                )
                SELECT CASE
                           WHEN age < 55 THEN '55세 미만'
                           WHEN age < 60 THEN '55~59세'
                           WHEN age < 65 THEN '60~64세'
                           WHEN age < 70 THEN '65~69세'
                           WHEN age < 75 THEN '70~74세'
                           ELSE '75세 이상'
                       END AS label,
                       COUNT(*) AS value
                FROM ages
                GROUP BY 1
                ORDER BY MIN(age)
                """);
    }

    public List<MetricRaw> loadRegions() {
        return metrics("""
                SELECT COALESCE(NULLIF(region, ''), '미응답') AS label, COUNT(*) AS value
                FROM survey_submission
                WHERE status = 'SUBMITTED'
                GROUP BY 1 ORDER BY value DESC, label
                """);
    }

    public List<MetricRaw> loadProductExperiences() {
        return metrics("""
                SELECT CASE product_experience
                           WHEN 'REGULAR' THEN '일반 제품 섭취'
                           WHEN 'SAMPLE_ONLY' THEN '샘플만 섭취'
                           WHEN 'NEVER' THEN '섭취 경험 없음'
                           WHEN 'UNKNOWN' THEN '잘 모르겠음'
                           ELSE '미응답'
                       END AS label,
                       COUNT(*) AS value
                FROM survey_submission
                WHERE status = 'SUBMITTED'
                GROUP BY 1 ORDER BY value DESC, label
                """);
    }

    public List<MetricRaw> loadFractureExperiences() {
        return metrics("""
                SELECT CASE fracture_experience
                           WHEN 'YES' THEN '골절 경험 있음'
                           WHEN 'NO' THEN '골절 경험 없음'
                           WHEN 'UNKNOWN' THEN '잘 모르겠음'
                           ELSE '미응답'
                       END AS label,
                       COUNT(*) AS value
                FROM survey_submission
                WHERE status = 'SUBMITTED'
                GROUP BY 1 ORDER BY value DESC, label
                """);
    }

    public List<FoodMetricRaw> loadFoodDistributions() {
        return jdbcTemplate.query("""
                SELECT answer.food_code, answer.food_name, answer.frequency, COUNT(*) AS value
                FROM food_answer answer
                JOIN survey_submission submission ON submission.id = answer.submission_id
                WHERE submission.status = 'SUBMITTED'
                GROUP BY answer.food_code, answer.food_name, answer.frequency
                ORDER BY answer.food_code, value DESC, answer.frequency
                """, (resultSet, rowNumber) -> new FoodMetricRaw(resultSet.getString("food_code"), resultSet.getString("food_name"), resultSet.getString("frequency"), resultSet.getLong("value")));
    }

    private List<MetricRaw> metrics(String sql) {
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new MetricRaw(resultSet.getString("label"), resultSet.getLong("value")));
    }

    private long number(Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }

    private Long nullableNumber(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    public record OverviewRaw(long total, long submitted, long draft, Long averageSeconds) {}
    public record MetricRaw(String label, long value) {}
    public record FoodMetricRaw(String foodCode, String foodName, String frequency, long value) {}
}
