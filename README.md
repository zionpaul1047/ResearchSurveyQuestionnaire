# Research Survey Questionnaire

연구대상자가 모바일·PC에서 설문을 작성하고, 응답을 PostgreSQL에 임시 저장한 뒤 최종 제출할 수 있는 풀스택 기본 프로젝트입니다.

> 현재 버전은 개발·검증용 MVP입니다. 관리자 인증·역할 권한·조회 감사로그는 기본 구현했지만, 실제 개인정보와 건강정보를 운영 환경에서 수집하기 전에는 개인정보 암호화, 외부 인증 연동, 보관·파기 정책과 보안성 검토를 반드시 추가해야 합니다.

## 구성

```text
ResearchSurveyQuestionnaire/
├── frontend/          React + TypeScript + Vite
├── backend/           Java 17 + Spring Boot + Gradle + Flyway
├── database/          PostgreSQL (Docker Compose)
├── infrastructure/    Nginx 설정
├── docs/              요구사항·API·구조 문서
├── docker-compose.yml
└── .env.example
```

## 가장 쉬운 실행 방법: Docker Compose

### 1. 환경변수 파일 생성

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

### 2. 전체 서비스 실행

```bash
docker compose up --build
```

### 3. 접속 주소

- 설문 화면: http://localhost:3000
- 관리자 현황 화면: http://localhost:3000/admin
- 백엔드 상태 확인: http://localhost:8080/actuator/health
- PostgreSQL: localhost:5432

로컬 기본 계정은 `.env.example`의 `admin` 또는 `analyst`입니다. 기본 비밀번호는 개발 편의를 위한 값이므로 외부 접속을 허용하기 전에 `.env`에서 반드시 변경하세요. 분석가는 집계 현황만 볼 수 있고 관리자는 이후 편집 기능까지 확장할 권한입니다.

### 4. 종료

```bash
docker compose down
```

DB 데이터까지 초기화하려면 다음 명령을 사용합니다. 기존 설문 데이터가 모두 삭제되므로 주의하세요.

```bash
docker compose down -v
```

## 로컬 개발 실행

### 프론트엔드

```bash
cd frontend
npm install
npm run dev
```

기본 개발 주소는 http://localhost:5173 입니다. `/api` 요청은 Vite가 `http://localhost:8080`으로 전달합니다.

### 백엔드

Java 17이 필요합니다. Gradle은 Wrapper가 포함되어 있어 별도 설치하지 않아도 됩니다.

```bash
cd backend
./gradlew bootRun
```

Windows PowerShell:

```powershell
cd backend
.\gradlew.bat bootRun
```

PostgreSQL이 먼저 실행되어 있어야 합니다. DB만 Docker로 실행할 수도 있습니다.

```bash
docker compose up postgres -d
```

## 현재 구현 범위

- 연구 참여 및 개인정보·건강정보 수집 동의
- 연구 참여 적격성 확인
- 기본정보와 신체정보 입력
- 제품 섭취 이력 동적 추가·삭제
- 골절 이력 최대 3건 추가·삭제
- 식품별 섭취빈도와 1회 섭취량
- 브라우저 자동 임시저장
- 서버 임시저장과 최종 제출
- 제품·구매 증빙 이미지 업로드 API
- PostgreSQL 정규화 테이블
- Flyway DB 마이그레이션
- Docker Compose 통합 실행
- Nginx 프론트 제공 및 `/api` 프록시
- 프론트·백엔드 GitHub Actions CI
- Spring Security 기반 관리자·분석가 역할 권한
- 개인정보 소규모 집단 억제가 적용된 읽기 전용 통계 API
- 관리자 설문 현황 대시보드
- 관리자 인증·통계 조회 감사로그

## 중요한 개발 상태

| 기능 | 현재 상태 |
|---|---|
| 설문 화면 | 구현 |
| 임시저장·최종제출 API | 구현 |
| PostgreSQL 저장 | 구현 |
| 로컬 파일 업로드 | 기본 구현 |
| 읽기 전용 관리자 현황 화면 | 구현 |
| 관리자 인증·역할 권한 | 로컬 환경변수 계정 기반 구현 |
| 관리자 조회 감사로그 | 구현 |
| 설문 문항 편집·버전 발행 | 미구현 |
| 개인정보 컬럼 암호화 | 미구현 |
| 연구소·협력업체 연계 | 미구현 |
| 전송 실패 재처리 | 미구현 |
| 문자·이메일 발송 | 미구현 |

## 문서

- [요구사항 및 범위](docs/REQUIREMENTS.md)
- [설문지 Ver. 2.4 구현 매핑](docs/QUESTIONNAIRE_MAPPING.md)
- [관리자 설문 편집·현황 대시보드 제안](docs/ADMIN_SURVEY_ANALYTICS_PLAN.md)
- [입력 가드레일 및 유효성 검증](docs/INPUT_GUARDRAILS.md)
- [시스템 구조](docs/ARCHITECTURE.md)
- [API 명세](docs/API.md)
- [직접 GitHub 업로드 방법](docs/GITHUB_UPLOAD.md)
- [Codex 작업 지침](AGENTS.md)

## 운영 전 필수 보강

1. 연락처·생년월일·건강정보 애플리케이션 암호화
2. 대상자별 고유 링크 또는 문자 인증
3. 관리자 계정 DB·사내 인증 연동, 로그인 실패 제한, 감사로그 보관정책
4. 업로드 파일 악성코드 검사와 비공개 저장소
5. HTTPS 및 비밀값 Secret 관리
6. 백업·복구·보관기간·자동파기
7. 개인정보·연구윤리·보안 담당자 검토
