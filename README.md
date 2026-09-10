# system-gestion-soutenance (monorepo)

Unified repo: `api/` + `ui/` + `e2e/` — history preserved via subtree.
Old repos deprecated, not deleted.

| Package | Path | Stack | Port |
|---|---|---|---|
| API | `api/` (Maven root) | Spring Boot 3.4.6 → 4.x, Java 25, H2 dev / MySQL prod | 8080 |
| UI | `ui/` | React 19, TS 6, Vite 8, Tailwind 4, TanStack Query v5 | 5173 |
| E2E | `e2e/` | Playwright 1.52 (lean — no vendored clones) | — |

## Dev

```bash
# API first
cd api && ./mvnw spring-boot:run
# then UI
cd ui && npm ci && npm run dev
# E2E (uses webServer locally, MySQL service in CI)
cd e2e && npm ci && npx playwright test
```

## CI

- `api-ci.yml` — paths `api/**`, JDK 25, Spotless/Checkstyle/Spotbugs/PMD + tests.
- `ui-ci.yml` — paths `ui/**`, Node 22, lint + coverage + build.
- `e2e.yml` — paths `api|ui|e2e`, MySQL 8.4 service, JDK 25 + Node 22, `%{http_code}` health gates.

## Status (Step 0 green-check, 2026-09-09)

- API: 689 tests pass, JaCoCo gate RED (lines 0.82 vs 0.85, branches 0.65 vs 0.70) — coverage debt to fix in Step 2.
- UI: lint ✅ build ✅, 614/615 → fixed MSW `request.formData()` hang in `handlers.ts` (now 615/615 on that file; full re-run pending).
- E2E: `example.spec` only — real journeys in Step 4.

