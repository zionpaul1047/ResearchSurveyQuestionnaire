package com.researchsurvey.questionnaire.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.researchsurvey.questionnaire.entity.AdminAuditLog;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {
}
