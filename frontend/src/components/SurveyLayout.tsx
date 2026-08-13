import type { ReactNode } from "react";

interface Props {
  step: number;
  steps: string[];
  saveMessage: string;
  children: ReactNode;
  onMove: (step: number) => void;
}

export default function SurveyLayout({ step, steps, saveMessage, children, onMove }: Props) {
  const progress = Math.round(((step + 1) / steps.length) * 100);

  return (
    <main>
      <header className="topbar">
        <div className="brand">
          <b>R</b>
          <span><strong>건강연구 설문</strong><small>연구대상자용 · Ver. 2.4</small></span>
        </div>
        <p><i />{saveMessage || "보안 연결 사용 중"}</p>
      </header>
      <div className="demo-environment-banner" role="note">
        <strong>데모 확인용 화면</strong>
        <span>실제 이름·연락처·생년월일·건강정보를 입력하지 말고 임의의 테스트 값만 사용해 주세요.</span>
      </div>
      <div className="page-layout">
        <aside className="sidebar">
          <label>설문 진행</label>
          <div className="progress-caption"><strong>{progress}%</strong><span>{step + 1} / {steps.length} 단계</span></div>
          <div className="progress-track"><i style={{ width: `${progress}%` }} /></div>
          <nav>
            {steps.map((name, index) => (
              <button key={name} className={index === step ? "active" : index < step ? "done" : ""} disabled={index > step} onClick={() => index < step && onMove(index)}>
                <b>{index < step ? "✓" : index + 1}</b>{name}
              </button>
            ))}
          </nav>
          <div className="help-card"><strong>도움이 필요하신가요?</strong><p>설문 응답 중 궁금한 점은 연구진이 제공한 안내문의 담당자 연락처를 확인해 주세요.</p></div>
        </aside>
        <section className="survey-content">
          <div className="mobile-progress"><span>{step + 1}. {steps[step]}</span><b>{progress}%</b></div>
          {children}
        </section>
      </div>
    </main>
  );
}
