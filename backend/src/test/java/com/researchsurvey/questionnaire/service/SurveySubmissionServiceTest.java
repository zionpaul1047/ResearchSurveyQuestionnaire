package com.researchsurvey.questionnaire.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.researchsurvey.questionnaire.dto.SurveySubmissionRequest;
import com.researchsurvey.questionnaire.entity.SubmissionStatus;
import com.researchsurvey.questionnaire.entity.SurveySubmission;
import com.researchsurvey.questionnaire.exception.ApiException;
import com.researchsurvey.questionnaire.repository.SurveySubmissionRepository;

@ExtendWith(MockitoExtension.class)
class SurveySubmissionServiceTest {
    @Mock
    SurveySubmissionRepository repository;

    @InjectMocks
    SurveySubmissionService service;

    @Test
    void createsDraftWithGeneratedIdentifier() {
        when(repository.save(any(SurveySubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createDraft(emptyRequest());

        assertThat(response.submissionId()).isNotNull();
        assertThat(response.submissionNumber()).startsWith("RSQ-");
        assertThat(response.status()).isEqualTo(SubmissionStatus.DRAFT);
    }

    @Test
    void rejectsEditingSubmittedSurvey() {
        UUID id = UUID.randomUUID();
        SurveySubmission submitted = new SurveySubmission(id, "RSQ-TEST", Instant.now());
        submitted.markSubmitted(Instant.now());
        when(repository.findById(id)).thenReturn(Optional.of(submitted));

        assertThatThrownBy(() -> service.updateDraft(id, emptyRequest()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("이미 최종 제출");
    }

    @Test
    void acceptsOnlyTheThreeFoodItemsPresentInTheSourceQuestionnaire() {
        UUID id = UUID.randomUUID();
        SurveySubmission draft = new SurveySubmission(id, "RSQ-TEST", Instant.now());
        when(repository.findById(id)).thenReturn(Optional.of(draft));
        when(repository.save(any(SurveySubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.submit(id, validRequest());

        assertThat(response.status()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(draft.getFoodAnswers()).hasSize(3);
    }

    @Test
    void rejectsUnknownProductExperienceUntilItIsConfirmed() {
        UUID id = UUID.randomUUID();
        SurveySubmission draft = new SurveySubmission(id, "RSQ-TEST", Instant.now());
        when(repository.findById(id)).thenReturn(Optional.of(draft));
        var valid = validRequest();
        var request = new SurveySubmissionRequest(valid.surveyVersion(), valid.privacyConsent(), valid.contactConsent(), valid.metadata(), valid.eligibility(), valid.basicInfo(), "UNKNOWN", null, List.of(), valid.fractureExperience(), valid.fractureTotalCount(), valid.fractureCountUnknown(), valid.fractures(), valid.mealsPerDay(), valid.foodAnswers());

        assertThatThrownBy(() -> service.submit(id, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("섭취한 제품을 확인");
    }

    @Test
    void rejectsInvalidPhoneEvenWhenSavingDraft() {
        var valid = validRequest();
        var metadata = new SurveySubmissionRequest.SurveyMetadataRequest("RSQ-001", "ONLINE", "", valid.metadata().surveyDate(), "010-ABCD-5678", "09:00-12:00");
        var request = copyWith(valid, metadata, valid.eligibility(), valid.basicInfo());

        assertThatThrownBy(() -> service.createDraft(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("연락처");
    }

    @Test
    void rejectsResidentNumberInFreeText() {
        var valid = validRequest();
        var basic = new SurveySubmissionRequest.BasicInfoRequest("기타", "900101-1234567", valid.basicInfo().education(), "", valid.basicInfo().householdIncome(), "", valid.basicInfo().employed(), "", valid.basicInfo().heightCm(), valid.basicInfo().weightKg(), valid.basicInfo().menopauseAge(), false, valid.basicInfo().hormoneTreatment(), "", "", false);
        var request = copyWith(valid, valid.metadata(), valid.eligibility(), basic);

        assertThatThrownBy(() -> service.createDraft(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("주민등록번호");
    }

    @Test
    void rejectsHeightWithMoreThanOneDecimalPlace() {
        var valid = validRequest();
        var basic = new SurveySubmissionRequest.BasicInfoRequest(valid.basicInfo().region(), "", valid.basicInfo().education(), "", valid.basicInfo().householdIncome(), "", valid.basicInfo().employed(), "", "160.25", valid.basicInfo().weightKg(), valid.basicInfo().menopauseAge(), false, valid.basicInfo().hormoneTreatment(), "", "", false);
        var request = copyWith(valid, valid.metadata(), valid.eligibility(), basic);

        assertThatThrownBy(() -> service.createDraft(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("소수점 첫째 자리");
    }

    @Test
    void rejectsBirthDateThatConflictsWithOver55Answer() {
        UUID id = UUID.randomUUID();
        SurveySubmission draft = new SurveySubmission(id, "RSQ-TEST", Instant.now());
        when(repository.findById(id)).thenReturn(Optional.of(draft));
        var valid = validRequest();
        var eligibility = new SurveySubmissionRequest.EligibilityRequest("YES", LocalDate.now().minusYears(40).toString(), "YES", "YES", "NONE");
        var request = copyWith(valid, valid.metadata(), eligibility, valid.basicInfo());

        assertThatThrownBy(() -> service.submit(id, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("만 55세 이상");
    }

    private SurveySubmissionRequest copyWith(SurveySubmissionRequest source, SurveySubmissionRequest.SurveyMetadataRequest metadata, SurveySubmissionRequest.EligibilityRequest eligibility, SurveySubmissionRequest.BasicInfoRequest basicInfo) {
        return new SurveySubmissionRequest(source.surveyVersion(), source.privacyConsent(), source.contactConsent(), metadata, eligibility, basicInfo, source.productExperience(), source.productDetails(), source.products(), source.fractureExperience(), source.fractureTotalCount(), source.fractureCountUnknown(), source.fractures(), source.mealsPerDay(), source.foodAnswers());
    }

    private SurveySubmissionRequest emptyRequest() {
        return new SurveySubmissionRequest("2.4", false, false, null, null, null, "", null, List.of(), "", "", false, List.of(), "", List.of());
    }

    private SurveySubmissionRequest validRequest() {
        return new SurveySubmissionRequest(
                "2.4",
                true,
                true,
                new SurveySubmissionRequest.SurveyMetadataRequest("RSQ-001", "ONLINE", "", "2026-08-13", "", ""),
                new SurveySubmissionRequest.EligibilityRequest("YES", "1960-01-01", "YES", "YES", "NONE"),
                new SurveySubmissionRequest.BasicInfoRequest("서울", "", "대학교 졸업", "", "400~600만원 미만", "", "NO", "", "160", "55", "52", false, "치료받은 적 없음", "", "", false),
                "NEVER",
                null,
                List.of(),
                "NO",
                "",
                false,
                List.of(),
                "세 끼",
                List.of(
                        new SurveySubmissionRequest.FoodAnswerRequest("RICE", "쌀밥", "일 1회", "1"),
                        new SurveySubmissionRequest.FoodAnswerRequest("MIXED_RICE", "잡곡밥(현미밥, 보리밥 등)", "주 2~4회", "1"),
                        new SurveySubmissionRequest.FoodAnswerRequest("BEAN_RICE", "콩밥", "거의 안 먹음", "")));
    }
}
