import { StepIntro } from "../components/FormUi";
import type { SurveyForm } from "../types";

export default function ReviewStep({ form }: { form: SurveyForm }) {
  const summaries = [
    ["적격성", form.eligibility.over55 === "YES" ? "참여 조건 확인" : "확인 필요"],
    ["신체정보", form.basicInfo.heightCm && form.basicInfo.weightKg ? `${form.basicInfo.heightCm}cm · ${form.basicInfo.weightKg}kg` : "미입력"],
    ["제품 섭취", form.productExperience === "REGULAR" ? `${form.products.length}개 제품 입력` : "섭취 이력 없음"],
    ["골절 경험", form.fractureExperience === "YES" ? `${form.fractureCountUnknown ? "총 건수 미상" : `총 ${form.fractureTotalCount}건`} · 상세 ${form.fractures.length}건` : "골절 이력 없음"],
    ["평균 식사", form.mealsPerDay || "미입력"],
    ["식품 문항", `${form.foodAnswers.filter((item) => item.frequency).length} / 3개 응답`]
  ];
  return <>
    <StepIntro number={7} title="응답 확인 및 제출" description="제출 후에는 직접 수정할 수 없습니다. 입력 내용을 확인해 주세요." />
    <div className="summary-grid">{summaries.map(([title, value]) => <article className="summary-card" key={title}><span>{title}</span><strong>{value}</strong><i>✓</i></article>)}</div>
    <div className="final-notice"><strong>제출 전 확인해 주세요</strong><p>최종 제출 후에는 화면에서 직접 수정할 수 없습니다. 제출번호를 기록해 두고, 정정이 필요하면 연구 담당자에게 문의해 주세요.</p></div>
  </>;
}
