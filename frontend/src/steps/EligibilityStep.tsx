import { Question, RadioChoice, StepIntro } from "../components/FormUi";
import type { Eligibility } from "../types";
import { todayLocal } from "../validation";

interface Props { value: Eligibility; onChange: (value: Eligibility) => void; disqualified: boolean }

export default function EligibilityStep({ value, onChange, disqualified }: Props) {
  const set = (key: keyof Eligibility, next: string) => onChange({ ...value, [key]: next });
  return <>
    <StepIntro number={2} title="연구 참여 적격성 확인" description="연구 참여 가능 여부를 확인하기 위한 질문입니다." />
    {disqualified && <div className="warning-banner"><strong>현재 응답으로는 연구 참여가 어렵습니다.</strong><p>입력 내용을 다시 확인해 주세요. 정확한 안내가 필요하면 연구 담당자에게 문의해 주세요.</p></div>}
    <Question code="S1" title="본 연구 참여에 자발적으로 동의하십니까?">
      <div className="choices"><RadioChoice name="voluntary" value="YES" current={value.voluntaryConsent} onChange={(next) => set("voluntaryConsent", next)}>예, 동의합니다</RadioChoice><RadioChoice name="voluntary" value="NO" current={value.voluntaryConsent} onChange={(next) => set("voluntaryConsent", next)}>아니오</RadioChoice></div>
    </Question>
    <Question code="S2" title="귀하의 생년월일은 언제입니까?">
      <input className="field short" aria-label="생년월일" type="date" min="1900-01-01" max={todayLocal()} autoComplete="bday" value={value.birthDate} onChange={(event) => set("birthDate", event.target.value)} />
      <p className="field-help">입력한 생년월일과 만 55세 이상 응답이 일치하는지 자동 확인합니다.</p>
    </Question>
    <Question code="S3" title="현재 만 55세 이상입니까?">
      <div className="choices"><RadioChoice name="over55" value="YES" current={value.over55} onChange={(next) => set("over55", next)}>예</RadioChoice><RadioChoice name="over55" value="NO" current={value.over55} onChange={(next) => set("over55", next)}>아니오</RadioChoice></div>
    </Question>
    <Question code="S4" title="귀하의 출생 시 성별은 여성입니까?">
      <div className="choices"><RadioChoice name="female" value="YES" current={value.femaleAtBirth} onChange={(next) => set("femaleAtBirth", next)}>예</RadioChoice><RadioChoice name="female" value="NO" current={value.femaleAtBirth} onChange={(next) => set("femaleAtBirth", next)}>아니오</RadioChoice><RadioChoice name="female" value="DECLINE" current={value.femaleAtBirth} onChange={(next) => set("femaleAtBirth", next)}>응답 원하지 않음</RadioChoice></div>
    </Question>
    <Question code="S9" title="현재 중증 신장·간·내분비 질환을 진단받아 치료 또는 추적관찰 중입니까?">
      <div className="choices vertical"><RadioChoice name="disease" value="NONE" current={value.exclusionDisease} onChange={(next) => set("exclusionDisease", next)}>해당 없음</RadioChoice><RadioChoice name="disease" value="EXCLUSION" current={value.exclusionDisease} onChange={(next) => set("exclusionDisease", next)}>해당 질환이 있음</RadioChoice><RadioChoice name="disease" value="UNKNOWN" current={value.exclusionDisease} onChange={(next) => set("exclusionDisease", next)}>잘 모르겠음</RadioChoice></div>
    </Question>
  </>;
}
