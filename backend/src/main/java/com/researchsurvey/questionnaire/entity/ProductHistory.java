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
@Table(name = "product_history")
public class ProductHistory {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private SurveySubmission submission;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;
    @Column(length = 500)
    private String ingredients;
    @Column(name = "total_intake_months")
    private Integer totalIntakeMonths;
    @Column(name = "average_frequency", length = 100)
    private String averageFrequency;
    @Column(name = "currently_taking", length = 20)
    private String currentlyTaking;

    protected ProductHistory() {}

    public ProductHistory(int sortOrder, String productName, String ingredients, Integer totalIntakeMonths, String averageFrequency, String currentlyTaking) {
        this.id = UUID.randomUUID();
        this.sortOrder = sortOrder;
        this.productName = productName;
        this.ingredients = ingredients;
        this.totalIntakeMonths = totalIntakeMonths;
        this.averageFrequency = averageFrequency;
        this.currentlyTaking = currentlyTaking;
    }

    public void attachTo(SurveySubmission value) { submission = value; }
}
