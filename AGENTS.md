# Codex 작업 지침

이 파일의 지침은 저장소 전체에 적용됩니다.

## 프로젝트 목적

연구대상자용 건강 설문 MVP입니다. React 화면이 Spring Boot API를 호출하고, 응답은 PostgreSQL에 저장됩니다. 현재 구현은 개발·검증용이며 실제 개인정보를 받는 운영 서비스가 아닙니다.

## 주요 경로

- `frontend/`: React 19 + TypeScript + Vite
- `backend/`: Java 17 + Spring Boot 3 + JPA + Flyway
- `backend/src/main/resources/db/migration/`: PostgreSQL 스키마 이력
- `infrastructure/nginx/`: 프론트 정적 제공 및 API 프록시
- `docs/`: 요구사항, 구조, API 문서

## 검증 명령

```bash
cd frontend
npm ci
npm run lint
npm run build
```

```bash
cd backend
./gradlew --no-daemon clean build
```

Windows PowerShell:

```powershell
cd backend
.\gradlew.bat --no-daemon clean build
```

통합 실행은 저장소 루트에서 다음 명령을 사용합니다.

```bash
cp .env.example .env
docker compose config
docker compose up --build
```

## 변경 원칙

1. 이미 적용된 Flyway 마이그레이션은 수정하지 말고 `V2__...sql`처럼 새 파일을 추가합니다.
2. 생년월일·건강정보·파일명 등 개인정보를 로그에 남기지 않습니다.
3. 인증 없이 관리자 조회·다운로드 API를 노출하지 않습니다.
4. 제출 상태는 `DRAFT`와 `SUBMITTED`를 유지하며, 제출 완료 데이터는 일반 수정 API로 변경하지 않습니다.
5. API 호환성이 깨지는 변경은 `/api/v1` 문서와 프론트 타입을 함께 갱신합니다.
6. 화면 검증만 신뢰하지 말고 최종 제출 규칙은 백엔드에서도 검증합니다.
7. 기능 변경 시 관련 테스트와 `docs/` 문서를 함께 갱신합니다.

## 아직 운영에 필요한 항목

대상자 인증, 관리자 RBAC, 개인정보 컬럼 암호화, 감사로그, 악성파일 검사, 보관·파기, 백업·복구 정책은 아직 구현되지 않았습니다. 실제 운영 배포 요청을 받으면 이 항목을 먼저 위험요소로 알리고 범위를 확인합니다.
