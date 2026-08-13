# GitHub에 직접 올리는 방법

ZIP 파일 자체를 GitHub 저장소에 올리면 안 됩니다. ZIP을 먼저 압축 해제한 뒤 안쪽 파일과 폴더를 저장소에 커밋해야 합니다.

## 방법 1: Git 명령 사용 권장

```powershell
git clone https://github.com/zionpaul1047/ResearchSurveyQuestionnaire.git
cd ResearchSurveyQuestionnaire
git switch -c feature/build-survey-platform
```

다운로드한 ZIP의 압축을 풀고, `ResearchSurveyQuestionnaire` 폴더 안의 내용을 위 저장소 폴더에 복사합니다. 그다음:

```powershell
git status
git add .
git commit -m "Build survey platform foundation"
git push -u origin feature/build-survey-platform
```

GitHub에서 `Compare & pull request`를 눌러 `main` 대상 Pull Request를 만듭니다.

## 방법 2: GitHub 웹 화면

1. 저장소를 엽니다.
2. `Add file` → `Upload files`를 선택합니다.
3. ZIP 자체가 아니라 압축 해제한 파일을 올립니다.
4. 파일 수가 많아 웹 업로드가 실패하면 방법 1을 사용합니다.

## Codex에서 이어서 작업할 때

저장소를 열면 루트의 `AGENTS.md`에 적힌 구조와 검증 규칙을 Codex가 작업 지침으로 사용합니다. 다음처럼 요청할 수 있습니다.

```text
이 프로젝트 구조를 먼저 분석해줘.
docker compose로 전체 서비스를 실행하고 오류가 있으면 수정해줘.
그다음 관리자 로그인과 설문 응답 목록 화면 구현 계획을 세워줘.
```

처음 검증할 명령:

```bash
docker compose config
docker compose up --build
```
