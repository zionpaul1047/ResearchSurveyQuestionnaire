import type { AnalyticsSummary, FoodMetricPoint, MetricPoint } from "./types";

type Overview = AnalyticsSummary["overview"];

const clampPercent = (value: number) => Math.max(0, Math.min(100, value));
const exact = (value: number) => value.toLocaleString("ko-KR");
const compact = new Intl.NumberFormat("ko-KR", { notation: "compact", maximumFractionDigits: 1 });

const niceMaximum = (value: number) => {
  if (value <= 0) return 1;
  const magnitude = 10 ** Math.floor(Math.log10(value));
  const normalized = value / magnitude;
  const step = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10;
  return step * magnitude;
};

function PanelHeader({ title, description, badge }: { title: string; description: string; badge: string }) {
  return <header><div><h2>{title}</h2><p>{description}</p></div><span>{badge}</span></header>;
}

export function CollectionStatusChart({ overview }: { overview: Overview }) {
  const rate = clampPercent(overview.submissionRate);
  const circumference = 2 * Math.PI * 54;
  const submittedLength = circumference * rate / 100;
  const draftRate = overview.totalResponses === 0 ? 0 : overview.draftResponses * 100 / overview.totalResponses;

  return <section className="admin-panel collection-status-chart">
    <PanelHeader title="응답 수집 상태" description="전체 응답 중 제출 완료와 임시저장 비율" badge={`N=${exact(overview.totalResponses)}`} />
    <div className="collection-chart-body">
      <div className="donut-chart" role="img" aria-label={`전체 ${exact(overview.totalResponses)}건 중 제출 완료 ${exact(overview.submittedResponses)}건, 제출 완료율 ${rate.toFixed(1)}퍼센트`}>
        <svg viewBox="0 0 140 140" aria-hidden="true">
          <circle className="donut-base" cx="70" cy="70" r="54" />
          <circle className="donut-value" cx="70" cy="70" r="54" strokeDasharray={`${submittedLength} ${circumference}`} />
        </svg>
        <div><strong>{rate.toFixed(1)}%</strong><span>제출 완료율</span></div>
      </div>
      <div className="collection-breakdown">
        <div className="collection-total"><span>현재 전체 응답</span><strong>{exact(overview.totalResponses)}건</strong></div>
        <div className="stacked-status-bar" aria-hidden="true">
          <i className="submitted" style={{ width: `${rate}%` }} />
          <i className="draft" style={{ width: `${clampPercent(draftRate)}%` }} />
        </div>
        <div className="status-legend">
          <div><i className="submitted" /><span>제출 완료</span><strong>{exact(overview.submittedResponses)}건</strong></div>
          <div><i className="draft" /><span>임시저장</span><strong>{exact(overview.draftResponses)}건</strong></div>
        </div>
        <p className="chart-reading-tip"><b>해석</b> 제출 완료율이 낮아지면 작성 중 이탈 또는 입력 난이도를 점검해야 합니다.</p>
      </div>
    </div>
  </section>;
}

export function TrendChart({ rows }: { rows: MetricPoint[] }) {
  const width = 720;
  const height = 260;
  const left = 48;
  const right = 22;
  const top = 28;
  const bottom = 42;
  const plotWidth = width - left - right;
  const plotHeight = height - top - bottom;
  const rawMaximum = Math.max(0, ...rows.map((row) => row.value));
  const maximum = niceMaximum(rawMaximum);
  const total = rows.reduce((sum, row) => sum + row.value, 0);
  const average = rows.length === 0 ? 0 : total / rows.length;
  const peak = rows.reduce<MetricPoint | null>((current, row) => current === null || row.value > current.value ? row : current, null);
  const points = rows.map((row, index) => {
    const x = rows.length <= 1 ? left + plotWidth / 2 : left + index * plotWidth / (rows.length - 1);
    const y = top + plotHeight - (row.value / maximum) * plotHeight;
    return { ...row, x, y };
  });
  const linePath = points.map((point, index) => `${index === 0 ? "M" : "L"} ${point.x} ${point.y}`).join(" ");
  const areaPath = points.length ? `${linePath} L ${points.at(-1)!.x} ${top + plotHeight} L ${points[0].x} ${top + plotHeight} Z` : "";

  return <section className="admin-panel trend-chart-panel">
    <PanelHeader title="최근 30일 제출 추이" description="대한민국 표준시 기준 일별 제출 완료 건수" badge={`30일 N=${exact(total)}`} />
    {rows.length === 0 ? <div className="admin-empty">아직 집계할 제출 데이터가 없습니다.</div> : <>
      <div className="trend-stat-strip">
        <div><span>30일 누적</span><strong>{exact(total)}건</strong></div>
        <div><span>일평균</span><strong>{average.toFixed(1)}건</strong></div>
        <div><span>최고 일</span><strong>{peak ? `${peak.label.slice(5)} · ${exact(peak.value)}건` : "-"}</strong></div>
      </div>
      <div className="trend-chart-wrap">
        <svg className="trend-svg" viewBox={`0 0 ${width} ${height}`} role="img" aria-label={`최근 30일 제출 완료 추이, 누적 ${exact(total)}건, 일평균 ${average.toFixed(1)}건`}>
          <defs>
            <linearGradient id="trendArea" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#2385a2" stopOpacity=".34" /><stop offset="100%" stopColor="#2385a2" stopOpacity=".02" /></linearGradient>
          </defs>
          {[0, 1, 2, 3, 4].map((grid) => {
            const y = top + grid * plotHeight / 4;
            const label = maximum * (1 - grid / 4);
            return <g key={grid}><line x1={left} x2={width - right} y1={y} y2={y} /><text x={left - 10} y={y + 4}>{compact.format(label)}</text></g>;
          })}
          {areaPath && <path className="trend-area" d={areaPath} />}
          {linePath && <path className="trend-line" d={linePath} />}
          {points.map((point, index) => {
            const showAxisLabel = index % 5 === 0 || index === points.length - 1;
            const showValue = point.value > 0 && (rows.length <= 10 || index % 5 === 0 || point === points.at(-1));
            return <g className="trend-point" key={point.label}>
              <title>{point.label}: {exact(point.value)}건</title>
              <circle cx={point.x} cy={point.y} r="4" />
              {showValue && <text className="point-value" x={point.x} y={point.y - 12}>{exact(point.value)}</text>}
              {showAxisLabel && <text className="point-label" x={point.x} y={height - 14}>{point.label.slice(5)}</text>}
            </g>;
          })}
        </svg>
      </div>
      <p className="exact-data-note">각 점은 일별 정확 건수이며, PC에서는 점에 마우스를 올려 날짜와 값을 확인할 수 있습니다.</p>
    </>}
  </section>;
}

