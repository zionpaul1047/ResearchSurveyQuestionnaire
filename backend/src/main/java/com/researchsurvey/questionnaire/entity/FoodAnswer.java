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
@Table(name = "food_answer")
public class FoodAnswer {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private SurveySubmission submission;
    @Column(name = "food_code", nullable = false, length = 50)
    private String foodCode;
    @Column(name = "food_name", nullable = false, length = 150)
    private String foodName;
    @Column(length = 50)
    private String frequency;
    @Column(length = 50)
    private String amount;

    protected FoodAnswer() {}

    public FoodAnswer(String foodCode, String foodName, String frequency, String amount) {
        this.id = UUID.randomUUID();
        this.foodCode = foodCode;
        this.foodName = foodName;
        this.frequency = frequency;
        this.amount = amount;
    }

    public void attachTo(SurveySubmission value) { submission = value; }
}
