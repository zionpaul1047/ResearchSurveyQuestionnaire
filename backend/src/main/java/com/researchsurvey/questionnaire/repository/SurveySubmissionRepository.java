package com.researchsurvey.questionnaire.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.researchsurvey.questionnaire.entity.SurveySubmission;

public interface SurveySubmissionRepository extends JpaRepository<SurveySubmission, UUID> {
}
