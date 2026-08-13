import { Question, RadioChoice, RepeatedCard, SectionHeading, SelectField, StepIntro } from "../components/FormUi";
import { newFracture, type FractureHistory, type YesNoUnknown } from "../types";
import { currentMonthLocal, monthMinus } from "../validation";

interface Props {
  experience: YesNoUnknown;
  surveyDate: string;
  totalCount: string;
  countUnknown: boolean;
  fractures: FractureHistory[];
  showProductTiming: boolean;
  onExperienceChange: (value: YesNoUnknown) => void;
  onTotalCountChange: (value: string) => void;
  onCountUnknownChange: (value: boolean) => void;
  onFracturesChange: (value: FractureHistory[]) => void;
}

export default function FractureStep({ experience, surveyDate, totalCount, countUnknown, fractures, showProductTiming, onExperienceChange, onTotalCountChange, onCountUnknownChange, onFracturesChange }: Props) {
  const latestMonth = surveyDate ? surveyDate.slice(0, 7) : currentMonthLocal();
  const earliestMonth = monthMinus(surveyDate, 24);
  const update = (index: number, patch: Partial<FractureHistory>) => onFracturesChange(fractures.map((item, itemIndex) => itemIndex === index ? { ...item, ...patch } : item));
  const updateTotalCount = (value: string) => {
    onTotalCountChange(value);
    const parsed = Number(value);
    if (!Number.isInteger(parsed) || parsed < 1) return;
    const targetCount = Math.min(parsed, 3);
    if (fractures.length < targetCount) onFracturesChange([...fractures, ...Array.from({ length: targetCount - fractures.length }, newFracture)]);
    if (fractures.length > targetCount) onFracturesChange(fractures.slice(0, targetCount));
  };
  return <>
    <StepIntro number={5} title="최근 24개월 이내 골절 경험" description="같은 사고로 여러 부위가 골절된 경우에는 1건으로 입력해 주세요." />
    <Question code="C1" title="조사일 이전 24개월 이내 의료기관에서 골절로 진단받은 적이 있습니까?">
      <div className="choices"><RadioChoice name="fractureExperience" value="YES" current={experience} onChange={(value) => onExperienceChange(value as YesNoUnknown)}>예</RadioChoice><RadioChoice name="fractureExperience" value="NO" current={experience} onChange={(value) => onExperienceChange(value as YesNoUnknown)}>아니오</RadioChoice><RadioChoice name="fractureExperience" value="UNKNOWN" current={experience} onChange={(value) => onExperienceChange(value as YesNoUnknown)}>잘 모르겠음</RadioChoice></div>
    </Question>
    {experience === "YES" && <>
      <Question code="C2" title="조사일 이전 24개월 이내 발생한 골절은 총 몇 건입니까?">
        <div className="inline-response"><label className="unit-field"><input className="field tiny" aria-label="골절 총 건수" type="number" inputMode="numeric" min="1" max="99" step="1" disabled={countUnknown} value={totalCount} onChange={(event) => updateTotalCount(event.target.value)} /><span>건</span></label><label className="check-line"><input type="checkbox" checked={countUnknown} onChange={(event) => onCountUnknownChange(event.target.checked)} /><span>잘 모르겠음</span></label></div>
        <p className="field-help">3건 이상이면 가장 최근 골절 3건만 아래에 기록합니다.</p>
      </Question>
      <SectionHeading title="골절 상세정보" description="최대 3건까지 가장 최근 골절부터 입력해 주세요." buttonLabel="골절 추가" disabled={fractures.length >= 3} onAdd={() => fractures.length < 3 && onFracturesChange([...fractures, newFracture()])} />
      {fractures.map((item, index) => <RepeatedCard key={item.clientId} title={`골절 ${index + 1}${index === 0 ? " · 가장 최근" : ""}`} onRemove={fractures.length > 1 ? () => onFracturesChange(fractures.filter((_, itemIndex) => itemIndex !== index)) : undefined}>
        <div className="form-grid">
          <label className="form-field"><span>발생 연월</span><input className="field" aria-label={`골절 ${index + 1} 발생 연월`} type="month" min={earliestMonth} max={latestMonth} value={item.occurredYearMonth} onChange={(event) => update(index, { occurredYearMonth: event.target.value })} /><small className="field-help">{earliestMonth}~{latestMonth} 범위</small></label>
          <label className="form-field"><span>골절 부위</span><SelectField ariaLabel={`골절 ${index + 1} 부위`} value={item.fractureSite} onChange={(value) => update(index, { fractureSite: value })}>{["척추", "고관절", "손목", "상완", "발목", "갈비뼈", "기타"].map((option) => <option key={option}>{option}</option>)}</SelectField></label>
          <label className="form-field"><span>당시 낙상 여부</span><SelectField ariaLabel={`골절 ${index + 1} 낙상 여부`} value={item.fallRelated} onChange={(value) => update(index, { fallRelated: value as YesNoUnknown })}><option value="YES">예</option><option value="NO">아니오</option><option value="UNKNOWN">잘 모르겠음</option></SelectField></label>
          <label className="form-field"><span>주된 발생 원인</span><SelectField ariaLabel={`골절 ${index + 1} 발생 원인`} value={item.primaryCause} onChange={(value) => update(index, { primaryCause: value })}>{["서 있거나 걷다가 넘어짐", "계단에서 넘어짐", "침대·의자 등 낮은 위치에서 떨어짐", "일상생활 중 가벼운 충돌", "일반적인 운동이나 신체활동 중 발생", "교통사고", "사다리·건물 등 높은 위치에서 추락", "의사에게 병적 골절이라고 설명받음", "기타", "잘 모르겠음"].map((option) => <option key={option}>{option}</option>)}</SelectField></label>
          <label className="form-field"><span>기록 확인 가능 여부</span><SelectField ariaLabel={`골절 ${index + 1} 기록 확인`} value={item.recordAvailability} onChange={(value) => update(index, { recordAvailability: value })}>{["진단서·진료기록 확인 가능", "병원명·진료일 확인 가능", "확인 가능한 기록 없음", "잘 모르겠음"].map((option) => <option key={option}>{option}</option>)}</SelectField></label>
          <label className="form-field"><span>입원·수술</span><SelectField ariaLabel={`골절 ${index + 1} 입원 수술`} value={item.treatment} onChange={(value) => update(index, { treatment: value })}>{["입원", "수술", "둘 다", "해당 없음"].map((option) => <option key={option}>{option}</option>)}</SelectField></label>
          {showProductTiming && <label className="form-field full"><span>제품 섭취 시점과의 관계</span><SelectField ariaLabel={`골절 ${index + 1} 제품 섭취 시점`} value={item.timingRelativeToProduct} onChange={(value) => update(index, { timingRelativeToProduct: value })}>{["제품 섭취 시작 전", "섭취 시작 후 6개월 이내", "6개월 초과~12개월 이내", "12개월 초과", "일시 중단 기간 중", "완전 중단 후", "잘 모르겠음"].map((option) => <option key={option}>{option}</option>)}</SelectField></label>}
        </div>
      </RepeatedCard>)}
    </>}
  </>;
}
