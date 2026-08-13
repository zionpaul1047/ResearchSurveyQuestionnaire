import type { AdminUser, AnalyticsSummary } from "./types";

const encodeBasicCredentials = (username: string, password: string) =>
  `Basic ${window.btoa(`${username}:${password}`)}`;

async function getJson<T>(path: string, authorization: string): Promise<T> {
  const response = await fetch(path, {
    headers: { Authorization: authorization },
    cache: "no-store",
  });

  if (response.status === 401 || response.status === 403) {
    throw new Error("아이디 또는 비밀번호가 올바르지 않거나 접근 권한이 없습니다.");
  }
  if (!response.ok) {
    throw new Error("관리자 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
  }
  return response.json() as Promise<T>;
}

export const createAuthorization = (username: string, password: string) =>
  encodeBasicCredentials(username.trim(), password);

export const loadAdminUser = (authorization: string) =>
  getJson<AdminUser>("/api/v1/admin/auth/me", authorization);

export const loadAnalyticsSummary = (authorization: string) =>
  getJson<AnalyticsSummary>("/api/v1/admin/analytics/summary", authorization);
