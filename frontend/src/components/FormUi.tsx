import type { ChangeEvent, HTMLInputTypeAttribute, ReactNode } from "react";

export function StepIntro({ number, title, description }: { number: number; title: string; description: string }) {
  return <div className="step-intro"><label>STEP {number}</label><h1>{title}</h1><p>{description}</p></div>;
}

export function Question({ code, title, children, required = true }: { code: string; title: string; children: ReactNode; required?: boolean }) {
  return <article className="question-card"><header><b>{code}</b><h2>{title}</h2>{required && <em>필수</em>}</header>{children}</article>;
}

export function RadioChoice({ name, value, current, onChange, children }: { name: string; value: string; current: string; onChange: (value: string) => void; children: ReactNode }) {
  return <label className={`radio-choice ${current === value ? "selected" : ""}`}><input type="radio" name={name} value={value} checked={current === value} onChange={() => onChange(value)} /><i />{children}</label>;
}

export function SelectField({ value, onChange, children, ariaLabel }: { value: string; onChange: (value: string) => void; children: ReactNode; ariaLabel: string }) {
  return <select className="field" aria-label={ariaLabel} value={value} onChange={(event: ChangeEvent<HTMLSelectElement>) => onChange(event.target.value)}><option value="">선택해 주세요</option>{children}</select>;
}

interface TextFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  type?: HTMLInputTypeAttribute;
  placeholder?: string;
  maxLength?: number;
  min?: string | number;
  max?: string | number;
  step?: string | number;
  inputMode?: "none" | "text" | "tel" | "url" | "email" | "numeric" | "decimal" | "search";
  autoComplete?: string;
  help?: string;
}

export function TextField({ label, value, onChange, type = "text", placeholder = "", maxLength, min, max, step, inputMode, autoComplete, help }: TextFieldProps) {
  return <label className="form-field"><span>{label}</span><input className="field" type={type} value={value} placeholder={placeholder} maxLength={maxLength} min={min} max={max} step={step} inputMode={inputMode} autoComplete={autoComplete} onChange={(event) => onChange(event.target.value)} />{help && <small className="field-help">{help}</small>}</label>;
}

export function RepeatedCard({ title, onRemove, children }: { title: string; onRemove?: () => void; children: ReactNode }) {
  return <article className="repeat-card"><header><strong>{title}</strong>{onRemove && <button type="button" onClick={onRemove}>삭제</button>}</header>{children}</article>;
}

export function SectionHeading({ title, description, buttonLabel, onAdd, disabled = false }: { title: string; description: string; buttonLabel: string; onAdd: () => void; disabled?: boolean }) {
  return <div className="section-heading"><span><h2>{title}</h2><p>{description}</p></span><button type="button" onClick={onAdd} disabled={disabled}>＋ {buttonLabel}</button></div>;
}
