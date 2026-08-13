package com.researchsurvey.questionnaire.service;

import java.util.List;

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

    public AdminAnalyticsService(AdminAnalyticsRepository repository) {
        this.repository = repository;
    }

    public AdminAnalyticsSummaryResponse getSummary() {
        OverviewRaw raw = repository.loadOverview();
        double submissionRate = raw.total() == 0
                ? 0.0
                : Math.round((raw.submitted() * 1000.0 / raw.total())) / 10.0;

        Overview overview = new Overview(
                raw.total(),
                raw.submitted(),
                raw.draft(),
                submissionRate,
                raw.averageSeconds());

        return new AdminAnalyticsSummaryResponse(
                overview,
                exact(repository.loadDailySubmissions()),
                exact(repository.loadAgeGroups()),
                exact(repository.loadRegions()),
                exact(repository.loadProductExperiences()),
                exact(repository.loadFractureExperiences()),
                exactFood(repository.loadFoodDistributions()));
    }

    private List<MetricPoint> exact(List<MetricRaw> rows) {
        return rows.stream()
                .map(row -> new MetricPoint(row.label(), row.value()))
                .toList();
    }

    private List<FoodMetricPoint> exactFood(List<FoodMetricRaw> rows) {
        return rows.stream()
                .map(row -> new FoodMetricPoint(
                        row.foodCode(),
                        row.foodName(),
                        row.frequency(),
                        row.value()))
                .toList();
    }
}
