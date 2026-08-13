package com.researchsurvey.questionnaire.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attachment")
public class Attachment {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private SurveySubmission submission;
    @Column(nullable = false, length = 50)
    private String category;
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;
    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    @Column(name = "file_size", nullable = false)
    private long fileSize;
    @Column(name = "sha256_hash", nullable = false, length = 64)
    private String sha256Hash;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Attachment() {}

    public Attachment(SurveySubmission submission, String category, String originalFileName, String storedFileName, String storagePath, String contentType, long fileSize, String sha256Hash, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.submission = submission;
        this.category = category;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.sha256Hash = sha256Hash;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getOriginalFileName() { return originalFileName; }
    public long getFileSize() { return fileSize; }
}
