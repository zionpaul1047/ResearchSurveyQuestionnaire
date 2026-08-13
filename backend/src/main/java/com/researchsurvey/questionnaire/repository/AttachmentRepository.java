package com.researchsurvey.questionnaire.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.researchsurvey.questionnaire.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    long countBySubmission_IdAndCategory(UUID submissionId, String category);
}
