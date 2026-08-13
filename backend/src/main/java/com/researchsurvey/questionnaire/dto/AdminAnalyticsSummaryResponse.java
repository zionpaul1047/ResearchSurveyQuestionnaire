package com.researchsurvey.questionnaire.dto;

import java.util.List;

public record AdminAnalyticsSummaryResponse(
        Overview overview,
        int minimumGroupSize,
        List<MetricPoint> dailySubmissions,
        List<MetricPoint> ageGroups,
        List<MetricPoint> regions,
        List<MetricPoint> productExperiences,
        List<MetricPoint> fractureExperiences,
        List<FoodMetricPoint> foodDistributions) {

    public record Overview(
            long totalResponses,
            long submittedResponses,
            long draftResponses,
            double submissionRate,
            Long averageCompletionSeconds,
            boolean averageCompletionSuppressed) {}

    public record MetricPoint(String label, Long value, boolean suppressed) {}

    public record FoodMetricPoint(String foodCode, String foodName, String frequency, Long value, boolean suppressed) {}
}
