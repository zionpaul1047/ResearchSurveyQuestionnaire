import { Question, RadioChoice, SelectField, StepIntro, TextField } from "../components/FormUi";
import type { SurveyForm } from "../types";
import { CONTACT_TIME_OPTIONS, formatKoreanPhone, limitSingleLine, normalizeParticipantCode, todayLocal } from "../validation";

interface Props { form: SurveyForm; onChange: (patch: Partial<SurveyForm>) => void }

export default function ConsentStep({ form, onChange }: Props) {
  const setMetadata = (key: keyof SurveyForm["metadata"], value: string) => onChange({ metadata: { ...form.metadata, [key]: value } });

  return <>
    <StepIntro number={1} title="연구 참여 동의" description="안내사항을 충분히 읽고 이해하신 후 동의 여부를 선택해 주세요." />
    <div className="info-banner"><b>i</b><span><strong>연구 참여 안내</strong><p>이 화면은 설문지 Ver. 2.4를 구현한 개발·검증용 MVP입니다. 실제 운영 전에는 승인된 연구설명문과 개인정보 처리 내용을 연결해야 합니다.</p></span></div>
    <div className="privacy-banner"><strong>개인정보 입력 주의</strong><p>이름, 주민등록번호, 주소 전체를 입력하지 마세요. 연구진이 제공한 식별번호와 연락 가능한 전화번호만 입력할 수 있습니다.</p></div>
    <article className="agreement-card">
      <h2>연구 안내사항 확인</h2>
      <label className="check-line"><input type="checkbox" checked={form.privacyConsent} onChange={(event) => onChange({ privacyConsent: event.target.checked })} /><span>연구진이 제공한 안내사항을 충분히 읽고 이해했습니다. <em>(필수)</em></span></label>
    </article>
    <article className="agreement-card compact">
      <label className="check-line"><input type="checkbox" checked={form.contactConsent} onChange={(event) => onChange({ contactConsent: event.target.checked })} /><span>필요한 경우 연구진이 응답 확인을 위해 연락할 수 있음을 확인했습니다. <em>(필수)</em></span></label>
    </article>
    <Question code="조사정보" title="설문 기본정보를 확인해 주세요.">
      <div className="form-grid">
        <TextField label="연구대상자 식별번호 (연구진 제공 시)" value={form.metadata.participantCode} placeholder="예: RSQ-001" maxLength={40} autoComplete="off" help="영문 대문자·숫자·-·_만 입력할 수 있습니다. 이름이나 주민등록번호는 입력하지 마세요." onChange={(value) => setMetadata("participantCode", normalizeParticipantCode(value))} />
        <label className="form-field"><span>조사일</span><input className="field" aria-label="조사일" type="date" max={todayLocal()} value={form.metadata.surveyDate} onChange={(event) => setMetadata("surveyDate", event.target.value)} /><small className="field-help">미래 날짜는 선택할 수 없습니다.</small></label>
        <div className="form-field full"><span>조사 방법</span><div className="choices"><RadioChoice name="surveyMethod" value="ONLINE" current={form.metadata.surveyMethod} onChange={(value) => setMetadata("surveyMethod", value)}>온라인 설문</RadioChoice><RadioChoice name="surveyMethod" value="PHONE" current={form.metadata.surveyMethod} onChange={(value) => setMetadata("surveyMethod", value)}>전화 설문</RadioChoice><RadioChoice name="surveyMethod" value="OTHER" current={form.metadata.surveyMethod} onChange={(value) => setMetadata("surveyMethod", value)}>기타</RadioChoice></div></div>
        {form.metadata.surveyMethod === "OTHER" && <TextField label="기타 조사 방법" value={form.metadata.surveyMethodOther} maxLength={100} help="100자 이내로 입력해 주세요." onChange={(value) => setMetadata("surveyMethodOther", limitSingleLine(value, 100))} />}
        <TextField label="응답 확인용 연락처 (선택)" type="tel" inputMode="tel" autoComplete="tel" maxLength={13} value={form.metadata.contactPhone} placeholder="010-1234-5678" help="숫자를 입력하면 하이픈이 자동으로 추가됩니다." onChange={(value) => setMetadata("contactPhone", formatKoreanPhone(value))} />
        <label className="form-field"><span>통화 가능한 시간 (선택)</span><SelectField ariaLabel="통화 가능한 시간" value={form.metadata.contactTime} onChange={(value) => setMetadata("contactTime", value)}>{CONTACT_TIME_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}</SelectField></label>
      </div>
    </Question>
  </>;
}
