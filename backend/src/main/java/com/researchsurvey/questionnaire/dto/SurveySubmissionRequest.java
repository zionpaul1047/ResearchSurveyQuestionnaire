package com.researchsurvey.questionnaire.dto;

import java.util.List;

public record SurveySubmissionRequest(
        String surveyVersion,
        boolean privacyConsent,
        boolean contactConsent,
        SurveyMetadataRequest metadata,
        EligibilityRequest eligibility,
        BasicInfoRequest basicInfo,
        String productExperience,
        ProductDetailsRequest productDetails,
        List<ProductRequest> products,
        String fractureExperience,
        String fractureTotalCount,
        boolean fractureCountUnknown,
        List<FractureRequest> fractures,
        String mealsPerDay,
        List<FoodAnswerRequest> foodAnswers) {

    public record SurveyMetadataRequest(
            String participantCode,
            String surveyMethod,
            String surveyMethodOther,
            String surveyDate,
            String contactPhone,
            String contactTime) {}

    public record EligibilityRequest(
            String voluntaryConsent,
            String birthDate,
            String over55,
            String femaleAtBirth,
            String exclusionDisease) {}

    public record BasicInfoRequest(
            String region,
            String regionOther,
            String education,
            String educationOther,
            String householdIncome,
            String householdIncomeOther,
            String employed,
            String employmentOther,
            String heightCm,
            String weightKg,
            String menopauseAge,
            boolean menopauseAgeUnknown,
            String hormoneTreatment,
            String hormoneDurationYears,
            String hormoneDurationMonths,
            boolean hormoneDurationUnknown) {}

    public record ProductDetailsRequest(
            String currentStatus,
            String startRecall,
            String startYearMonth,
            String startYear,
            List<String> startReasons,
            String startReasonOther,
            List<String> priorityReasons,
            String interruptionStatus,
            String interruptionDetails,
            String evidencePeriodType,
            String evidenceStartYearMonth,
            String evidenceEndYearMonth) {}

    public record ProductRequest(
            String clientId,
            String productName,
            List<String> ingredients,
            String totalIntakeMonths,
            String averageFrequency,
            String currentlyTaking) {}

    public record FractureRequest(
            String clientId,
            String occurredYearMonth,
            String fractureSite,
            String fallRelated,
            String primaryCause,
            String recordAvailability,
            String treatment,
            String timingRelativeToProduct) {}

    public record FoodAnswerRequest(
            String foodCode,
            String foodName,
            String frequency,
            String amount) {}
}
