# Database

PostgreSQL 스키마 변경은 `backend/src/main/resources/db/migration` 아래의 Flyway SQL로 관리합니다.

- 최초 스키마: `V1__create_survey_tables.sql`
- 이미 적용한 파일은 수정하지 않습니다.
- 다음 변경은 `V2__설명.sql`, `V3__설명.sql`처럼 새 파일로 추가합니다.

Docker Compose가 처음 DB를 실행할 때 백엔드가 Flyway를 통해 자동으로 테이블을 생성합니다.
