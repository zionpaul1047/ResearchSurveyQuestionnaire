import { FormEvent, useEffect, useMemo, useState } from "react";
import { createAuthorization, loadAdminUser, loadAnalyticsSummary } from "./api";
import type { AdminUser, AnalyticsSummary, MetricPoint } from "./types";

type MetricChartProps = {
  title: string;
  description: string;
  rows: MetricPoint[];
  minimumGroupSize: number;
};

const roleLabel = (role: string) => (role === "ADMIN" ? "관리자" : role === "ANALYST" ? "분석가" : role);

const formatDuration = (seconds: number | null, suppressed: boolean, minimumGroupSize: number) => {
  if (suppressed || seconds === null) return `< ${minimumGroupSize}명 보호`;
  const minutes = Math.floor(seconds / 60);
  const remainder = seconds % 60;
  return minutes > 0 ? `${minutes}분 ${remainder}초` : `${remainder}초`;
};

function MetricChart({ title, description, rows, minimumGroupSize }: MetricChartProps) {
  const maximum = Math.max(1, ...rows.map((row) => row.value ?? 0));

  return (
    <section className="admin-panel metric-panel">
      <header>
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        <span>{rows.length}개 항목</span>
      </header>
      {rows.length === 0 ? (
        <div className="admin-empty">아직 집계할 제출 데이터가 없습니다.</div>
      ) : (
        <div className="metric-list">
          {rows.map((row, index) => (
            <div className="metric-row" key={`${row.label}-${index}`}>
              <div className="metric-label">
                <span>{row.label}</span>
                <strong>{row.suppressed ? `< ${minimumGroupSize}명` : `${row.value?.toLocaleString()}명`}</strong>
              </div>
              <div className={`metric-track${row.suppressed ? " suppressed" : ""}`}>
                <i style={{ width: row.suppressed ? "100%" : `${Math.max(4, ((row.value ?? 0) / maximum) * 100)}%` }} />
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function LoginView({ onLogin, loading, error }: { onLogin: (username: string, password: string) => void; loading: boolean; error: string }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const submit = (event: FormEvent) => {
    event.preventDefault();
    onLogin(username, password);
  };

  return (
    <main className="admin-login-page">
      <section className="admin-login-card">
        <a className="admin-back-link" href="/">← 설문 화면으로</a>
        <div className="admin-login-mark">RSQ</div>
        <p className="admin-eyebrow">RESEARCH OPERATIONS</p>
        <h1>설문 운영 대시보드</h1>
        <p className="admin-login-description">허가된 관리자와 분석가만 집계된 설문 현황을 조회할 수 있습니다.</p>
        <form onSubmit={submit}>
          <label>
            <span>아이디</span>
            <input autoComplete="username" maxLength={100} onChange={(event) => setUsername(event.target.value)} required value={username} />
          </label>
          <label>
            <span>비밀번호</span>
            <input autoComplete="current-password" minLength={12} onChange={(event) => setPassword(event.target.value)} required type="password" value={password} />
          </label>
          {error && <p className="admin-login-error" role="alert">{error}</p>}
          <button disabled={loading || !username.trim() || password.length < 12} type="submit">
            {loading ? "인증 확인 중..." : "보안 로그인"}
          </button>
        </form>
        <div className="admin-security-note">
          <strong>보안 안내</strong>
          <p>로그인 정보는 브라우저 저장소에 저장하지 않습니다. 운영 환경에서는 반드시 HTTPS와 별도 비밀정보 관리가 필요합니다.</p>
        </div>
      </section>
    </main>
  );
}

function Dashboard({ user, summary, onRefresh, onLogout, refreshing }: { user: AdminUser; summary: AnalyticsSummary; onRefresh: () => void; onLogout: () => void; refreshing: boolean }) {
  const foodRows = useMemo(() => {
    const groups = new Map<string, typeof summary.foodDistributions>();
    summary.foodDistributions.forEach((row) => {
      const current = groups.get(row.foodName) ?? [];
      current.push(row);
      groups.set(row.foodName, current);
    });
    return Array.from(groups.entries());
  }, [summary.foodDistributions]);

  const overview = summary.overview;

  return (
    <div className="admin-shell">
      <header className="admin-topbar">
        <a className="admin-brand" href="/admin"><b>RSQ</b><span><strong>Survey Operations</strong><small>관리자 대시보드</small></span></a>
        <div className="admin-user-box">
          <span><i /> {user.username} · {user.roles.map(roleLabel).join(", ")}</span>
          <button onClick={onLogout}>로그아웃</button>
        </div>
      </header>

      <main className="admin-main">
        <div className="admin-demo-notice" role="note"><strong>외부 확인용 데모</strong><span>현재 데이터는 테스트용이며 실제 개인정보를 수집하거나 운영 판단에 사용하면 안 됩니다.</span></div>
        <section className="admin-heading">
          <div>
            <p className="admin-eyebrow">AGGREGATE ANALYTICS</p>
            <h1>설문 수집 현황</h1>
            <p>개별 응답은 표시하지 않으며, 개인정보 보호 기준을 통과한 집계값만 제공합니다.</p>
          </div>
          <button disabled={refreshing} onClick={onRefresh}>{refreshing ? "새로고침 중..." : "↻ 데이터 새로고침"}</button>
        </section>

        <div className="admin-privacy-bar">
          <strong>소규모 집단 보호 적용</strong>
          <span>항목별 응답자가 {summary.minimumGroupSize}명 미만이면 정확한 수치와 막대 길이를 숨깁니다.</span>
        </div>

        <section className="admin-kpi-grid">
          <article><span>전체 응답</span><strong>{overview.totalResponses.toLocaleString()}</strong><small>임시저장 포함</small></article>
          <article><span>제출 완료</span><strong>{overview.submittedResponses.toLocaleString()}</strong><small>분석 대상 응답</small></article>
          <article><span>임시저장</span><strong>{overview.draftResponses.toLocaleString()}</strong><small>아직 미제출</small></article>
          <article className="accent"><span>제출 완료율</span><strong>{overview.submissionRate.toFixed(1)}%</strong><small>전체 대비 제출</small></article>
          <article><span>평균 작성 시간</span><strong className="duration">{formatDuration(overview.averageCompletionSeconds, overview.averageCompletionSuppressed, summary.minimumGroupSize)}</strong><small>제출 완료 기준</small></article>
        </section>

        <section className="admin-chart-grid wide-first">
          <MetricChart title="최근 30일 제출 추이" description="대한민국 표준시 기준 일별 제출 완료 건수" rows={summary.dailySubmissions} minimumGroupSize={summary.minimumGroupSize} />
          <MetricChart title="연령대 분포" description="생년월일과 설문일을 기준으로 계산" rows={summary.ageGroups} minimumGroupSize={summary.minimumGroupSize} />
        </section>

        <section className="admin-chart-grid">
          <MetricChart title="지역별 응답" description="제출 완료 응답자의 표준 지역 구분" rows={summary.regions} minimumGroupSize={summary.minimumGroupSize} />
          <MetricChart title="제품 섭취 경험" description="제품 섭취 경험 응답 분포" rows={summary.productExperiences} minimumGroupSize={summary.minimumGroupSize} />
          <MetricChart title="골절 경험" description="골절 경험 여부 응답 분포" rows={summary.fractureExperiences} minimumGroupSize={summary.minimumGroupSize} />
        </section>

        <section className="admin-panel food-distribution-panel">
          <header><div><h2>식품 섭취 빈도</h2><p>식품별 선택 빈도를 집계한 결과입니다.</p></div><span>{foodRows.length}개 식품</span></header>
          {foodRows.length === 0 ? <div className="admin-empty">아직 집계할 식품 응답이 없습니다.</div> : (
            <div className="food-distribution-grid">
              {foodRows.map(([foodName, rows]) => (
                <article key={foodName}>
                  <h3>{foodName}</h3>
                  {rows.map((row) => <div key={`${row.foodCode}-${row.frequency}`}><span>{row.frequency}</span><strong>{row.suppressed ? `< ${summary.minimumGroupSize}명` : `${row.value?.toLocaleString()}명`}</strong></div>)}
                </article>
              ))}
            </div>
          )}
        </section>

        <footer className="admin-footer">RSQ 집계 대시보드 · 조회 기록은 관리자 감사 로그에 저장됩니다.</footer>
      </main>
    </div>
  );
}

export default function AdminApp() {
  const [authorization, setAuthorization] = useState("");
  const [user, setUser] = useState<AdminUser | null>(null);
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const login = async (username: string, password: string) => {
    setLoading(true);
    setError("");
    const nextAuthorization = createAuthorization(username, password);
    try {
      const [nextUser, nextSummary] = await Promise.all([
        loadAdminUser(nextAuthorization),
        loadAnalyticsSummary(nextAuthorization),
      ]);
      setAuthorization(nextAuthorization);
      setUser(nextUser);
      setSummary(nextSummary);
    } catch (requestError) {
      setAuthorization("");
      setUser(null);
      setSummary(null);
      setError(requestError instanceof Error ? requestError.message : "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const refresh = async () => {
    if (!authorization) return;
    setLoading(true);
    try {
      setSummary(await loadAnalyticsSummary(authorization));
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "새로고침에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setAuthorization("");
    setUser(null);
    setSummary(null);
    setError("");
  };

  useEffect(() => {
    document.title = user ? "설문 수집 현황 | RSQ" : "관리자 로그인 | RSQ";
  }, [user]);

  if (!user || !summary) return <LoginView error={error} loading={loading} onLogin={login} />;
  return <Dashboard onLogout={logout} onRefresh={refresh} refreshing={loading} summary={summary} user={user} />;
}
