package com.researchsurvey.questionnaire.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.researchsurvey.questionnaire.dto.AttachmentResponse;
import com.researchsurvey.questionnaire.dto.SubmissionResponse;
import com.researchsurvey.questionnaire.dto.SurveySubmissionRequest;
import com.researchsurvey.questionnaire.service.AttachmentService;
import com.researchsurvey.questionnaire.service.SurveySubmissionService;

@RestController
@RequestMapping("/api/v1/submissions")
public class SurveySubmissionController {
    private final SurveySubmissionService submissionService;
    private final AttachmentService attachmentService;

    public SurveySubmissionController(SurveySubmissionService submissionService, AttachmentService attachmentService) {
        this.submissionService = submissionService;
        this.attachmentService = attachmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse createDraft(@RequestBody SurveySubmissionRequest request) {
        return submissionService.createDraft(request);
    }

    @PutMapping("/{submissionId}")
    public SubmissionResponse updateDraft(@PathVariable UUID submissionId, @RequestBody SurveySubmissionRequest request) {
        return submissionService.updateDraft(submissionId, request);
    }

    @GetMapping("/{submissionId}/status")
    public SubmissionResponse getStatus(@PathVariable UUID submissionId) {
        return submissionService.getStatus(submissionId);
    }

    @PostMapping("/{submissionId}/submit")
    public SubmissionResponse submit(@PathVariable UUID submissionId, @RequestBody SurveySubmissionRequest request) {
        return submissionService.submit(submissionId, request);
    }

    @PostMapping("/{submissionId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse uploadAttachment(
            @PathVariable UUID submissionId,
            @RequestParam(defaultValue = "PRODUCT_PHOTO") String category,
            @RequestParam MultipartFile file) {
        return attachmentService.upload(submissionId, category, file);
    }
}
