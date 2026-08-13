package com.researchsurvey.questionnaire.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.researchsurvey.questionnaire.entity.AdminAuditLog;
import com.researchsurvey.questionnaire.repository.AdminAuditLogRepository;

@Service
public class AdminAuditService {
    private final AdminAuditLogRepository repository;

    public AdminAuditService(AdminAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String username, String action, String resource, String detail) {
        repository.save(new AdminAuditLog(limit(username, 100), limit(action, 50), limit(resource, 100), limit(detail, 500), Instant.now()));
    }

    private String limit(String value, int maxLength) {
        if (value == null) return null;
        String normalized = value.replaceAll("[\\r\\n\\t]", " ").trim();
        return normalized.substring(0, Math.min(normalized.length(), maxLength));
    }
}
