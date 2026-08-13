import { useEffect, useMemo, useState } from "react";
import { saveDraft, submitSurvey, uploadAttachments } from "./api";
import SurveyLayout from "./components/SurveyLayout";
import BasicInfoStep from "./steps/BasicInfoStep";
import ConsentStep from "./steps/ConsentStep";
import EligibilityStep from "./steps/EligibilityStep";
import FoodStep from "./steps/FoodStep";
import FractureStep from "./steps/FractureStep";
import ProductStep from "./steps/ProductStep";
import ReviewStep from "./steps/ReviewStep";
import { initialSurveyForm, SOURCE_FOOD_ITEMS, type SubmissionResponse, type SurveyForm } from "./types";
import { ageOnDate, ALLOWED_IMAGE_TYPES, CONTACT_TIME_OPTIONS, hasAtMostOneDecimal, isIntegerInRange, isValidKoreanPhone, MAX_ATTACHMENTS_PER_CATEGORY, monthMinus, PARTICIPANT_CODE_PATTERN, PRODUCT_FREQUENCIES, todayLocal } from "./validation";

const STEPS = ["참여 동의", "적격성 확인", "기본 정보", "제품 섭취", "골절 경험", "식품 섭취", "확인 및 제출"];
const DRAFT_KEY = "research-survey-form-v2.4";
const ID_KEY = "research-survey-submission-id";

function loadLocalForm(): SurveyForm {
  try {
    const legacy = localStorage.getItem(DRAFT_KEY);
    const raw = sessionStorage.getItem(DRAFT_KEY) ?? legacy;
    if (legacy) {
      sessionStorage.setItem(DRAFT_KEY, legacy);
      localStorage.removeItem(DRAFT_KEY);
    }
    if (!raw) return initialSurveyForm();
    const initial = initialSurveyForm();
    const saved = JSON.parse(raw) as Partial<SurveyForm>;
    const allowedFoodCodes = new Set<string>(SOURCE_FOOD_ITEMS.map(([code]) => code));
    return {
      ...initial,
      ...saved,
      metadata: { ...initial.metadata, ...saved.metadata },
      eligibility: { ...initial.eligibility, ...saved.eligibility },
      basicInfo: { ...initial.basicInfo, ...saved.basicInfo },
      productDetails: { ...initial.productDetails, ...saved.productDetails },
      products: saved.products?.length ? saved.products : initial.products,
      fractures: saved.fractures?.length ? saved.fractures : initial.fractures,
      foodAnswers: (saved.foodAnswers ?? []).filter((item) => allowedFoodCodes.has(item.foodCode))
    };
  } catch {
    return initialSurveyForm();
  }
}

