package com.researchsurvey.questionnaire.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.researchsurvey.questionnaire.dto.SubmissionResponse;
import com.researchsurvey.questionnaire.dto.SurveySubmissionRequest;
import com.researchsurvey.questionnaire.entity.FoodAnswer;
import com.researchsurvey.questionnaire.entity.FractureHistory;
import com.researchsurvey.questionnaire.entity.ProductHistory;
import com.researchsurvey.questionnaire.entity.SubmissionStatus;
import com.researchsurvey.questionnaire.entity.SurveySubmission;
import com.researchsurvey.questionnaire.exception.ApiException;
import com.researchsurvey.questionnaire.repository.SurveySubmissionRepository;

@Service
public class SurveySubmissionService {
    private static final Map<String, String> FOOD_NAMES = Map.of(
            "RICE", "쌀밥",
            "MIXED_RICE", "잡곡밥(현미밥, 보리밥 등)",
            "BEAN_RICE", "콩밥");
    private static final Set<String> REQUIRED_FOOD_CODES = FOOD_NAMES.keySet();
    private static final Set<String> FOOD_FREQUENCIES = Set.of("거의 안 먹음", "월 1회", "월 2~3회", "주 1회", "주 2~4회", "주 5~6회", "일 1회", "일 2회", "일 3회");
    private static final Set<String> FOOD_AMOUNTS = Set.of("0.5", "1", "1.5");
    private static final Set<String> PRODUCT_EXPERIENCES = Set.of("REGULAR", "SAMPLE_ONLY", "NEVER", "UNKNOWN");
    private static final Set<String> START_REASON_CODES = Set.of("1", "2", "3", "4", "5", "6", "7", "8");
    private static final Set<String> CONTACT_TIMES = Set.of("09:00-12:00", "12:00-15:00", "15:00-18:00", "18:00-20:00");
    private static final Set<String> REGIONS = Set.of("서울", "경기·인천", "강원", "충청", "전라", "경상", "제주", "기타");
    private static final Set<String> EDUCATIONS = Set.of("초등학교 졸업 이하", "중학교 졸업", "고등학교 졸업", "대학교 졸업", "대학원 이상", "기타");
    private static final Set<String> INCOMES = Set.of("200만원 미만", "200~400만원 미만", "400~600만원 미만", "600~800만원 미만", "800만원 이상", "기타");
    private static final Set<String> HORMONE_TREATMENTS = Set.of("현재 치료 중", "과거 치료, 현재 중단", "치료받은 적 없음", "잘 모르겠음");
    private static final Set<String> PRODUCT_FREQUENCIES = Set.of("매일 1회", "매일 2회 이상", "주 5~6일", "주 2~4일", "주 1일", "월 1~3일", "불규칙", "잘 모르겠음");
    private static final Set<String> INGREDIENTS = Set.of("칼슘", "비타민D", "복합제", "기타");
    private static final Set<String> YES_NO_UNKNOWN = Set.of("YES", "NO", "UNKNOWN");
    private static final Set<String> FRACTURE_SITES = Set.of("척추", "고관절", "손목", "상완", "발목", "갈비뼈", "기타");
    private static final Set<String> FRACTURE_CAUSES = Set.of("서 있거나 걷다가 넘어짐", "계단에서 넘어짐", "침대·의자 등 낮은 위치에서 떨어짐", "일상생활 중 가벼운 충돌", "일반적인 운동이나 신체활동 중 발생", "교통사고", "사다리·건물 등 높은 위치에서 추락", "의사에게 병적 골절이라고 설명받음", "기타", "잘 모르겠음");
    private static final Set<String> FRACTURE_RECORDS = Set.of("진단서·진료기록 확인 가능", "병원명·진료일 확인 가능", "확인 가능한 기록 없음", "잘 모르겠음");
    private static final Set<String> FRACTURE_TREATMENTS = Set.of("입원", "수술", "둘 다", "해당 없음");
    private static final Set<String> PRODUCT_TIMINGS = Set.of("제품 섭취 시작 전", "섭취 시작 후 6개월 이내", "6개월 초과~12개월 이내", "12개월 초과", "일시 중단 기간 중", "완전 중단 후", "잘 모르겠음");
    private static final Pattern PARTICIPANT_CODE = Pattern.compile("^[A-Z0-9][A-Z0-9_-]{0,39}$");
    private static final Pattern RESIDENT_NUMBER = Pattern.compile("(?<!\\d)\\d{6}-?\\d{7}(?!\\d)");
    private final SurveySubmissionRepository repository;

