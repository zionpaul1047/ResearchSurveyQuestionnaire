package com.researchsurvey.questionnaire.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.researchsurvey.questionnaire.dto.AdminAnalyticsSummaryResponse;
import com.researchsurvey.questionnaire.dto.AdminAnalyticsSummaryResponse.FoodMetricPoint;
import com.researchsurvey.questionnaire.dto.AdminAnalyticsSummaryResponse.MetricPoint;
import com.researchsurvey.questionnaire.dto.AdminAnalyticsSummaryResponse.Overview;
import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository;
import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository.FoodMetricRaw;
import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository.MetricRaw;
import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository.OverviewRaw;

@Service
public class AdminAnalyticsService {
    private final AdminAnalyticsRepository repository;
    private final int minimumGroupSize;

    public AdminAnalyticsService(
            AdminAnalyticsRepository repository,
            @Value("${survey.analytics.minimum-group-size}") int minimumGroupSize) {
        if (minimumGroupSize < 1) {
            throw new IllegalArgumentException("통계 최소 집단 크기는 1 이상이어야 합니다.");
        }
        this.repository = repository;
        this.minimumGroupSize = minimumGroupSize;
    }

    public AdminAnalyticsSummaryResponse getSummary() {
        OverviewRaw raw = repository.loadOverview();
        boolean suppressAverage = raw.submitted() < minimumGroupSize || raw.averageSeconds() == null;
        double submissionRate = raw.total() == 0
                ? 0.0
                : Math.round((raw.submitted() * 1000.0 / raw.total())) / 10.0;

        Overview overview = new Overview(
                raw.total(),
                raw.submitted(),
                raw.draft(),
                submissionRate,
                suppressAverage ? null : raw.averageSeconds(),
                suppressAverage);

        return new AdminAnalyticsSummaryResponse(
                overview,
                minimumGroupSize,
                suppress(repository.loadDailySubmissions()),
                suppress(repository.loadAgeGroups()),
                suppress(repository.loadRegions()),
                suppress(repository.loadProductExperiences()),
                suppress(repository.loadFractureExperiences()),
                suppressFood(repository.loadFoodDistributions()));
    }

    private List<MetricPoint> suppress(List<MetricRaw> rows) {
        return rows.stream()
                .map(row -> new MetricPoint(
                        row.label(),
                        row.value() < minimumGroupSize ? null : row.value(),
                        row.value() < minimumGroupSize))
                .toList();
    }

    private List<FoodMetricPoint> suppressFood(List<FoodMetricRaw> rows) {
        return rows.stream()
                .map(row -> new FoodMetricPoint(
                        row.foodCode(),
                        row.foodName(),
                        row.frequency(),
                        row.value() < minimumGroupSize ? null : row.value(),
                        row.value() < minimumGroupSize))
                .toList();
    }
}