export default function App() {
  const [step, setStep] = useState(0);
  const [form, setForm] = useState<SurveyForm>(loadLocalForm);
  const [submissionId, setSubmissionId] = useState(() => {
    const legacy = localStorage.getItem(ID_KEY);
    if (legacy) {
      sessionStorage.setItem(ID_KEY, legacy);
      localStorage.removeItem(ID_KEY);
    }
    return sessionStorage.getItem(ID_KEY) ?? "";
  });
  const [purchaseEvidenceFiles, setPurchaseEvidenceFiles] = useState<File[]>([]);
  const [productPhotoFiles, setProductPhotoFiles] = useState<File[]>([]);
  const [saveMessage, setSaveMessage] = useState("현재 브라우저 탭에 임시저장 중");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [completed, setCompleted] = useState<SubmissionResponse | null>(null);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      sessionStorage.setItem(DRAFT_KEY, JSON.stringify(form));
      setSaveMessage("현재 탭에 자동 저장됨");
    }, 350);
    return () => window.clearTimeout(timer);
  }, [form]);

  const disqualified = useMemo(() => form.eligibility.voluntaryConsent === "NO" || form.eligibility.over55 === "NO" || ["NO", "DECLINE"].includes(form.eligibility.femaleAtBirth) || ["EXCLUSION", "UNKNOWN"].includes(form.eligibility.exclusionDisease), [form.eligibility]);

  const patchForm = (patch: Partial<SurveyForm>) => setForm((current) => ({ ...current, ...patch }));
  const move = (next: number) => { setError(""); setStep(Math.max(0, Math.min(STEPS.length - 1, next))); window.scrollTo({ top: 0, behavior: "smooth" }); };
  const handleFilesChange = (nextFiles: File[], setFiles: (files: File[]) => void) => {
    if (nextFiles.length > MAX_ATTACHMENTS_PER_CATEGORY) {
      setFiles(nextFiles.slice(0, MAX_ATTACHMENTS_PER_CATEGORY));
      setError(`첨부파일은 분류별 최대 ${MAX_ATTACHMENTS_PER_CATEGORY}개까지 선택할 수 있습니다.`);
      return;
    }
    const unsupported = nextFiles.find((file) => !ALLOWED_IMAGE_TYPES.has(file.type));
    if (unsupported) {
      setFiles(nextFiles.filter((file) => ALLOWED_IMAGE_TYPES.has(file.type)));
      setError(`${unsupported.name}: JPG, PNG, WEBP 이미지만 선택할 수 있습니다.`);
      return;
    }
    const oversized = nextFiles.find((file) => file.size > 10 * 1024 * 1024);
    if (oversized) {
      setFiles(nextFiles.filter((file) => file.size <= 10 * 1024 * 1024));
      setError(`${oversized.name}: 파일은 10MB 이하만 선택할 수 있습니다.`);
      return;
    }
    setError("");
    setFiles(nextFiles);
  };

  const validateStep = (targetStep = step): string => {
    if (targetStep === 0) {
      if (!form.privacyConsent || !form.contactConsent) return "필수 확인 항목을 모두 확인해 주세요.";
      if (!form.metadata.surveyMethod || !form.metadata.surveyDate) return "조사 방법과 조사일을 확인해 주세요.";
      if (form.metadata.surveyDate > todayLocal()) return "조사일은 오늘 이후로 입력할 수 없습니다.";
      if (form.metadata.participantCode && !PARTICIPANT_CODE_PATTERN.test(form.metadata.participantCode)) return "식별번호는 영문 대문자·숫자·-·_를 사용해 40자 이내로 입력해 주세요.";
      if (!isValidKoreanPhone(form.metadata.contactPhone)) return "연락처는 0으로 시작하는 9~11자리 국내 전화번호로 입력해 주세요.";
      if (form.metadata.contactTime && !CONTACT_TIME_OPTIONS.some(([value]) => value === form.metadata.contactTime)) return "통화 가능한 시간을 제공된 시간 구간에서 선택해 주세요.";
      if (form.metadata.surveyMethod === "OTHER" && !form.metadata.surveyMethodOther.trim()) return "기타 조사 방법을 입력해 주세요.";
    }
    if (targetStep === 1) {
      if (!form.eligibility.voluntaryConsent || !form.eligibility.birthDate || !form.eligibility.over55 || !form.eligibility.femaleAtBirth || !form.eligibility.exclusionDisease) return "적격성 확인 필수 문항에 모두 응답해 주세요.";
      const age = ageOnDate(form.eligibility.birthDate, form.metadata.surveyDate);
      if (age === null || age < 0 || age > 120) return "생년월일과 조사일을 다시 확인해 주세요.";
      if ((age >= 55) !== (form.eligibility.over55 === "YES")) return `생년월일 기준 만 ${age}세로, 만 55세 이상 응답과 일치하지 않습니다.`;
      if (disqualified) return "현재 응답으로는 설문을 계속할 수 없습니다. 연구 담당자에게 문의해 주세요.";
    }
    if (targetStep === 2) {
      const info = form.basicInfo;
      if (!info.region || !info.education || !info.householdIncome || !info.employed || !info.heightCm || !info.weightKg || !info.hormoneTreatment) return "기본정보의 필수 문항을 모두 입력해 주세요.";
      if ((info.region === "기타" && !info.regionOther.trim()) || (info.education === "기타" && !info.educationOther.trim()) || (info.householdIncome === "기타" && !info.householdIncomeOther.trim()) || (info.employed === "OTHER" && !info.employmentOther.trim())) return "기타를 선택한 항목의 내용을 입력해 주세요.";
      const height = Number(info.heightCm); const weight = Number(info.weightKg);
      if (!hasAtMostOneDecimal(info.heightCm) || height < 100 || height > 220 || !hasAtMostOneDecimal(info.weightKg) || weight < 20 || weight > 250) return "키와 체중은 허용 범위 안에서 소수점 첫째 자리까지 입력해 주세요.";
      if (info.menopauseAge && !isIntegerInRange(info.menopauseAge, 20, 80)) return "폐경 나이는 20~80세 정수로 입력해 주세요.";
      if (["현재 치료 중", "과거 치료, 현재 중단"].includes(info.hormoneTreatment) && !info.hormoneDurationUnknown && !info.hormoneDurationYears && !info.hormoneDurationMonths) return "여성호르몬 치료 총 기간을 입력하거나 ‘잘 모르겠음’을 선택해 주세요.";
      if (info.hormoneDurationYears && !isIntegerInRange(info.hormoneDurationYears, 0, 60)) return "치료 기간의 연수는 0~60 범위의 정수로 입력해 주세요.";
      if (info.hormoneDurationMonths && !isIntegerInRange(info.hormoneDurationMonths, 0, 11)) return "치료 기간의 개월은 0~11 범위의 정수로 입력해 주세요.";
      if (!info.hormoneDurationUnknown && info.hormoneDurationYears === "0" && (!info.hormoneDurationMonths || info.hormoneDurationMonths === "0")) return "치료 기간은 1개월 이상으로 입력해 주세요.";
    }
    if (targetStep === 3) {
      if (!form.productExperience) return "제품 섭취 경험을 선택해 주세요.";
      if (form.productExperience === "UNKNOWN") return "섭취한 제품을 확인한 뒤 다시 응답해 주세요.";
      if (form.productExperience === "REGULAR") {
        const details = form.productDetails;
        if (!details.currentStatus || !details.startRecall || !details.startReasons.length || !details.interruptionStatus) return "B2~B5 제품 섭취 상세 문항에 모두 응답해 주세요.";
        const surveyMonth = form.metadata.surveyDate.slice(0, 7);
        const surveyYear = Number(form.metadata.surveyDate.slice(0, 4));
        if (details.startRecall === "YEAR_MONTH" && (!details.startYearMonth || details.startYearMonth < "1900-01" || details.startYearMonth > surveyMonth)) return "제품 최초 섭취 연월은 1900년 1월부터 조사월 사이로 입력해 주세요.";
        if (details.startRecall === "YEAR_ONLY" && (!isIntegerInRange(details.startYear, 1900, surveyYear) || details.startYear.length !== 4)) return "제품 최초 섭취 연도를 네 자리로 입력해 주세요.";
        if (details.startReasons.includes("7") && !details.startReasonOther.trim()) return "제품 섭취를 시작한 기타 이유를 입력해 주세요.";
        if (details.startReasons.some((reason) => reason !== "8") && !details.priorityReasons[0]) return "제품 섭취를 결정한 가장 중요한 이유를 선택해 주세요.";
        if (new Set(details.priorityReasons).size !== details.priorityReasons.length) return "중요 이유 순위에는 같은 항목을 중복 선택할 수 없습니다.";
        if (details.interruptionStatus === "YES" && !details.interruptionDetails.trim()) return "제품 중단 및 재섭취 이력을 입력해 주세요.";
        if (details.evidencePeriodType === "BOTH" && (!details.evidenceStartYearMonth || !details.evidenceEndYearMonth || details.evidenceStartYearMonth > details.evidenceEndYearMonth)) return "구매·수령 이력의 시작 연월과 종료 연월 순서를 확인해 주세요.";
        if ((details.evidenceStartYearMonth && details.evidenceStartYearMonth > surveyMonth) || (details.evidenceEndYearMonth && details.evidenceEndYearMonth > surveyMonth)) return "구매·수령 이력은 조사월 이후로 입력할 수 없습니다.";
        if (!form.products.length || form.products.length > 20 || form.products.some((item) => !item.productName.trim() || item.productName.length > 200 || !item.ingredients.length || !isIntegerInRange(item.totalIntakeMonths, 0, 1200) || !PRODUCT_FREQUENCIES.includes(item.averageFrequency as typeof PRODUCT_FREQUENCIES[number]) || !item.currentlyTaking)) return "각 제품의 이름, 주요 성분, 총 섭취기간, 표준 섭취빈도, 현재 섭취 여부를 확인해 주세요.";
      }
    }
    if (targetStep === 4) {
      if (!form.fractureExperience) return "골절 경험 여부를 선택해 주세요.";
      if (form.fractureExperience === "UNKNOWN") return "골절 여부를 확인한 뒤 다시 응답해 주세요.";
      if (form.fractureExperience === "YES") {
        const total = Number(form.fractureTotalCount);
        if (!form.fractureCountUnknown && !isIntegerInRange(form.fractureTotalCount, 1, 99)) return "최근 24개월 이내 골절 총 건수를 1~99건으로 입력하거나 ‘잘 모르겠음’을 선택해 주세요.";
        if (!form.fractureCountUnknown && form.fractures.length !== Math.min(total, 3)) return "골절 총 건수와 상세 입력 건수를 확인해 주세요.";
        if (!form.fractures.length || form.fractures.some((item) => !item.occurredYearMonth || !item.fractureSite || !item.fallRelated || !item.primaryCause || !item.recordAvailability || !item.treatment || (form.productExperience === "REGULAR" && !item.timingRelativeToProduct))) return "각 골절의 발생 연월, 부위, 낙상 여부, 원인, 기록, 치료 정보를 확인해 주세요.";
        const earliest = monthMinus(form.metadata.surveyDate, 24); const latest = form.metadata.surveyDate.slice(0, 7);
        if (form.fractures.some((item) => item.occurredYearMonth < earliest || item.occurredYearMonth > latest)) return "골절 발생 연월은 조사일 이전 24개월 범위로 입력해 주세요.";
      }
    }
    if (targetStep === 5) {
      if (!form.mealsPerDay) return "하루 평균 식사 횟수를 선택해 주세요.";
      if (form.foodAnswers.filter((item) => item.frequency).length < SOURCE_FOOD_ITEMS.length) return "모든 식품의 섭취 빈도를 선택해 주세요.";
      if (form.foodAnswers.some((item) => item.frequency !== "거의 안 먹음" && !item.amount)) return "섭취하는 식품의 1회 섭취량을 모두 선택해 주세요.";
    }
    return "";
  };

  const handleNext = () => {
    const validationError = validateStep(step);
    if (validationError) { setError(validationError); return; }
    move(step + 1);
  };

  const persistDraft = async (): Promise<SubmissionResponse> => {
    const saved = await saveDraft(form, submissionId || undefined);
    if (!submissionId) {
      setSubmissionId(saved.submissionId);
      sessionStorage.setItem(ID_KEY, saved.submissionId);
    }
    return saved;
  };

  const handleServerSave = async () => {
    setBusy(true); setError("");
    try {
      const saved = await persistDraft();
      setSaveMessage(`서버 임시저장 완료 · ${new Date(saved.updatedAt).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}`);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "임시저장 중 오류가 발생했습니다.");
    } finally { setBusy(false); }
  };

  const handleSubmit = async () => {
    setBusy(true); setError("");
    try {
      for (let targetStep = 0; targetStep < STEPS.length - 1; targetStep += 1) {
        const validationError = validateStep(targetStep);
        if (validationError) {
          move(targetStep);
          setError(validationError);
          return;
        }
      }
      const draft = await persistDraft();
      if (purchaseEvidenceFiles.length) await uploadAttachments(draft.submissionId, purchaseEvidenceFiles, "PURCHASE_EVIDENCE");
      if (productPhotoFiles.length) await uploadAttachments(draft.submissionId, productPhotoFiles, "PRODUCT_PHOTO");
      const response = await submitSurvey(draft.submissionId, form);
      sessionStorage.removeItem(DRAFT_KEY);
      sessionStorage.removeItem(ID_KEY);
      setCompleted(response);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "최종 제출 중 오류가 발생했습니다.");
    } finally { setBusy(false); }
  };

  if (completed) return <Completion response={completed} />;

  return <SurveyLayout step={step} steps={STEPS} saveMessage={saveMessage} onMove={move}>
    {step === 0 && <ConsentStep form={form} onChange={patchForm} />}
    {step === 1 && <EligibilityStep value={form.eligibility} disqualified={disqualified} onChange={(eligibility) => patchForm({ eligibility })} />}
    {step === 2 && <BasicInfoStep value={form.basicInfo} onChange={(basicInfo) => patchForm({ basicInfo })} />}
    {step === 3 && <ProductStep experience={form.productExperience} surveyDate={form.metadata.surveyDate} details={form.productDetails} products={form.products} purchaseEvidenceFiles={purchaseEvidenceFiles} productPhotoFiles={productPhotoFiles} onExperienceChange={(productExperience) => patchForm({ productExperience })} onDetailsChange={(productDetails) => patchForm({ productDetails })} onProductsChange={(products) => patchForm({ products })} onPurchaseEvidenceFilesChange={(files) => handleFilesChange(files, setPurchaseEvidenceFiles)} onProductPhotoFilesChange={(files) => handleFilesChange(files, setProductPhotoFiles)} />}
    {step === 4 && <FractureStep experience={form.fractureExperience} surveyDate={form.metadata.surveyDate} totalCount={form.fractureTotalCount} countUnknown={form.fractureCountUnknown} fractures={form.fractures} showProductTiming={form.productExperience === "REGULAR"} onExperienceChange={(fractureExperience) => patchForm({ fractureExperience })} onTotalCountChange={(fractureTotalCount) => patchForm({ fractureTotalCount })} onCountUnknownChange={(fractureCountUnknown) => patchForm({ fractureCountUnknown, fractureTotalCount: fractureCountUnknown ? "" : form.fractureTotalCount })} onFracturesChange={(fractures) => patchForm({ fractures })} />}
    {step === 5 && <FoodStep meals={form.mealsPerDay} answers={form.foodAnswers} onMealsChange={(mealsPerDay) => patchForm({ mealsPerDay })} onAnswersChange={(foodAnswers) => patchForm({ foodAnswers })} />}
    {step === 6 && <ReviewStep form={form} />}
    {error && <div className="form-error" role="alert">{error}</div>}
    <footer className="form-actions">
      <button className="secondary" type="button" disabled={step === 0 || busy} onClick={() => move(step - 1)}>이전</button>
      <button className="save-button" type="button" disabled={busy} onClick={handleServerSave}>{busy ? "처리 중..." : "서버 임시 저장"}</button>
      {step < STEPS.length - 1 ? <button className="primary" type="button" disabled={busy} onClick={handleNext}>다음 단계 →</button> : <button className="primary" type="button" disabled={busy} onClick={handleSubmit}>{busy ? "제출 중..." : "설문 제출하기 ✓"}</button>}
    </footer>
  </SurveyLayout>;
}

function Completion({ response }: { response: SubmissionResponse }) {
  return <main className="completion"><article><b>✓</b><label>제출 완료</label><h1>설문에 참여해 주셔서<br />감사합니다.</h1><p>응답이 정상적으로 서버에 저장되었습니다.</p><div><small>제출번호</small><strong>{response.submissionNumber}</strong></div><button type="button" onClick={() => window.location.reload()}>처음 화면으로</button></article></main>;
}
