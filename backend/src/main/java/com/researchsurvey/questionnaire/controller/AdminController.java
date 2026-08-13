package com.researchsurvey.questionnaire.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.researchsurvey.questionnaire.dto.AdminAnalyticsSummaryResponse;
import com.researchsurvey.questionnaire.dto.AdminMeResponse;
import com.researchsurvey.questionnaire.service.AdminAnalyticsService;
import com.researchsurvey.questionnaire.service.AdminAuditService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminAnalyticsService analyticsService;
    private final AdminAuditService auditService;

    public AdminController(AdminAnalyticsService analyticsService, AdminAuditService auditService) {
        this.analyticsService = analyticsService;
        this.auditService = auditService;
    }

    @GetMapping("/auth/me")
    public AdminMeResponse me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();
        auditService.record(authentication.getName(), "AUTHENTICATE", "ADMIN_PORTAL", "successful authentication");
        return new AdminMeResponse(authentication.getName(), roles);
    }

    @GetMapping("/analytics/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public AdminAnalyticsSummaryResponse summary(Authentication authentication) {
        AdminAnalyticsSummaryResponse response = analyticsService.getSummary();
        auditService.record(authentication.getName(), "VIEW_ANALYTICS", "ANALYTICS_SUMMARY", "aggregate-only response");
        return response;
    }
}
