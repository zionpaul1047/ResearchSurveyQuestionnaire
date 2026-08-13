# API 명세

기본 경로: `/api/v1`

## 1. 신규 임시저장 생성

```http
POST /api/v1/submissions
Content-Type: application/json
```

응답 `201 Created`:

```json
{
  "submissionId": "550e8400-e29b-41d4-a716-446655440000",
  "submissionNumber": "RSQ-2026-08-13-A1B2C3D4",
  "status": "DRAFT",
  "updatedAt": "2026-08-13T08:00:00Z",
  "submittedAt": null
}
```

## 2. 기존 임시저장 수정

```http
PUT /api/v1/submissions/{submissionId}
Content-Type: application/json
```

- 상태가 `DRAFT`일 때만 수정됩니다.
- 이미 제출된 경우 `409 Conflict`입니다.

## 3. 최종 제출

```http
POST /api/v1/submissions/{submissionId}/submit
Content-Type: application/json
```

- 서버가 전체 필수값과 적격 조건을 다시 검증합니다.
- 성공하면 상태가 `SUBMITTED`로 변경됩니다.

## 4. 제출 상태 확인

```http
GET /api/v1/submissions/{submissionId}/status
```

현재는 개발 편의를 위한 엔드포인트입니다. 운영 전에는 대상자 토큰 검증을 추가해야 합니다.

## 5. 첨부파일 업로드

```http
POST /api/v1/submissions/{submissionId}/attachments
Content-Type: multipart/form-data
```

Form 필드:

| 필드 | 설명 |
|---|---|
| `file` | JPG, PNG 또는 WEBP 이미지 |
| `category` | `PURCHASE_EVIDENCE`(구매·수령 자료) 또는 `PRODUCT_PHOTO`(제품 사진) |

응답 `201 Created`:

```json
{
  "attachmentId": "550e8400-e29b-41d4-a716-446655440001",
  "originalFileName": "sample-product.jpg",
  "fileSize": 123456
}
```

## 오류 응답

Spring의 Problem Details 형식을 사용합니다.

```json
{
  "type": "about:blank",
  "title": "요청을 처리할 수 없습니다.",
  "status": 400,
  "detail": "모든 식품의 섭취 빈도를 선택해 주세요."
}
```

## 입력 검증 경계

- 임시저장도 형식·길이·허용값 검증을 통과해야 합니다. 필수 문항은 비어 있을 수 있지만 잘못된 형식은 저장되지 않습니다.
- 최종 제출은 형식 검증에 더해 필수값, 적격성, 조건부 문항과 날짜 간 관계를 전부 검사합니다.
- 연락처는 국내 전화번호 형식으로 정규화됩니다.
- 식별번호와 자유입력에는 주민등록번호를 넣을 수 없습니다.
- 제품 평균 섭취빈도와 통화 가능 시간은 사전 정의된 선택값만 허용합니다.
- 첨부파일은 분류별 최대 10개까지 허용합니다.

상세 규칙은 [입력 가드레일 문서](INPUT_GUARDRAILS.md)를 참고합니다.

## 관리자 인증과 권한

관리자 API는 HTTP Basic 인증을 사용합니다. 계정은 환경변수로 주입하며, 브라우저 화면은 인증정보를 `localStorage`나 `sessionStorage`에 저장하지 않습니다.

| 역할 | 허용 범위 |
|---|---|
| `ANALYST` | 본인 계정 확인, 집계 통계 조회 |
| `ADMIN` | 분석가 권한 전체, 향후 설문 편집 API |

현재 방식은 로컬·PoC 기준입니다. 운영 환경에서는 HTTPS, 사내 SSO/OIDC, 로그인 실패 제한과 Secret 관리로 교체해야 합니다.

## 6. 관리자 로그인 확인

```http
GET /api/v1/admin/auth/me
Authorization: Basic {credentials}
```

응답 `200 OK`:

```json
{
  "username": "analyst",
  "roles": ["ANALYST"]
}
```

인증하지 않았거나 계정이 틀리면 `401 Unauthorized`입니다.

## 7. 설문 집계 현황

```http
GET /api/v1/admin/analytics/summary
Authorization: Basic {credentials}
```

- 전체·제출·임시저장 건수와 제출률
- 평균 작성시간
- 최근 30일 제출, 연령대, 지역, 제품·골절 경험 분포
- 식품별 섭취 빈도 분포
- 개별 응답이나 직접 식별정보는 반환하지 않음
- 기본 5명 미만인 항목은 `value: null`, `suppressed: true`로 반환
- 인증과 통계 조회 성공은 `admin_audit_log`에 기록

소규모 집단 기준은 `SURVEY_ANALYTICS_MIN_GROUP_SIZE`로 설정할 수 있으며 운영 정책 검토 없이 낮추면 안 됩니다.
