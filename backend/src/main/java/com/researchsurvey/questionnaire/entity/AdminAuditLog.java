package com.researchsurvey.questionnaire.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 100)
    private String resource;

    @Column(length = 500)
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AdminAuditLog() {}

    public AdminAuditLog(String username, String action, String resource, String detail, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.action = action;
        this.resource = resource;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }
}
