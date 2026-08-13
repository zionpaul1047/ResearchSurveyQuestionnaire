import { Question, StepIntro } from "../components/FormUi";
import { SOURCE_FOOD_ITEMS, type FoodAnswer } from "../types";

interface Props { meals: string; answers: FoodAnswer[]; onMealsChange: (value: string) => void; onAnswersChange: (value: FoodAnswer[]) => void }

const frequencies = ["거의 안 먹음", "월 1회", "월 2~3회", "주 1회", "주 2~4회", "주 5~6회", "일 1회", "일 2회", "일 3회"];

export default function FoodStep({ meals, answers, onMealsChange, onAnswersChange }: Props) {
  const answerFor = (code: string, name: string) => answers.find((item) => item.foodCode === code) ?? { foodCode: code, foodName: name, frequency: "", amount: "" };
  const update = (code: string, name: string, patch: Partial<FoodAnswer>) => {
    const current = answerFor(code, name);
    const next = { ...current, ...patch };
    onAnswersChange([...answers.filter((item) => item.foodCode !== code), next]);
  };
  return <>
    <StepIntro number={6} title="식품섭취빈도조사" description="지난 1년 동안의 평균적인 섭취 습관을 기준으로 응답해 주세요." />
    <Question code="H1" title="지난 1년 동안 하루에 평균적으로 몇 끼 식사하셨습니까?">
      <div className="chip-grid five">{["한 끼", "두 끼", "세 끼", "네 끼", "다섯 끼 이상"].map((option) => <button type="button" key={option} className={meals === option ? "selected" : ""} onClick={() => onMealsChange(option)}>{option}</button>)}</div>
    </Question>
    <article className="food-card">
      <header><h2>식품별 섭취 빈도와 1회 섭취량</h2><span>{answers.filter((item) => item.frequency).length} / {SOURCE_FOOD_ITEMS.length} 응답</span></header>
      <div className="food-row food-labels"><span>식품</span><span>섭취 빈도</span><span>1회 섭취량</span></div>
      {SOURCE_FOOD_ITEMS.map(([code, name]) => { const answer = answerFor(code, name); return <div className="food-row" key={code}><strong>{name}</strong><select aria-label={`${name} 섭취 빈도`} value={answer.frequency} onChange={(event) => update(code, name, { frequency: event.target.value, amount: event.target.value === "거의 안 먹음" ? "" : answer.amount })}><option value="">선택</option>{frequencies.map((option) => <option key={option}>{option}</option>)}</select><select aria-label={`${name} 1회 섭취량`} value={answer.amount} disabled={!answer.frequency || answer.frequency === "거의 안 먹음"} onChange={(event) => update(code, name, { amount: event.target.value })}><option value="">선택</option><option value="0.5">0.5공기</option><option value="1">1공기</option><option value="1.5">1.5공기</option></select></div>; })}
    </article>
  </>;
}