type DistributionChartProps = {
  title: string;
  description: string;
  rows: MetricPoint[];
  accent: "blue" | "teal" | "orange" | "violet";
};

export function DistributionChart({ title, description, rows, accent }: DistributionChartProps) {
  const maximum = Math.max(1, ...rows.map((row) => row.value));
  const total = rows.reduce((sum, row) => sum + row.value, 0);

  return <section className={`admin-panel distribution-chart accent-${accent}`}>
    <PanelHeader title={title} description={description} badge={`N=${exact(total)}`} />
    {rows.length === 0 ? <div className="admin-empty">아직 집계할 제출 데이터가 없습니다.</div> : <div className="distribution-list">
      {rows.map((row, index) => {
        const percent = total === 0 ? 0 : row.value * 100 / total;
        return <div className="distribution-row" key={row.label}>
          <div className="distribution-label"><b>{String(index + 1).padStart(2, "0")}</b><span>{row.label}</span><strong>{exact(row.value)}명 · {percent.toFixed(1)}%</strong></div>
          <div className="distribution-track" role="img" aria-label={`${row.label} ${exact(row.value)}명, ${percent.toFixed(1)}퍼센트`}><i style={{ width: `${row.value / maximum * 100}%` }} /></div>
        </div>;
      })}
      <p className="exact-data-note">N={exact(total)} · 관리자용 정확 집계 · 반올림은 비율 표시에만 적용</p>
    </div>}
  </section>;
}

export function FoodDistributionChart({ rows }: { rows: FoodMetricPoint[] }) {
  const groups = new Map<string, FoodMetricPoint[]>();
  rows.forEach((row) => groups.set(row.foodName, [...(groups.get(row.foodName) ?? []), row]));

  return <section className="admin-panel food-visual-panel">
    <PanelHeader title="식품 섭취 빈도" description="식품별 선택 빈도와 응답 내 비율" badge={`${groups.size}개 식품`} />
    {groups.size === 0 ? <div className="admin-empty">아직 집계할 식품 응답이 없습니다.</div> : <div className="food-visual-grid">
      {Array.from(groups.entries()).map(([foodName, foodRows], groupIndex) => {
        const maximum = Math.max(1, ...foodRows.map((row) => row.value));
        const total = foodRows.reduce((sum, row) => sum + row.value, 0);
        return <article className={`food-visual-card food-color-${groupIndex % 4}`} key={foodName}>
          <header><div><span>{String(groupIndex + 1).padStart(2, "0")}</span><h3>{foodName}</h3></div><b>N={exact(total)}</b></header>
          <div className="food-frequency-list">
            {foodRows.map((row) => {
              const percent = total === 0 ? 0 : row.value * 100 / total;
              return <div key={`${row.foodCode}-${row.frequency}`}>
                <div><span>{row.frequency}</span><strong>{exact(row.value)}명 · {percent.toFixed(1)}%</strong></div>
                <i role="img" aria-label={`${foodName} ${row.frequency} ${exact(row.value)}명, ${percent.toFixed(1)}퍼센트`}><b style={{ width: `${row.value / maximum * 100}%` }} /></i>
              </div>;
            })}
          </div>
        </article>;
      })}
    </div>}
  </section>;
}
