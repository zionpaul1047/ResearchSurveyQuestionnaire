import { Question, RadioChoice, RepeatedCard, SectionHeading, SelectField, StepIntro, TextField } from "../components/FormUi";
import { newProduct, type ProductDetails, type ProductHistory } from "../types";
import { currentMonthLocal, limitMultiline, limitSingleLine, PRODUCT_FREQUENCIES } from "../validation";

interface Props {
  experience: string;
  surveyDate: string;
  details: ProductDetails;
  products: ProductHistory[];
  purchaseEvidenceFiles: File[];
  productPhotoFiles: File[];
  onExperienceChange: (value: string) => void;
  onDetailsChange: (value: ProductDetails) => void;
  onProductsChange: (value: ProductHistory[]) => void;
  onPurchaseEvidenceFilesChange: (files: File[]) => void;
  onProductPhotoFilesChange: (files: File[]) => void;
}

const ingredientOptions = ["칼슘", "비타민D", "복합제", "기타"];
const reasonOptions = [
  ["1", "골다공증·골감소증 진단"],
  ["2", "골밀도 저하 또는 건강검진 결과"],
  ["3", "과거 골절 또는 낙상 경험"],
  ["4", "의사·약사 등 전문가 권유"],
  ["5", "가족·지인 권유"],
  ["6", "광고·제품정보를 보고 예방 목적"],
  ["7", "기타"],
  ["8", "잘 모르겠음"]
];

