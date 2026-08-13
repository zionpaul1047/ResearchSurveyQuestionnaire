package com.researchsurvey.questionnaire.dto;

import java.util.UUID;

import com.researchsurvey.questionnaire.entity.Attachment;

public record AttachmentResponse(UUID attachmentId, String originalFileName, long fileSize) {
    public static AttachmentResponse from(Attachment attachment) {
        return new AttachmentResponse(attachment.getId(), attachment.getOriginalFileName(), attachment.getFileSize());
    }
}
