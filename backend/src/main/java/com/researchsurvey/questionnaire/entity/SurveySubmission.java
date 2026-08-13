package com.researchsurvey.questionnaire.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "survey_submission")
public class SurveySubmission {
    @Id
    private UUID id;

    @Column(name = "submission_number", nullable = false, unique = true, length = 40)
    private String submissionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status;

    @Column(name = "survey_version", nullable = false, length = 20)
    private String surveyVersion;

    @Column(name = "privacy_consent", nullable = false)
    private boolean privacyConsent;

    @Column(name = "contact_consent", nullable = false)
    private boolean contactConsent;

    @Column(name = "participant_code", length = 100)
    private String participantCode;

    @Column(name = "survey_method", length = 20)
    private String surveyMethod;

    @Column(name = "survey_method_other", length = 100)
    private String surveyMethodOther;

    @Column(name = "survey_date")
    private LocalDate surveyDate;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "contact_time", length = 100)
    private String contactTime;

    @Column(name = "voluntary_consent", length = 20)
    private String voluntaryConsent;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "over_55", length = 20)
    private String over55;

    @Column(name = "female_at_birth", length = 20)
    private String femaleAtBirth;

    @Column(name = "exclusion_disease", length = 30)
    private String exclusionDisease;

    @Column(length = 50)
    private String region;

    @Column(name = "region_other", length = 100)
    private String regionOther;

    @Column(length = 100)
    private String education;

    @Column(name = "education_other", length = 100)
    private String educationOther;

    @Column(name = "household_income", length = 100)
    private String householdIncome;

    @Column(name = "household_income_other", length = 100)
    private String householdIncomeOther;

    @Column(length = 20)
    private String employed;

    @Column(name = "employment_other", length = 100)
    private String employmentOther;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "menopause_age")
    private Integer menopauseAge;

    @Column(name = "menopause_age_unknown", nullable = false)
    private boolean menopauseAgeUnknown;

    @Column(name = "hormone_treatment", length = 100)
    private String hormoneTreatment;

    @Column(name = "hormone_duration_years")
    private Integer hormoneDurationYears;

    @Column(name = "hormone_duration_months")
    private Integer hormoneDurationMonths;

    @Column(name = "hormone_duration_unknown", nullable = false)
    private boolean hormoneDurationUnknown;

    @Column(name = "product_experience", length = 30)
    private String productExperience;

    @Column(name = "product_current_status", length = 30)
    private String productCurrentStatus;

    @Column(name = "product_start_recall", length = 30)
    private String productStartRecall;

    @Column(name = "product_start_year_month", length = 7)
    private String productStartYearMonth;

    @Column(name = "product_start_year")
    private Integer productStartYear;

    @Column(name = "product_start_reasons", length = 100)
    private String productStartReasons;

    @Column(name = "product_start_reason_other", length = 500)
    private String productStartReasonOther;

    @Column(name = "product_priority_reasons", length = 100)
    private String productPriorityReasons;

    @Column(name = "product_interruption_status", length = 20)
    private String productInterruptionStatus;

    @Column(name = "product_interruption_details", length = 2000)
    private String productInterruptionDetails;

    @Column(name = "evidence_period_type", length = 30)
    private String evidencePeriodType;

    @Column(name = "evidence_start_year_month", length = 7)
    private String evidenceStartYearMonth;

    @Column(name = "evidence_end_year_month", length = 7)
    private String evidenceEndYearMonth;

    @Column(name = "fracture_experience", length = 20)
    private String fractureExperience;

    @Column(name = "fracture_total_count")
    private Integer fractureTotalCount;

    @Column(name = "fracture_count_unknown", nullable = false)
    private boolean fractureCountUnknown;

    @Column(name = "meals_per_day", length = 30)
    private String mealsPerDay;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductHistory> products = new ArrayList<>();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FractureHistory> fractures = new ArrayList<>();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FoodAnswer> foodAnswers = new ArrayList<>();

    protected SurveySubmission() {}

    public SurveySubmission(UUID id, String submissionNumber, Instant now) {
        this.id = id;
        this.submissionNumber = submissionNumber;
        this.status = SubmissionStatus.DRAFT;
        this.surveyVersion = "2.4";
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void replaceProducts(List<ProductHistory> replacements) {
        products.clear();
        replacements.forEach(item -> { item.attachTo(this); products.add(item); });
    }

    public void replaceFractures(List<FractureHistory> replacements) {
        fractures.clear();
        replacements.forEach(item -> { item.attachTo(this); fractures.add(item); });
    }

    public void replaceFoodAnswers(List<FoodAnswer> replacements) {
        foodAnswers.clear();
        replacements.forEach(item -> { item.attachTo(this); foodAnswers.add(item); });
    }

    public void markSubmitted(Instant now) {
        status = SubmissionStatus.SUBMITTED;
        submittedAt = now;
        updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getSubmissionNumber() { return submissionNumber; }
    public SubmissionStatus getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public List<ProductHistory> getProducts() { return products; }
    public List<FractureHistory> getFractures() { return fractures; }
    public List<FoodAnswer> getFoodAnswers() { return foodAnswers; }
    public void setSurveyVersion(String value) { surveyVersion = value; }
    public void setPrivacyConsent(boolean value) { privacyConsent = value; }
    public void setContactConsent(boolean value) { contactConsent = value; }
    public void setParticipantCode(String value) { participantCode = value; }
    public void setSurveyMethod(String value) { surveyMethod = value; }
    public void setSurveyMethodOther(String value) { surveyMethodOther = value; }
    public void setSurveyDate(LocalDate value) { surveyDate = value; }
    public void setContactPhone(String value) { contactPhone = value; }
    public void setContactTime(String value) { contactTime = value; }
    public void setVoluntaryConsent(String value) { voluntaryConsent = value; }
    public void setBirthDate(LocalDate value) { birthDate = value; }
    public void setOver55(String value) { over55 = value; }
    public void setFemaleAtBirth(String value) { femaleAtBirth = value; }
    public void setExclusionDisease(String value) { exclusionDisease = value; }
    public void setRegion(String value) { region = value; }
    public void setRegionOther(String value) { regionOther = value; }
    public void setEducation(String value) { education = value; }
    public void setEducationOther(String value) { educationOther = value; }
    public void setHouseholdIncome(String value) { householdIncome = value; }
    public void setHouseholdIncomeOther(String value) { householdIncomeOther = value; }
    public void setEmployed(String value) { employed = value; }
    public void setEmploymentOther(String value) { employmentOther = value; }
    public void setHeightCm(BigDecimal value) { heightCm = value; }
    public void setWeightKg(BigDecimal value) { weightKg = value; }
    public void setMenopauseAge(Integer value) { menopauseAge = value; }
    public void setMenopauseAgeUnknown(boolean value) { menopauseAgeUnknown = value; }
    public void setHormoneTreatment(String value) { hormoneTreatment = value; }
    public void setHormoneDurationYears(Integer value) { hormoneDurationYears = value; }
    public void setHormoneDurationMonths(Integer value) { hormoneDurationMonths = value; }
    public void setHormoneDurationUnknown(boolean value) { hormoneDurationUnknown = value; }
    public void setProductExperience(String value) { productExperience = value; }
    public void setProductCurrentStatus(String value) { productCurrentStatus = value; }
    public void setProductStartRecall(String value) { productStartRecall = value; }
    public void setProductStartYearMonth(String value) { productStartYearMonth = value; }
    public void setProductStartYear(Integer value) { productStartYear = value; }
    public void setProductStartReasons(String value) { productStartReasons = value; }
    public void setProductStartReasonOther(String value) { productStartReasonOther = value; }
    public void setProductPriorityReasons(String value) { productPriorityReasons = value; }
    public void setProductInterruptionStatus(String value) { productInterruptionStatus = value; }
    public void setProductInterruptionDetails(String value) { productInterruptionDetails = value; }
    public void setEvidencePeriodType(String value) { evidencePeriodType = value; }
    public void setEvidenceStartYearMonth(String value) { evidenceStartYearMonth = value; }
    public void setEvidenceEndYearMonth(String value) { evidenceEndYearMonth = value; }
    public void setFractureExperience(String value) { fractureExperience = value; }
    public void setFractureTotalCount(Integer value) { fractureTotalCount = value; }
    public void setFractureCountUnknown(boolean value) { fractureCountUnknown = value; }
    public void setMealsPerDay(String value) { mealsPerDay = value; }
    public void setUpdatedAt(Instant value) { updatedAt = value; }
}
