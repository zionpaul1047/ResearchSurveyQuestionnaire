import { Question, RadioChoice, StepIntro } from "../components/FormUi";
import type { Eligibility } from "../types";
import { ageOnDate, todayLocal } from "../validation";

interface Props {
  value: Eligibility;
  surveyDate: string;
  age: number | null;
  disqualificationReasons: string[];
  onChange: (value: Eligibility) => void;
}

function formatKoreanDate(value: string): string {
  if (!value) return "조사일 미입력";
  const [year, month, day] = value.split("-");
  return `${year}.${month}.${day}`;
}

export default function EligibilityStep({ value, surveyDate, age, disqualificationReasons, onChange }: Props) {
  const set = (key: keyof Eligibility, next: string) => onChange({ ...value, [key]: next });
  const setBirthDate = (birthDate: string) => {
    const calculatedAge = ageOnDate(birthDate, surveyDate);
    onChange({
      ...value,
      birthDate,
      over55: calculatedAge === null ? "" : calculatedAge >= 55 ? "YES" : "NO",
    });
  };

  return <>
    <StepIntro number={2} title="연구 참여 적격성 확인" description="생년월일을 입력하면 조사일 기준 만 나이를 자동 계산하고 참여 조건을 안내합니다." />
    <div className="eligibility-guide" role="note">
      <strong>입력 안내</strong>
      <ul>
        <li>생년월일은 신분증에 표시된 날짜와 같은 형식으로 선택해 주세요.</li>
        <li>만 나이는 조사일 <b>{formatKoreanDate(surveyDate)}</b> 기준으로 자동 계산됩니다.</li>
        <li>입력이 정확한데 참여가 어렵다고 표시되면 값을 바꾸지 말고 연구 담당자에게 문의해 주세요.</li>
      </ul>
    </div>
    {disqualificationReasons.length > 0 && <div className="warning-banner eligibility-warning" role="alert" aria-live="polite">
      <strong>현재 입력 기준으로 연구 참여 확인이 필요합니다.</strong>
      <ul>{disqualificationReasons.map((reason) => <li key={reason}>{reason}</li>)}</ul>
      <p>입력 내용을 다시 확인해 주세요. 입력이 정확하다면 연구 담당자에게 문의해 주세요.</p>
    </div>}
    <Question code="S1" title="본 연구 참여에 자발적으로 동의하십니까?">
      <div className="choices"><RadioChoice name="voluntary" value="YES" current={value.voluntaryConsent} onChange={(next) => set("voluntaryConsent", next)}>예, 동의합니다</RadioChoice><RadioChoice name="voluntary" value="NO" current={value.voluntaryConsent} onChange={(next) => set("voluntaryConsent", next)}>아니오</RadioChoice></div>
    </Question>
    <Question code="S2" title="귀하의 생년월일은 언제입니까?">
      <input className="field short" aria-label="생년월일" aria-describedby="birth-date-help" type="date" min="1900-01-01" max={todayLocal()} autoComplete="bday" value={value.birthDate} onChange={(event) => setBirthDate(event.target.value)} />
      <p className="field-help" id="birth-date-help"><b>입력 예시: 1965.03.15.</b> 날짜를 선택하면 아래 연령 판정이 자동으로 바뀝니다.</p>
    </Question>
    <Question code="S3" title="조사일 기준 연령 판정">
      <div className={`age-assessment ${age === null ? "pending" : age >= 55 ? "eligible" : "ineligible"}`} role="status" aria-live="polite">
        {age === null ? <>
          <strong>생년월일을 먼저 선택해 주세요.</strong>
          <p>조사일 기준 만 나이와 55세 이상 여부가 여기에 자동으로 표시됩니다.</p>
        </> : <>
          <div><strong>만 {age}세</strong><span>{age >= 55 ? "55세 이상 조건 충족" : "55세 이상 조건 미충족"}</span></div>
          <p>생년월일과 조사일을 기준으로 자동 계산된 결과이며 직접 수정할 수 없습니다.</p>
        </>}
      </div>
    </Question>
    <Question code="S4" title="귀하의 출생 시 성별은 여성입니까?">
      <div className="choices"><RadioChoice name="female" value="YES" current={value.femaleAtBirth} onChange={(next) => set("femaleAtBirth", next)}>예</RadioChoice><RadioChoice name="female" value="NO" current={value.femaleAtBirth} onChange={(next) => set("femaleAtBirth", next)}>아니오</RadioChoice><RadioChoice name="female" value="DECLINE" current={value.femaleAtBirth} onChange={(next) => set("femaleAtBirth", next)}>응답 원하지 않음</RadioChoice></div>
    </Question>
    <Question code="S9" title="현재 중증 신장·간·내분비 질환을 진단받아 치료 또는 추적관찰 중입니까?">
      <div className="choices vertical"><RadioChoice name="disease" value="NONE" current={value.exclusionDisease} onChange={(next) => set("exclusionDisease", next)}>해당 없음</RadioChoice><RadioChoice name="disease" value="EXCLUSION" current={value.exclusionDisease} onChange={(next) => set("exclusionDisease", next)}>해당 질환이 있음</RadioChoice><RadioChoice name="disease" value="UNKNOWN" current={value.exclusionDisease} onChange={(next) => set("exclusionDisease", next)}>잘 모르겠음</RadioChoice></div>
    </Question>
  </>;
}
