import type { SubmissionResponse, SurveyForm } from "./types";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

async function request<T>(url: string, options: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {})
    }
  });

  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    throw new Error(problem?.detail ?? problem?.message ?? `요청에 실패했습니다. (${response.status})`);
  }
  return response.json() as Promise<T>;
}

export async function saveDraft(form: SurveyForm, submissionId?: string): Promise<SubmissionResponse> {
  return request<SubmissionResponse>(submissionId ? `/submissions/${submissionId}` : "/submissions", {
    method: submissionId ? "PUT" : "POST",
    body: JSON.stringify(form)
  });
}

export async function submitSurvey(submissionId: string, form: SurveyForm): Promise<SubmissionResponse> {
  return request<SubmissionResponse>(`/submissions/${submissionId}/submit`, {
    method: "POST",
    body: JSON.stringify(form)
  });
}

export async function uploadAttachments(submissionId: string, files: File[], category: "PURCHASE_EVIDENCE" | "PRODUCT_PHOTO"): Promise<void> {
  for (const file of files) {
    const body = new FormData();
    body.append("file", file);
    body.append("category", category);
    const response = await fetch(`${API_BASE}/submissions/${submissionId}/attachments`, {
      method: "POST",
      body
    });
    if (!response.ok) {
      const problem = await response.json().catch(() => null);
      throw new Error(problem?.detail ?? `${file.name} 업로드에 실패했습니다.`);
    }
  }
}
