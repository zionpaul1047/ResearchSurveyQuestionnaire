package com.researchsurvey.questionnaire.dto;

import java.util.List;

public record AdminAnalyticsSummaryResponse(
        Overview overview,
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
            Long averageCompletionSeconds) {}

    public record MetricPoint(String label, long value) {}

    public record FoodMetricPoint(String foodCode, String foodName, String frequency, long value) {}
}
