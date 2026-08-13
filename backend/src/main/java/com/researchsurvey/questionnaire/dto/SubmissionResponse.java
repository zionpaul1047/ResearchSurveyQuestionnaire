package com.researchsurvey.questionnaire.dto;

import java.time.Instant;
import java.util.UUID;

import com.researchsurvey.questionnaire.entity.SubmissionStatus;
import com.researchsurvey.questionnaire.entity.SurveySubmission;

public record SubmissionResponse(
        UUID submissionId,
        String submissionNumber,
        SubmissionStatus status,
        Instant updatedAt,
        Instant submittedAt) {

    public static SubmissionResponse from(SurveySubmission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getSubmissionNumber(),
                submission.getStatus(),
                submission.getUpdatedAt(),
                submission.getSubmittedAt());
    }
}
