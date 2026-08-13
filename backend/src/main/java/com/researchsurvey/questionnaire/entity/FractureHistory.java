package com.researchsurvey.questionnaire.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fracture_history")
public class FractureHistory {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private SurveySubmission submission;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(name = "occurred_year_month", length = 7)
    private String occurredYearMonth;
    @Column(name = "fracture_site", length = 100)
    private String fractureSite;
    @Column(name = "fall_related", length = 20)
    private String fallRelated;
    @Column(name = "primary_cause", length = 200)
    private String primaryCause;
    @Column(name = "record_availability", length = 200)
    private String recordAvailability;
    @Column(length = 100)
    private String treatment;
    @Column(name = "timing_relative_to_product", length = 200)
    private String timingRelativeToProduct;

    protected FractureHistory() {}

    public FractureHistory(int sortOrder, String occurredYearMonth, String fractureSite, String fallRelated, String primaryCause, String recordAvailability, String treatment, String timingRelativeToProduct) {
        this.id = UUID.randomUUID();
        this.sortOrder = sortOrder;
        this.occurredYearMonth = occurredYearMonth;
        this.fractureSite = fractureSite;
        this.fallRelated = fallRelated;
        this.primaryCause = primaryCause;
        this.recordAvailability = recordAvailability;
        this.treatment = treatment;
        this.timingRelativeToProduct = timingRelativeToProduct;
    }

    public void attachTo(SurveySubmission value) { submission = value; }
}
