import { Question, RadioChoice, SelectField, StepIntro, TextField } from "../components/FormUi";
import type { BasicInfo } from "../types";
import { limitSingleLine } from "../validation";

interface Props { value: BasicInfo; onChange: (value: BasicInfo) => void }

const regions = ["서울", "경기·인천", "강원", "충청", "전라", "경상", "제주", "기타"];

export default function BasicInfoStep({ value, onChange }: Props) {
  const set = (key: keyof BasicInfo, next: string) => onChange({ ...value, [key]: next });
  return <>
    <StepIntro number={3} title="일반적 특성 및 신체정보" description="현재 상태를 기준으로 응답해 주세요." />
    <Question code="A1" title="현재 거주지역은 어디입니까?">
      <div className="chip-grid">{regions.map((region) => <button type="button" key={region} className={value.region === region ? "selected" : ""} onClick={() => set("region", region)}>{region}</button>)}</div>
      {value.region === "기타" && <div className="conditional-field"><TextField label="기타 거주지역" value={value.regionOther} maxLength={100} help="시·도 또는 권역 수준으로 입력하고 상세 주소는 입력하지 마세요." onChange={(next) => set("regionOther", limitSingleLine(next, 100))} /></div>}
    </Question>
    <Question code="A2" title="최종 학력은 어떻게 되십니까?">
      <SelectField ariaLabel="최종 학력" value={value.education} onChange={(next) => set("education", next)}>{["초등학교 졸업 이하", "중학교 졸업", "고등학교 졸업", "대학교 졸업", "대학원 이상", "기타"].map((item) => <option key={item}>{item}</option>)}</SelectField>
      {value.education === "기타" && <div className="conditional-field"><TextField label="기타 최종 학력" value={value.educationOther} maxLength={100} help="학교명 등 개인을 식별할 수 있는 정보는 입력하지 마세요." onChange={(next) => set("educationOther", limitSingleLine(next, 100))} /></div>}
    </Question>
    <Question code="A3" title="월평균 가구소득은 어떻게 되십니까?">
      <SelectField ariaLabel="월평균 가구소득" value={value.householdIncome} onChange={(next) => set("householdIncome", next)}>{["200만원 미만", "200~400만원 미만", "400~600만원 미만", "600~800만원 미만", "800만원 이상", "기타"].map((item) => <option key={item}>{item}</option>)}</SelectField>
      {value.householdIncome === "기타" && <div className="conditional-field"><TextField label="기타 월평균 가구소득" value={value.householdIncomeOther} maxLength={100} help="금액 구간 또는 응답 불가 사유를 100자 이내로 입력해 주세요." onChange={(next) => set("householdIncomeOther", limitSingleLine(next, 100))} /></div>}
    </Question>
    <Question code="A4" title="현재 직업이 있습니까?">
      <div className="choices"><RadioChoice name="employed" value="YES" current={value.employed} onChange={(next) => set("employed", next)}>예</RadioChoice><RadioChoice name="employed" value="NO" current={value.employed} onChange={(next) => set("employed", next)}>아니오</RadioChoice><RadioChoice name="employed" value="OTHER" current={value.employed} onChange={(next) => set("employed", next)}>기타</RadioChoice></div>
      {value.employed === "OTHER" && <div className="conditional-field"><TextField label="기타 직업 상태" value={value.employmentOther} maxLength={100} help="직장명이나 사업장 주소는 입력하지 마세요." onChange={(next) => set("employmentOther", limitSingleLine(next, 100))} /></div>}
    </Question>
    <Question code="A5" title="현재 키와 체중은 어떻게 됩니까?">
      <div className="physical-fields"><label><span>키</span><input className="field" aria-label="키" type="number" inputMode="decimal" min="100" max="220" step="0.1" value={value.heightCm} onChange={(event) => set("heightCm", event.target.value)} /><i>cm</i></label><label><span>체중</span><input className="field" aria-label="체중" type="number" inputMode="decimal" min="20" max="250" step="0.1" value={value.weightKg} onChange={(event) => set("weightKg", event.target.value)} /><i>kg</i></label></div>
      <p className="field-help">소수점 첫째 자리까지만 입력할 수 있습니다.</p>
    </Question>
    <Question code="A6" title="폐경이 된 나이는 몇 세입니까?" required={false}>
      <div className="inline-response"><input className="field tiny" aria-label="폐경 나이" type="number" inputMode="numeric" min="20" max="80" step="1" placeholder="나이" disabled={value.menopauseAgeUnknown} value={value.menopauseAge} onChange={(event) => set("menopauseAge", event.target.value)} /><label className="check-line"><input type="checkbox" checked={value.menopauseAgeUnknown} onChange={(event) => onChange({ ...value, menopauseAgeUnknown: event.target.checked, menopauseAge: event.target.checked ? "" : value.menopauseAge })} /><span>잘 모르겠음</span></label></div>
    </Question>
    <Question code="A7" title="여성호르몬 치료를 받은 적이 있습니까?">
      <SelectField ariaLabel="여성호르몬 치료 여부" value={value.hormoneTreatment} onChange={(next) => set("hormoneTreatment", next)}>{["현재 치료 중", "과거 치료, 현재 중단", "치료받은 적 없음", "잘 모르겠음"].map((item) => <option key={item}>{item}</option>)}</SelectField>
    </Question>
    {["현재 치료 중", "과거 치료, 현재 중단"].includes(value.hormoneTreatment) && <Question code="A7-1" title="여성호르몬 치료를 받은 총 기간은 얼마나 됩니까?">
      <div className="inline-response"><label className="unit-field"><input className="field tiny" aria-label="여성호르몬 치료 연수" type="number" inputMode="numeric" min="0" max="60" step="1" disabled={value.hormoneDurationUnknown} value={value.hormoneDurationYears} onChange={(event) => set("hormoneDurationYears", event.target.value)} /><span>년</span></label><label className="unit-field"><input className="field tiny" aria-label="여성호르몬 치료 개월" type="number" inputMode="numeric" min="0" max="11" step="1" disabled={value.hormoneDurationUnknown} value={value.hormoneDurationMonths} onChange={(event) => set("hormoneDurationMonths", event.target.value)} /><span>개월</span></label><label className="check-line"><input type="checkbox" checked={value.hormoneDurationUnknown} onChange={(event) => onChange({ ...value, hormoneDurationUnknown: event.target.checked, hormoneDurationYears: event.target.checked ? "" : value.hormoneDurationYears, hormoneDurationMonths: event.target.checked ? "" : value.hormoneDurationMonths })} /><span>잘 모르겠음</span></label></div>
    </Question>}
  </>;
}