export default function ProductStep({ experience, surveyDate, details, products, purchaseEvidenceFiles, productPhotoFiles, onExperienceChange, onDetailsChange, onProductsChange, onPurchaseEvidenceFilesChange, onProductPhotoFilesChange }: Props) {
  const latestMonth = surveyDate ? surveyDate.slice(0, 7) : currentMonthLocal();
  const latestYear = Number(latestMonth.slice(0, 4));
  const setDetails = <K extends keyof ProductDetails>(key: K, value: ProductDetails[K]) => onDetailsChange({ ...details, [key]: value });
  const update = (index: number, patch: Partial<ProductHistory>) => onProductsChange(products.map((item, itemIndex) => itemIndex === index ? { ...item, ...patch } : item));
  const toggleIngredient = (index: number, ingredient: string) => {
    const current = products[index].ingredients;
    update(index, { ingredients: current.includes(ingredient) ? current.filter((item) => item !== ingredient) : [...current, ingredient] });
  };
  const toggleReason = (reason: string) => {
    const removing = details.startReasons.includes(reason);
    const startReasons = removing
      ? details.startReasons.filter((item) => item !== reason)
      : reason === "8" ? ["8"] : [...details.startReasons.filter((item) => item !== "8"), reason];
    onDetailsChange({ ...details, startReasons, priorityReasons: details.priorityReasons.filter((item) => startReasons.includes(item)) });
  };
  const setPriority = (index: number, reason: string) => {
    const priorities = [...details.priorityReasons];
    priorities[index] = reason;
    setDetails("priorityReasons", priorities.filter(Boolean));
  };

  return <>
    <StepIntro number={4} title="제품 섭취" description="제품 섭취 시작 시점과 중단 이력, 확인 가능한 자료를 알려주세요." />
    <Question code="B1" title="제품을 한 번이라도 섭취한 적이 있습니까?">
      <div className="choices vertical"><RadioChoice name="productExperience" value="REGULAR" current={experience} onChange={onExperienceChange}>예, 홍보용 샘플 이외의 제품을 섭취한 적이 있음</RadioChoice><RadioChoice name="productExperience" value="SAMPLE_ONLY" current={experience} onChange={onExperienceChange}>홍보용 샘플만 섭취함</RadioChoice><RadioChoice name="productExperience" value="NEVER" current={experience} onChange={onExperienceChange}>섭취한 적 없음</RadioChoice><RadioChoice name="productExperience" value="UNKNOWN" current={experience} onChange={onExperienceChange}>잘 모르겠음</RadioChoice></div>
    </Question>
    {experience === "UNKNOWN" && <div className="warning-banner"><strong>섭취 제품 확인이 필요합니다.</strong><p>원본 설문 흐름에 따라 제품을 확인한 뒤 다시 응답해 주세요.</p></div>}
    {["SAMPLE_ONLY", "NEVER"].includes(experience) && <div className="info-banner"><b>i</b><span><strong>제품 상세 문항을 건너뜁니다.</strong><p>원본 설문지의 B11 문항은 제공본에 없어 이번 MVP에서는 다음 설문 단계로 이동합니다.</p></span></div>}
    {experience === "REGULAR" && <>
      <Question code="B2" title="현재도 제품을 섭취하고 있습니까?">
        <div className="choices vertical"><RadioChoice name="productCurrentStatus" value="REGULAR" current={details.currentStatus} onChange={(value) => setDetails("currentStatus", value)}>예, 정기적으로</RadioChoice><RadioChoice name="productCurrentStatus" value="INTERMITTENT" current={details.currentStatus} onChange={(value) => setDetails("currentStatus", value)}>예, 간헐적으로</RadioChoice><RadioChoice name="productCurrentStatus" value="STOPPED" current={details.currentStatus} onChange={(value) => setDetails("currentStatus", value)}>아니오, 과거에는 섭취했으나 현재 중단함</RadioChoice></div>
      </Question>
      <Question code="B3" title="제품을 처음 섭취하기 시작한 시점은 언제입니까?">
        <div className="choices"><RadioChoice name="productStartRecall" value="YEAR_MONTH" current={details.startRecall} onChange={(value) => setDetails("startRecall", value)}>연도와 월을 기억함</RadioChoice><RadioChoice name="productStartRecall" value="YEAR_ONLY" current={details.startRecall} onChange={(value) => setDetails("startRecall", value)}>연도만 기억함</RadioChoice><RadioChoice name="productStartRecall" value="UNKNOWN" current={details.startRecall} onChange={(value) => setDetails("startRecall", value)}>전혀 기억나지 않음</RadioChoice></div>
        {details.startRecall === "YEAR_MONTH" && <div className="conditional-field"><label className="form-field"><span>최초 섭취 연월</span><input className="field short" aria-label="최초 섭취 연월" type="month" min="1900-01" max={latestMonth} value={details.startYearMonth} onChange={(event) => setDetails("startYearMonth", event.target.value)} /><small className="field-help">조사월 이후 날짜는 선택할 수 없습니다.</small></label></div>}
        {details.startRecall === "YEAR_ONLY" && <div className="conditional-field"><TextField label="최초 섭취 연도" type="number" inputMode="numeric" min={1900} max={latestYear} step={1} value={details.startYear} placeholder="예: 2023" help={`1900~${latestYear}년 범위의 네 자리 연도`} onChange={(value) => setDetails("startYear", value)} /></div>}
      </Question>
      <Question code="B4" title="제품 섭취를 처음 시작한 이유를 모두 선택해 주세요.">
        <div className="checkbox-list">{reasonOptions.map(([code, label]) => <label key={code} className={details.startReasons.includes(code) ? "selected" : ""}><input type="checkbox" checked={details.startReasons.includes(code)} onChange={() => toggleReason(code)} /><b>{code}</b><span>{label}</span></label>)}</div>
        {details.startReasons.includes("7") && <div className="conditional-field"><TextField label="기타 시작 이유" value={details.startReasonOther} maxLength={500} help="개인·의료기관 이름을 제외하고 500자 이내로 입력해 주세요." onChange={(value) => setDetails("startReasonOther", limitSingleLine(value, 500))} /></div>}
      </Question>
      {details.startReasons.some((reason) => reason !== "8") && <Question code="B4-1" title="가장 중요했던 이유를 순서대로 최대 3개 선택해 주세요.">
        <div className="priority-grid">{[0, 1, 2].map((index) => <label className="form-field" key={index}><span>{index + 1}순위{index > 0 ? " (선택)" : ""}</span><select className="field" value={details.priorityReasons[index] ?? ""} onChange={(event) => setPriority(index, event.target.value)}><option value="">선택해 주세요</option>{reasonOptions.filter(([code]) => details.startReasons.includes(code) && code !== "8").map(([code, label]) => <option key={code} value={code}>{code}. {label}</option>)}</select></label>)}</div>
      </Question>}
      <Question code="B5" title="제품 섭취를 연속하여 1개월 이상 중단한 적이 있습니까?">
        <div className="choices"><RadioChoice name="interruptionStatus" value="NONE" current={details.interruptionStatus} onChange={(value) => setDetails("interruptionStatus", value)}>없음, 계속 섭취함</RadioChoice><RadioChoice name="interruptionStatus" value="YES" current={details.interruptionStatus} onChange={(value) => setDetails("interruptionStatus", value)}>있음</RadioChoice><RadioChoice name="interruptionStatus" value="UNKNOWN" current={details.interruptionStatus} onChange={(value) => setDetails("interruptionStatus", value)}>잘 모르겠음</RadioChoice></div>
      </Question>
      {details.interruptionStatus === "YES" && <Question code="B6" title="제품의 중단 및 재섭취 이력을 적어주세요.">
        <label className="form-field"><span>정확한 월을 모르면 대략적인 연도와 기간을 적어도 됩니다.</span><textarea className="field textarea" maxLength={2000} value={details.interruptionDetails} placeholder="예: 2024년 3월~5월 1차 중단, 2025년 1월부터 다시 섭취하지 않음" onChange={(event) => setDetails("interruptionDetails", limitMultiline(event.target.value, 2000))} /><small className="field-help">이름·연락처·병원명은 제외하고 2,000자 이내로 입력해 주세요. {details.interruptionDetails.length}/2,000</small></label>
      </Question>}

      <SectionHeading title="섭취 제품 정보 (B12 형식)" description="제품이 여러 개인 경우 각각 입력해 주세요. 최대 20개까지 등록할 수 있습니다." buttonLabel="제품 추가" disabled={products.length >= 20} onAdd={() => products.length < 20 && onProductsChange([...products, newProduct()])} />
      {products.map((item, index) => <RepeatedCard key={item.clientId} title={`제품 ${index + 1}`} onRemove={products.length > 1 ? () => onProductsChange(products.filter((_, itemIndex) => itemIndex !== index)) : undefined}>
        <div className="form-grid">
          <TextField label="제품명 또는 제품 종류" value={item.productName} placeholder="예: 칼슘 영양제" maxLength={200} help="200자 이내로 입력해 주세요." onChange={(value) => update(index, { productName: limitSingleLine(value, 200) })} />
          <div className="form-field"><span>주요 성분</span><div className="checkbox-chips">{ingredientOptions.map((ingredient) => <label key={ingredient} className={item.ingredients.includes(ingredient) ? "selected" : ""}><input type="checkbox" checked={item.ingredients.includes(ingredient)} onChange={() => toggleIngredient(index, ingredient)} />{ingredient}</label>)}</div></div>
          <TextField label="실제 총 섭취기간(개월)" type="number" inputMode="numeric" min={0} max={1200} step={1} value={item.totalIntakeMonths} help="0~1,200개월의 정수로 입력해 주세요." onChange={(value) => update(index, { totalIntakeMonths: value })} />
          <label className="form-field"><span>평균 섭취빈도</span><SelectField ariaLabel={`제품 ${index + 1} 평균 섭취빈도`} value={item.averageFrequency} onChange={(value) => update(index, { averageFrequency: value })}>{PRODUCT_FREQUENCIES.map((frequency) => <option key={frequency}>{frequency}</option>)}</SelectField></label>
          <label className="form-field"><span>현재 섭취 여부</span><SelectField ariaLabel={`제품 ${index + 1} 현재 섭취 여부`} value={item.currentlyTaking} onChange={(value) => update(index, { currentlyTaking: value as ProductHistory["currentlyTaking"] })}><option value="YES">예</option><option value="NO">아니오</option><option value="UNKNOWN">잘 모르겠음</option></SelectField></label>
        </div>
      </RepeatedCard>)}

      <FileUpload title="구매·수령 자료 (선택)" description="제품명과 구매·수령일이 보이는 구매내역, 영수증 또는 배송문자" files={purchaseEvidenceFiles} onChange={onPurchaseEvidenceFilesChange} />
      <FileUpload title="제품 확인 사진 (선택)" description="제품 앞면과 제품명·제조사·성분·소비기한 등이 보이는 표시사항" files={productPhotoFiles} onChange={onProductPhotoFilesChange} />

      <Question code="B10" title="확인 가능한 구매·수령 이력의 기간은 언제부터 언제까지입니까?" required={false}>
        <SelectField ariaLabel="구매 수령 이력 확인 범위" value={details.evidencePeriodType} onChange={(value) => setDetails("evidencePeriodType", value)}><option value="BOTH">시작과 종료 시점 모두 확인 가능</option><option value="START_ONLY">시작 시점만 확인 가능</option><option value="END_ONLY">종료 시점만 확인 가능</option><option value="PARTIAL">일부 구매·수령 이력만 확인 가능</option><option value="NONE">확인하지 못함 또는 해당 없음</option></SelectField>
        <div className="form-grid conditional-field">
          {["BOTH", "START_ONLY"].includes(details.evidencePeriodType) && <label className="form-field"><span>확인 가능한 시작 연월</span><input className="field" aria-label="구매 수령 이력 시작 연월" type="month" min="1900-01" max={latestMonth} value={details.evidenceStartYearMonth} onChange={(event) => setDetails("evidenceStartYearMonth", event.target.value)} /></label>}
          {["BOTH", "END_ONLY"].includes(details.evidencePeriodType) && <label className="form-field"><span>확인 가능한 종료 연월</span><input className="field" aria-label="구매 수령 이력 종료 연월" type="month" min="1900-01" max={latestMonth} value={details.evidenceEndYearMonth} onChange={(event) => setDetails("evidenceEndYearMonth", event.target.value)} /></label>}
        </div>
      </Question>
    </>}
  </>;
}

function FileUpload({ title, description, files, onChange }: { title: string; description: string; files: File[]; onChange: (files: File[]) => void }) {
  return <label className="upload-zone">
    <input type="file" accept="image/jpeg,image/png,image/webp" multiple onChange={(event) => onChange(Array.from(event.target.files ?? []))} />
    <b>↑</b><strong>{title}</strong><small>{description}</small><small>JPG, PNG, WEBP · 파일당 최대 10MB · 분류별 최대 10개</small>
    {files.length > 0 && <em>{files.length}개 파일 선택됨</em>}
  </label>;
}
