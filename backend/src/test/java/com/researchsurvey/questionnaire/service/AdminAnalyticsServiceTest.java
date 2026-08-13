package com.researchsurvey.questionnaire.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository;
import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository.FoodMetricRaw;
import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository.MetricRaw;
import com.researchsurvey.questionnaire.repository.AdminAnalyticsRepository.OverviewRaw;

class AdminAnalyticsServiceTest {
    private AdminAnalyticsRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(AdminAnalyticsRepository.class);
        when(repository.loadDailySubmissions()).thenReturn(List.of());
        when(repository.loadAgeGroups()).thenReturn(List.of());
        when(repository.loadRegions()).thenReturn(List.of());
        when(repository.loadProductExperiences()).thenReturn(List.of());
        when(repository.loadFractureExperiences()).thenReturn(List.of());
        when(repository.loadFoodDistributions()).thenReturn(List.of());
    }

    @Test
    void returnsExactSmallGroupsAndAverageCompletionTime() {
        when(repository.loadOverview()).thenReturn(new OverviewRaw(10, 4, 6, 180L));
        when(repository.loadAgeGroups()).thenReturn(List.of(new MetricRaw("65~69세", 4), new MetricRaw("70~74세", 5)));
        when(repository.loadFoodDistributions()).thenReturn(List.of(
                new FoodMetricRaw("MILK", "우유", "매일", 2),
                new FoodMetricRaw("MILK", "우유", "주 1~2회", 6)));

        var summary = new AdminAnalyticsService(repository).getSummary();

        assertThat(summary.overview().submissionRate()).isEqualTo(40.0);
        assertThat(summary.overview().averageCompletionSeconds()).isEqualTo(180L);
        assertThat(summary.ageGroups().get(0).value()).isEqualTo(4L);
        assertThat(summary.ageGroups().get(1).value()).isEqualTo(5L);
        assertThat(summary.foodDistributions().get(0).value()).isEqualTo(2L);
        assertThat(summary.foodDistributions().get(1).value()).isEqualTo(6L);
    }

    @Test
    void returnsAverageWhenEnoughSubmittedResponsesExist() {
        when(repository.loadOverview()).thenReturn(new OverviewRaw(8, 6, 2, 125L));

        var summary = new AdminAnalyticsService(repository).getSummary();

        assertThat(summary.overview().submissionRate()).isEqualTo(75.0);
        assertThat(summary.overview().averageCompletionSeconds()).isEqualTo(125L);
    }
}