    public SurveySubmissionService(SurveySubmissionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SubmissionResponse createDraft(SurveySubmissionRequest request) {
        validateInputGuardrails(request);
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        SurveySubmission submission = new SurveySubmission(id, createSubmissionNumber(id), now);
        applyRequest(submission, request, now);
        return SubmissionResponse.from(repository.save(submission));
    }

    @Transactional
    public SubmissionResponse updateDraft(UUID id, SurveySubmissionRequest request) {
        SurveySubmission submission = find(id);
        ensureDraft(submission);
        validateInputGuardrails(request);
        applyRequest(submission, request, Instant.now());
        return SubmissionResponse.from(repository.save(submission));
    }

    @Transactional
    public SubmissionResponse submit(UUID id, SurveySubmissionRequest request) {
        SurveySubmission submission = find(id);
        ensureDraft(submission);
        validateFinalSubmission(request);
        Instant now = Instant.now();
        applyRequest(submission, request, now);
        submission.markSubmitted(now);
        return SubmissionResponse.from(repository.save(submission));
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getStatus(UUID id) {
        return SubmissionResponse.from(find(id));
    }

    SurveySubmission find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "설문 제출 정보를 찾을 수 없습니다."));
    }

    void ensureDraft(SurveySubmission submission) {
        if (submission.getStatus() != SubmissionStatus.DRAFT) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 최종 제출된 설문은 변경할 수 없습니다.");
        }
    }

    private void applyRequest(SurveySubmission submission, SurveySubmissionRequest request, Instant now) {
        if (request == null) throw new ApiException(HttpStatus.BAD_REQUEST, "설문 응답 본문이 없습니다.");
        submission.setSurveyVersion(normalizeSingleLine(defaultIfBlank(request.surveyVersion(), "2.4")));
        submission.setPrivacyConsent(request.privacyConsent());
        submission.setContactConsent(request.contactConsent());

        if (request.metadata() != null) {
            var value = request.metadata();
            submission.setParticipantCode(blankToNull(value.participantCode()) == null ? null : value.participantCode().trim().toUpperCase(Locale.ROOT));
            submission.setSurveyMethod(value.surveyMethod());
            submission.setSurveyMethodOther("OTHER".equals(value.surveyMethod()) ? normalizeSingleLineOrNull(value.surveyMethodOther()) : null);
            submission.setSurveyDate(parseDate(value.surveyDate(), "조사일"));
            submission.setContactPhone(normalizePhone(value.contactPhone()));
            submission.setContactTime(blankToNull(value.contactTime()));
        }

        if (request.eligibility() != null) {
            var value = request.eligibility();
            submission.setVoluntaryConsent(value.voluntaryConsent());
            submission.setBirthDate(parseDate(value.birthDate(), "생년월일"));
            submission.setOver55(value.over55());
            submission.setFemaleAtBirth(value.femaleAtBirth());
            submission.setExclusionDisease(value.exclusionDisease());
        }
        if (request.basicInfo() != null) {
            var value = request.basicInfo();
            submission.setRegion(value.region());
            submission.setRegionOther("기타".equals(value.region()) ? normalizeSingleLineOrNull(value.regionOther()) : null);
            submission.setEducation(value.education());
            submission.setEducationOther("기타".equals(value.education()) ? normalizeSingleLineOrNull(value.educationOther()) : null);
            submission.setHouseholdIncome(value.householdIncome());
            submission.setHouseholdIncomeOther("기타".equals(value.householdIncome()) ? normalizeSingleLineOrNull(value.householdIncomeOther()) : null);
            submission.setEmployed(value.employed());
            submission.setEmploymentOther("OTHER".equals(value.employed()) ? normalizeSingleLineOrNull(value.employmentOther()) : null);
            submission.setHeightCm(parseDecimal(value.heightCm(), "키"));
            submission.setWeightKg(parseDecimal(value.weightKg(), "체중"));
            submission.setMenopauseAge(value.menopauseAgeUnknown() ? null : parseInteger(value.menopauseAge(), "폐경 나이"));
            submission.setMenopauseAgeUnknown(value.menopauseAgeUnknown());
            submission.setHormoneTreatment(value.hormoneTreatment());
            boolean treatmentHistory = isOneOf(value.hormoneTreatment(), "현재 치료 중", "과거 치료, 현재 중단");
            submission.setHormoneDurationYears(treatmentHistory && !value.hormoneDurationUnknown() ? parseInteger(value.hormoneDurationYears(), "여성호르몬 치료 연수") : null);
            submission.setHormoneDurationMonths(treatmentHistory && !value.hormoneDurationUnknown() ? parseInteger(value.hormoneDurationMonths(), "여성호르몬 치료 개월") : null);
            submission.setHormoneDurationUnknown(treatmentHistory && value.hormoneDurationUnknown());
        }
        submission.setProductExperience(request.productExperience());
        if ("REGULAR".equals(request.productExperience()) && request.productDetails() != null) {
            var value = request.productDetails();
            submission.setProductCurrentStatus(value.currentStatus());
            submission.setProductStartRecall(value.startRecall());
            submission.setProductStartYearMonth("YEAR_MONTH".equals(value.startRecall()) ? blankToNull(value.startYearMonth()) : null);
            submission.setProductStartYear("YEAR_ONLY".equals(value.startRecall()) ? parseInteger(value.startYear(), "제품 최초 섭취 연도") : null);
            submission.setProductStartReasons(joinOrNull(value.startReasons()));
            submission.setProductStartReasonOther(safeList(value.startReasons()).contains("7") ? normalizeSingleLineOrNull(value.startReasonOther()) : null);
            submission.setProductPriorityReasons(joinOrNull(value.priorityReasons()));
            submission.setProductInterruptionStatus(value.interruptionStatus());
            submission.setProductInterruptionDetails("YES".equals(value.interruptionStatus()) ? normalizeMultilineOrNull(value.interruptionDetails()) : null);
            submission.setEvidencePeriodType(blankToNull(value.evidencePeriodType()));
            submission.setEvidenceStartYearMonth(isOneOf(value.evidencePeriodType(), "BOTH", "START_ONLY") ? blankToNull(value.evidenceStartYearMonth()) : null);
            submission.setEvidenceEndYearMonth(isOneOf(value.evidencePeriodType(), "BOTH", "END_ONLY") ? blankToNull(value.evidenceEndYearMonth()) : null);
        } else {
            clearProductDetails(submission);
        }
        submission.setFractureExperience(request.fractureExperience());
        boolean hasFractures = "YES".equals(request.fractureExperience());
        submission.setFractureTotalCount(hasFractures && !request.fractureCountUnknown() ? parseInteger(request.fractureTotalCount(), "골절 총 건수") : null);
        submission.setFractureCountUnknown(hasFractures && request.fractureCountUnknown());
        submission.setMealsPerDay(request.mealsPerDay());

        List<SurveySubmissionRequest.ProductRequest> products = "REGULAR".equals(request.productExperience()) ? safeList(request.products()).stream().filter(java.util.Objects::nonNull).toList() : List.of();
        submission.replaceProducts(IntStream.range(0, products.size()).mapToObj(index -> {
            var item = products.get(index);
            return new ProductHistory(index + 1, normalizeSingleLine(item.productName()), String.join(",", safeList(item.ingredients())), parseInteger(item.totalIntakeMonths(), "제품 섭취기간"), item.averageFrequency(), item.currentlyTaking());
        }).toList());

        List<SurveySubmissionRequest.FractureRequest> fractures = "YES".equals(request.fractureExperience()) ? safeList(request.fractures()).stream().filter(java.util.Objects::nonNull).toList() : List.of();
        submission.replaceFractures(IntStream.range(0, fractures.size()).mapToObj(index -> {
            var item = fractures.get(index);
            return new FractureHistory(index + 1, blankToNull(item.occurredYearMonth()), item.fractureSite(), item.fallRelated(), item.primaryCause(), item.recordAvailability(), item.treatment(), item.timingRelativeToProduct());
        }).toList());

        submission.replaceFoodAnswers(safeList(request.foodAnswers()).stream()
                .filter(item -> item != null && !isBlank(item.foodCode()) && FOOD_NAMES.containsKey(item.foodCode()))
                .map(item -> new FoodAnswer(item.foodCode(), FOOD_NAMES.get(item.foodCode()), item.frequency(), item.amount()))
                .toList());
        submission.setUpdatedAt(now);
    }

    private void validateInputGuardrails(SurveySubmissionRequest request) {
        if (request == null) throw new ApiException(HttpStatus.BAD_REQUEST, "설문 응답 본문이 없습니다.");
        validateSingleLine(request.surveyVersion(), 20, "설문 버전", false);

        if (request.metadata() != null) {
            var value = request.metadata();
            validateSingleLine(value.participantCode(), 40, "연구대상자 식별번호", true);
            if (!isBlank(value.participantCode())) {
                String normalizedCode = value.participantCode().trim().toUpperCase(Locale.ROOT);
                require(PARTICIPANT_CODE.matcher(normalizedCode).matches(), "연구대상자 식별번호는 영문 대문자, 숫자, -, _만 사용해 40자 이내로 입력해 주세요.");
                require(!RESIDENT_NUMBER.matcher(normalizedCode).find(), "연구대상자 식별번호에 주민등록번호를 입력할 수 없습니다.");
            }
            validateOptionalEnum(value.surveyMethod(), Set.of("ONLINE", "PHONE", "OTHER"), "조사 방법");
            if ("OTHER".equals(value.surveyMethod())) validateSingleLine(value.surveyMethodOther(), 100, "기타 조사 방법", true);
            if (!isBlank(value.surveyDate())) {
                LocalDate surveyDate = parseDate(value.surveyDate(), "조사일");
                require(!surveyDate.isAfter(LocalDate.now()), "조사일은 오늘 이후로 입력할 수 없습니다.");
            }
            validatePhone(value.contactPhone());
            validateOptionalEnum(value.contactTime(), CONTACT_TIMES, "통화 가능한 시간");
        }

        if (request.eligibility() != null) {
            var value = request.eligibility();
            validateOptionalEnum(value.voluntaryConsent(), Set.of("YES", "NO"), "자발적 동의");
            validateOptionalEnum(value.over55(), Set.of("YES", "NO"), "만 55세 이상 여부");
            validateOptionalEnum(value.femaleAtBirth(), Set.of("YES", "NO", "DECLINE"), "출생 시 성별 응답");
            validateOptionalEnum(value.exclusionDisease(), Set.of("NONE", "EXCLUSION", "UNKNOWN"), "제외 질환 여부");
            if (!isBlank(value.birthDate())) {
                LocalDate birthDate = parseDate(value.birthDate(), "생년월일");
                require(!birthDate.isBefore(LocalDate.of(1900, 1, 1)) && !birthDate.isAfter(LocalDate.now()), "생년월일은 1900년 1월 1일부터 오늘 사이로 입력해 주세요.");
            }
        }

        if (request.basicInfo() != null) {
            var value = request.basicInfo();
            validateOptionalEnum(value.region(), REGIONS, "거주지역");
            if ("기타".equals(value.region())) validateSingleLine(value.regionOther(), 100, "기타 거주지역", true);
            validateOptionalEnum(value.education(), EDUCATIONS, "최종 학력");
            if ("기타".equals(value.education())) validateSingleLine(value.educationOther(), 100, "기타 최종 학력", true);
            validateOptionalEnum(value.householdIncome(), INCOMES, "월평균 가구소득");
            if ("기타".equals(value.householdIncome())) validateSingleLine(value.householdIncomeOther(), 100, "기타 월평균 가구소득", true);
            validateOptionalEnum(value.employed(), Set.of("YES", "NO", "OTHER"), "직업 여부");
            if ("OTHER".equals(value.employed())) validateSingleLine(value.employmentOther(), 100, "기타 직업 상태", true);
            validateOptionalDecimal(value.heightCm(), 100, 220, "키");
            validateOptionalDecimal(value.weightKg(), 20, 250, "체중");
            validateOptionalInteger(value.menopauseAge(), 20, 80, "폐경 나이");
            require(!value.menopauseAgeUnknown() || isBlank(value.menopauseAge()), "폐경 나이를 잘 모르겠음으로 선택한 경우 나이 값을 함께 입력할 수 없습니다.");
            validateOptionalEnum(value.hormoneTreatment(), HORMONE_TREATMENTS, "여성호르몬 치료 여부");
            if (isOneOf(value.hormoneTreatment(), "현재 치료 중", "과거 치료, 현재 중단")) {
                validateOptionalInteger(value.hormoneDurationYears(), 0, 60, "여성호르몬 치료 연수");
                validateOptionalInteger(value.hormoneDurationMonths(), 0, 11, "여성호르몬 치료 개월");
                require(!value.hormoneDurationUnknown() || isBlank(value.hormoneDurationYears()) && isBlank(value.hormoneDurationMonths()), "치료 기간을 잘 모르겠음으로 선택한 경우 기간 값을 함께 입력할 수 없습니다.");
            }
        }

        validateOptionalEnum(request.productExperience(), PRODUCT_EXPERIENCES, "제품 섭취 경험");
        if ("REGULAR".equals(request.productExperience()) && request.productDetails() != null) {
            var value = request.productDetails();
            validateOptionalEnum(value.currentStatus(), Set.of("REGULAR", "INTERMITTENT", "STOPPED"), "현재 제품 섭취 상태");
            validateOptionalEnum(value.startRecall(), Set.of("YEAR_MONTH", "YEAR_ONLY", "UNKNOWN"), "제품 최초 섭취 시점 기억 범위");
            if (!isBlank(value.startYearMonth())) parseYearMonth(value.startYearMonth(), "제품 최초 섭취 연월");
            validateOptionalInteger(value.startYear(), 1900, LocalDate.now().getYear(), "제품 최초 섭취 연도");
            validateSingleLine(value.startReasonOther(), 500, "제품 섭취 기타 시작 이유", true);
            validateOptionalEnum(value.interruptionStatus(), Set.of("NONE", "YES", "UNKNOWN"), "제품 중단 여부");
            validateMultiline(value.interruptionDetails(), 2000, "제품 중단 및 재섭취 이력", true);
            validateOptionalEnum(value.evidencePeriodType(), Set.of("BOTH", "START_ONLY", "END_ONLY", "PARTIAL", "NONE"), "구매·수령 이력 확인 범위");
            if (!isBlank(value.evidenceStartYearMonth())) parseYearMonth(value.evidenceStartYearMonth(), "구매·수령 이력 시작 연월");
            if (!isBlank(value.evidenceEndYearMonth())) parseYearMonth(value.evidenceEndYearMonth(), "구매·수령 이력 종료 연월");
        }

        var products = "REGULAR".equals(request.productExperience()) ? safeList(request.products()) : List.<SurveySubmissionRequest.ProductRequest>of();
        require(products.size() <= 20, "섭취 제품은 최대 20개까지 입력할 수 있습니다.");
        products.stream().filter(java.util.Objects::nonNull).forEach(item -> {
            validateSingleLine(item.productName(), 200, "제품명", false);
            var ingredients = safeList(item.ingredients());
            require(ingredients.size() <= INGREDIENTS.size() && ingredients.stream().distinct().count() == ingredients.size() && INGREDIENTS.containsAll(ingredients), "주요 성분 선택값을 확인해 주세요.");
            validateOptionalInteger(item.totalIntakeMonths(), 0, 1200, "제품 섭취기간");
            validateOptionalEnum(item.averageFrequency(), PRODUCT_FREQUENCIES, "평균 섭취빈도");
            validateOptionalEnum(item.currentlyTaking(), YES_NO_UNKNOWN, "현재 제품 섭취 여부");
        });

        validateOptionalEnum(request.fractureExperience(), YES_NO_UNKNOWN, "골절 경험");
        if ("YES".equals(request.fractureExperience()) && !request.fractureCountUnknown()) validateOptionalInteger(request.fractureTotalCount(), 1, 99, "골절 총 건수");
        var fractures = "YES".equals(request.fractureExperience()) ? safeList(request.fractures()) : List.<SurveySubmissionRequest.FractureRequest>of();
        require(fractures.size() <= 3, "골절 상세정보는 최대 3건까지 입력할 수 있습니다.");
        fractures.stream().filter(java.util.Objects::nonNull).forEach(item -> {
            if (!isBlank(item.occurredYearMonth())) parseYearMonth(item.occurredYearMonth(), "골절 발생 연월");
            validateOptionalEnum(item.fractureSite(), FRACTURE_SITES, "골절 부위");
            validateOptionalEnum(item.fallRelated(), YES_NO_UNKNOWN, "골절 당시 낙상 여부");
            validateOptionalEnum(item.primaryCause(), FRACTURE_CAUSES, "골절 발생 원인");
            validateOptionalEnum(item.recordAvailability(), FRACTURE_RECORDS, "골절 기록 확인 가능 여부");
            validateOptionalEnum(item.treatment(), FRACTURE_TREATMENTS, "골절 입원·수술 여부");
            validateOptionalEnum(item.timingRelativeToProduct(), PRODUCT_TIMINGS, "골절과 제품 섭취 시점 관계");
        });

        require(safeList(request.foodAnswers()).size() <= REQUIRED_FOOD_CODES.size(), "식품 섭취 응답 개수가 올바르지 않습니다.");
    }

    private void validateFinalSubmission(SurveySubmissionRequest request) {
        validateInputGuardrails(request);
        require(request.privacyConsent() && request.contactConsent(), "필수 확인 항목을 모두 확인해 주세요.");
        require(request.metadata() != null, "설문 기본정보가 없습니다.");
        var metadata = request.metadata();
        require(isOneOf(metadata.surveyMethod(), "ONLINE", "PHONE", "OTHER"), "조사 방법을 확인해 주세요.");
        require(!isBlank(metadata.surveyDate()), "조사일을 입력해 주세요.");
        LocalDate surveyDate = parseDate(metadata.surveyDate(), "조사일");
        require(!"OTHER".equals(metadata.surveyMethod()) || !isBlank(metadata.surveyMethodOther()), "기타 조사 방법을 입력해 주세요.");
        require(request.eligibility() != null, "적격성 확인 정보가 없습니다.");
        var eligibility = request.eligibility();
        require("YES".equals(eligibility.voluntaryConsent()), "자발적 연구 참여 동의가 필요합니다.");
        require(!isBlank(eligibility.birthDate()), "생년월일을 입력해 주세요.");
        LocalDate birthDate = parseDate(eligibility.birthDate(), "생년월일");
        int age = Period.between(birthDate, surveyDate).getYears();
        require(!birthDate.isAfter(surveyDate) && age >= 0 && age <= 120, "생년월일과 조사일을 다시 확인해 주세요.");
        require(age >= 55, "생년월일 기준 만 55세 이상 조건을 충족하지 않습니다.");
        require("YES".equals(eligibility.over55()), "연구 참여 연령 조건을 충족하지 않습니다.");
        require("YES".equals(eligibility.femaleAtBirth()), "연구 참여 성별 조건을 충족하지 않습니다.");
        require("NONE".equals(eligibility.exclusionDisease()), "제외 질환 여부를 확인해 주세요.");

        require(request.basicInfo() != null, "기본정보가 없습니다.");
        var basic = request.basicInfo();
        require(!isBlank(basic.region()) && !isBlank(basic.education()) && !isBlank(basic.householdIncome()) && !isBlank(basic.employed()) && !isBlank(basic.hormoneTreatment()), "기본정보의 필수 문항을 모두 입력해 주세요.");
        require(REGIONS.contains(basic.region()) && EDUCATIONS.contains(basic.education()) && INCOMES.contains(basic.householdIncome()) && isOneOf(basic.employed(), "YES", "NO", "OTHER") && HORMONE_TREATMENTS.contains(basic.hormoneTreatment()), "기본정보 선택값을 확인해 주세요.");
        require(!"기타".equals(basic.region()) || !isBlank(basic.regionOther()), "기타 거주지역을 입력해 주세요.");
        require(!"기타".equals(basic.education()) || !isBlank(basic.educationOther()), "기타 최종 학력을 입력해 주세요.");
        require(!"기타".equals(basic.householdIncome()) || !isBlank(basic.householdIncomeOther()), "기타 월평균 가구소득을 입력해 주세요.");
        require(!"OTHER".equals(basic.employed()) || !isBlank(basic.employmentOther()), "기타 직업 상태를 입력해 주세요.");
        BigDecimal height = parseDecimal(basic.heightCm(), "키");
        BigDecimal weight = parseDecimal(basic.weightKg(), "체중");
        require(height != null && height.compareTo(BigDecimal.valueOf(100)) >= 0 && height.compareTo(BigDecimal.valueOf(220)) <= 0, "키는 100~220cm 범위로 입력해 주세요.");
        require(weight != null && weight.compareTo(BigDecimal.valueOf(20)) >= 0 && weight.compareTo(BigDecimal.valueOf(250)) <= 0, "체중은 20~250kg 범위로 입력해 주세요.");
        Integer menopauseAge = basic.menopauseAgeUnknown() ? null : parseInteger(basic.menopauseAge(), "폐경 나이");
        require(menopauseAge == null || menopauseAge >= 20 && menopauseAge <= 80, "폐경 나이는 20~80세 범위로 입력해 주세요.");
        if (isOneOf(basic.hormoneTreatment(), "현재 치료 중", "과거 치료, 현재 중단") && !basic.hormoneDurationUnknown()) {
            Integer years = parseInteger(basic.hormoneDurationYears(), "여성호르몬 치료 연수");
            Integer months = parseInteger(basic.hormoneDurationMonths(), "여성호르몬 치료 개월");
            require(years != null || months != null, "여성호르몬 치료 총 기간을 입력하거나 잘 모르겠음을 선택해 주세요.");
            require(years == null || years >= 0 && years <= 60, "여성호르몬 치료 연수는 0~60년 범위로 입력해 주세요.");
            require(months == null || months >= 0 && months <= 11, "여성호르몬 치료 개월은 0~11개월 범위로 입력해 주세요.");
            require((years == null ? 0 : years * 12) + (months == null ? 0 : months) > 0, "여성호르몬 치료 기간은 1개월 이상으로 입력해 주세요.");
        }

        require(!isBlank(request.productExperience()) && PRODUCT_EXPERIENCES.contains(request.productExperience()), "제품 섭취 경험을 선택해 주세요.");
        require(!"UNKNOWN".equals(request.productExperience()), "섭취한 제품을 확인한 뒤 다시 응답해 주세요.");
        if ("REGULAR".equals(request.productExperience())) {
            validateProductDetails(request.productDetails(), surveyDate);
            require(!safeList(request.products()).isEmpty(), "섭취 제품을 한 개 이상 입력해 주세요.");
            require(safeList(request.products()).stream().noneMatch(java.util.Objects::isNull), "섭취 제품 정보가 올바르지 않습니다.");
            safeList(request.products()).forEach(item -> {
                require(!isBlank(item.productName()) && !safeList(item.ingredients()).isEmpty() && !isBlank(item.totalIntakeMonths()) && !isBlank(item.averageFrequency()) && !isBlank(item.currentlyTaking()), "제품명, 주요 성분, 섭취기간, 섭취빈도, 현재 섭취 여부를 확인해 주세요.");
                Integer months = parseInteger(item.totalIntakeMonths(), "제품 섭취기간");
                require(months != null && months >= 0 && months <= 1200, "제품 섭취기간은 0~1200개월 범위로 입력해 주세요.");
            });
        }

        require(!isBlank(request.fractureExperience()), "골절 경험을 선택해 주세요.");
        require(!"UNKNOWN".equals(request.fractureExperience()), "골절 여부를 확인한 뒤 다시 응답해 주세요.");
        if ("YES".equals(request.fractureExperience())) {
            require(!safeList(request.fractures()).isEmpty() && safeList(request.fractures()).size() <= 3, "골절 이력은 1~3건으로 입력해 주세요.");
            require(safeList(request.fractures()).stream().noneMatch(java.util.Objects::isNull), "골절 이력 정보가 올바르지 않습니다.");
            Integer totalCount = request.fractureCountUnknown() ? null : parseInteger(request.fractureTotalCount(), "골절 총 건수");
            require(request.fractureCountUnknown() || totalCount != null && totalCount >= 1, "골절 총 건수를 입력하거나 잘 모르겠음을 선택해 주세요.");
            require(request.fractureCountUnknown() || safeList(request.fractures()).size() == Math.min(totalCount, 3), "골절 총 건수와 상세 입력 건수를 확인해 주세요.");
            YearMonth surveyMonth = YearMonth.from(surveyDate);
            safeList(request.fractures()).forEach(item -> {
                require(!isBlank(item.occurredYearMonth()) && !isBlank(item.fractureSite()) && !isBlank(item.fallRelated()) && !isBlank(item.primaryCause()) && !isBlank(item.recordAvailability()) && !isBlank(item.treatment()), "골절 발생 연월, 부위, 낙상 여부, 원인, 기록, 치료 정보를 확인해 주세요.");
                YearMonth occurredMonth = parseYearMonth(item.occurredYearMonth(), "골절 발생 연월");
                require(!occurredMonth.isAfter(surveyMonth) && !occurredMonth.isBefore(surveyMonth.minusMonths(24)), "골절 발생 연월은 조사일 이전 24개월 이내여야 합니다.");
                require(!"REGULAR".equals(request.productExperience()) || !isBlank(item.timingRelativeToProduct()), "각 골절과 제품 섭취 시점의 관계를 선택해 주세요.");
            });
        }

        require(isOneOf(request.mealsPerDay(), "한 끼", "두 끼", "세 끼", "네 끼", "다섯 끼 이상"), "하루 평균 식사 횟수를 선택해 주세요.");
        var foodAnswers = safeList(request.foodAnswers());
        require(foodAnswers.stream().allMatch(item -> item != null && !isBlank(item.foodCode()) && FOOD_NAMES.get(item.foodCode()) != null && FOOD_NAMES.get(item.foodCode()).equals(item.foodName()) && !isBlank(item.frequency())), "식품 섭취 응답이 올바르지 않습니다.");
        Set<String> completedFoodCodes = foodAnswers.stream().filter(item -> !isBlank(item.foodCode()) && !isBlank(item.foodName()) && !isBlank(item.frequency())).map(SurveySubmissionRequest.FoodAnswerRequest::foodCode).collect(java.util.stream.Collectors.toSet());
        require(completedFoodCodes.equals(REQUIRED_FOOD_CODES) && foodAnswers.size() == REQUIRED_FOOD_CODES.size(), "원본 설문에 포함된 모든 식품의 섭취 빈도를 한 번씩 선택해 주세요.");
        require(foodAnswers.stream().allMatch(item -> FOOD_FREQUENCIES.contains(item.frequency())), "식품 섭취 빈도 값이 올바르지 않습니다.");
        require(foodAnswers.stream().allMatch(item -> "거의 안 먹음".equals(item.frequency()) ? isBlank(item.amount()) : FOOD_AMOUNTS.contains(item.amount())), "섭취하는 식품의 1회 섭취량을 확인해 주세요.");
    }

    private void validateProductDetails(SurveySubmissionRequest.ProductDetailsRequest details, LocalDate surveyDate) {
        require(details != null, "제품 섭취 상세정보가 없습니다.");
        YearMonth surveyMonth = YearMonth.from(surveyDate);
        require(isOneOf(details.currentStatus(), "REGULAR", "INTERMITTENT", "STOPPED"), "현재 제품 섭취 상태를 선택해 주세요.");
        require(isOneOf(details.startRecall(), "YEAR_MONTH", "YEAR_ONLY", "UNKNOWN"), "제품 최초 섭취 시점의 기억 범위를 선택해 주세요.");
        if ("YEAR_MONTH".equals(details.startRecall())) {
            YearMonth startMonth = parseYearMonth(details.startYearMonth(), "제품 최초 섭취 연월");
            require(!startMonth.isBefore(YearMonth.of(1900, 1)) && !startMonth.isAfter(surveyMonth), "제품 최초 섭취 연월은 1900년 1월부터 조사월 사이로 입력해 주세요.");
        }
        if ("YEAR_ONLY".equals(details.startRecall())) {
            Integer year = parseInteger(details.startYear(), "제품 최초 섭취 연도");
            require(year != null && year >= 1900 && year <= surveyDate.getYear(), "제품 최초 섭취 연도를 확인해 주세요.");
        }
        List<String> reasons = safeList(details.startReasons());
        require(!reasons.isEmpty() && reasons.stream().allMatch(reason -> !isBlank(reason) && START_REASON_CODES.contains(reason)) && reasons.stream().distinct().count() == reasons.size(), "제품 섭취 시작 이유를 확인해 주세요.");
        require(!reasons.contains("8") || reasons.size() == 1, "잘 모르겠음은 다른 제품 섭취 시작 이유와 함께 선택할 수 없습니다.");
        require(!reasons.contains("7") || !isBlank(details.startReasonOther()), "제품 섭취를 시작한 기타 이유를 입력해 주세요.");
        List<String> priorities = safeList(details.priorityReasons());
        if (reasons.stream().anyMatch(reason -> !"8".equals(reason))) {
            require(!priorities.isEmpty() && priorities.size() <= 3 && priorities.stream().distinct().count() == priorities.size() && reasons.containsAll(priorities) && !priorities.contains("8"), "제품 섭취 중요 이유 순위를 확인해 주세요.");
        }
        require(isOneOf(details.interruptionStatus(), "NONE", "YES", "UNKNOWN"), "제품 중단 여부를 선택해 주세요.");
        require(!"YES".equals(details.interruptionStatus()) || !isBlank(details.interruptionDetails()), "제품 중단 및 재섭취 이력을 입력해 주세요.");
        if (!isBlank(details.evidencePeriodType())) {
            require(isOneOf(details.evidencePeriodType(), "BOTH", "START_ONLY", "END_ONLY", "PARTIAL", "NONE"), "구매·수령 이력 확인 범위를 확인해 주세요.");
            YearMonth evidenceStart = isOneOf(details.evidencePeriodType(), "BOTH", "START_ONLY") ? parseYearMonth(details.evidenceStartYearMonth(), "구매·수령 이력 시작 연월") : null;
            YearMonth evidenceEnd = isOneOf(details.evidencePeriodType(), "BOTH", "END_ONLY") ? parseYearMonth(details.evidenceEndYearMonth(), "구매·수령 이력 종료 연월") : null;
            require(evidenceStart == null || !evidenceStart.isAfter(surveyMonth), "구매·수령 이력 시작 연월은 조사월 이후로 입력할 수 없습니다.");
            require(evidenceEnd == null || !evidenceEnd.isAfter(surveyMonth), "구매·수령 이력 종료 연월은 조사월 이후로 입력할 수 없습니다.");
            require(evidenceStart == null || evidenceEnd == null || !evidenceStart.isAfter(evidenceEnd), "구매·수령 이력의 시작 연월과 종료 연월 순서를 확인해 주세요.");
        }
    }

    private void clearProductDetails(SurveySubmission submission) {
        submission.setProductCurrentStatus(null);
        submission.setProductStartRecall(null);
        submission.setProductStartYearMonth(null);
        submission.setProductStartYear(null);
        submission.setProductStartReasons(null);
        submission.setProductStartReasonOther(null);
        submission.setProductPriorityReasons(null);
        submission.setProductInterruptionStatus(null);
        submission.setProductInterruptionDetails(null);
        submission.setEvidencePeriodType(null);
        submission.setEvidenceStartYearMonth(null);
        submission.setEvidenceEndYearMonth(null);
    }

    private String createSubmissionNumber(UUID id) {
        return "RSQ-" + LocalDate.now() + "-" + id.toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    private static void validateOptionalEnum(String value, Set<String> allowed, String fieldName) {
        if (!isBlank(value)) require(allowed.contains(value), fieldName + " 선택값이 올바르지 않습니다.");
    }

    private static void validateSingleLine(String value, int maxLength, String fieldName, boolean protectSensitiveIdentifier) {
        if (isBlank(value)) return;
        require(value.length() <= maxLength, fieldName + "은(는) " + maxLength + "자 이내로 입력해 주세요.");
        require(value.chars().noneMatch(character -> character < 32 || character == 127), fieldName + "에는 줄바꿈이나 제어문자를 입력할 수 없습니다.");
        if (protectSensitiveIdentifier) require(!RESIDENT_NUMBER.matcher(value).find(), fieldName + "에 주민등록번호를 입력할 수 없습니다.");
    }

    private static void validateMultiline(String value, int maxLength, String fieldName, boolean protectSensitiveIdentifier) {
        if (isBlank(value)) return;
        require(value.length() <= maxLength, fieldName + "은(는) " + maxLength + "자 이내로 입력해 주세요.");
        require(value.chars().noneMatch(character -> character < 32 && character != '\n' && character != '\r' || character == 127), fieldName + "에 허용되지 않는 제어문자가 포함되어 있습니다.");
        if (protectSensitiveIdentifier) require(!RESIDENT_NUMBER.matcher(value).find(), fieldName + "에 주민등록번호를 입력할 수 없습니다.");
    }

    private static void validatePhone(String value) {
        if (isBlank(value)) return;
        require(value.matches("^[0-9 -]+$"), "연락처에는 숫자, 공백, 하이픈만 입력할 수 있습니다.");
        String digits = value.replaceAll("\\D", "");
        boolean valid = digits.startsWith("02") ? digits.matches("^02\\d{7,8}$") : digits.matches("^0\\d{9,10}$");
        require(valid, "연락처는 올바른 9~11자리 국내 전화번호로 입력해 주세요.");
    }

    private static void validateOptionalDecimal(String value, int min, int max, String fieldName) {
        if (isBlank(value)) return;
        require(value.matches("^\\d{1,3}(\\.\\d)?$"), fieldName + "은(는) 소수점 첫째 자리까지 입력해 주세요.");
        BigDecimal number = parseDecimal(value, fieldName);
        require(number.compareTo(BigDecimal.valueOf(min)) >= 0 && number.compareTo(BigDecimal.valueOf(max)) <= 0, fieldName + "은(는) " + min + "~" + max + " 범위로 입력해 주세요.");
    }

    private static void validateOptionalInteger(String value, int min, int max, String fieldName) {
        if (isBlank(value)) return;
        require(value.matches("^\\d+$"), fieldName + "에는 정수를 입력해 주세요.");
        Integer number = parseInteger(value, fieldName);
        require(number >= min && number <= max, fieldName + "은(는) " + min + "~" + max + " 범위로 입력해 주세요.");
    }

    private static String normalizePhone(String value) {
        if (isBlank(value)) return null;
        String digits = value.replaceAll("\\D", "");
        if (digits.startsWith("02")) {
            int middleEnd = digits.length() == 9 ? 5 : 6;
            return digits.substring(0, 2) + "-" + digits.substring(2, middleEnd) + "-" + digits.substring(middleEnd);
        }
        int middleEnd = digits.length() == 10 ? 6 : 7;
        return digits.substring(0, 3) + "-" + digits.substring(3, middleEnd) + "-" + digits.substring(middleEnd);
    }

    private static String normalizeSingleLine(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private static String normalizeSingleLineOrNull(String value) {
        return isBlank(value) ? null : normalizeSingleLine(value);
    }

    private static String normalizeMultilineOrNull(String value) {
        return isBlank(value) ? null : value.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private static LocalDate parseDate(String value, String fieldName) {
        if (isBlank(value)) return null;
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException exception) { throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " 형식이 올바르지 않습니다."); }
    }

    private static YearMonth parseYearMonth(String value, String fieldName) {
        if (isBlank(value)) throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + "을(를) 입력해 주세요.");
        try { return YearMonth.parse(value); }
        catch (DateTimeParseException exception) { throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " 형식이 올바르지 않습니다."); }
    }

    private static BigDecimal parseDecimal(String value, String fieldName) {
        if (isBlank(value)) return null;
        try { return new BigDecimal(value); }
        catch (NumberFormatException exception) { throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + "에는 숫자를 입력해 주세요."); }
    }

    private static Integer parseInteger(String value, String fieldName) {
        if (isBlank(value)) return null;
        try { return Integer.valueOf(value); }
        catch (NumberFormatException exception) { throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + "에는 정수를 입력해 주세요."); }
    }

    private static String defaultIfBlank(String value, String defaultValue) { return isBlank(value) ? defaultValue : value; }
    private static String blankToNull(String value) { return isBlank(value) ? null : value.trim(); }
    private static String joinOrNull(List<String> values) { return safeList(values).isEmpty() ? null : String.join(",", values); }
    private static boolean isOneOf(String value, String... allowed) {
        if (value == null) return false;
        for (String candidate : allowed) if (candidate.equals(value)) return true;
        return false;
    }
    private static boolean isBlank(String value) { return value == null || value.isBlank(); }
    private static <T> List<T> safeList(List<T> value) { return value == null ? Collections.emptyList() : value; }
}
