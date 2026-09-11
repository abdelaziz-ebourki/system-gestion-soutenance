# system-gestion-soutenance (monorepo)

University defense-management platform (soutenances): admin, coordinator,
teacher and student workspaces — scheduling, juries, evaluations, documents (PDF),
notifications. Unified repo (`api/` + `ui/` + `e2e/`); history preserved via
subtree. The old `system-gestion-soutenance-{api,ui,e2e}` repos are deprecated
(read-only, tagged `v-final-pre-monorepo`).

| Package | Path | Stack | Port |
|---|---|---|---|
| API | `api/` (Maven root) | Spring Boot 4.0.8, Java 25, H2 dev / MySQL prod | 8080 |
| UI | `ui/` | React 19, TS 6, Vite 8, Tailwind 4, TanStack Query v5 | 5173 |
| E2E | `e2e/` | Playwright (lean — no vendored clones) | — |

Tests: API 771 (`mvn verify`, JaCoCo 85/70 enforced) · UI 619 (`vitest`) · E2E 34 (`playwright`, MySQL in CI).

## Run locally

```bash
# API first (dev profile: H2 + demo seed)
export JWT_SECRET=$(openssl rand -hex 32)   # required — the app refuses to boot without it
cd api && ./mvnw spring-boot:run            # :8080

# then UI (Vite proxies /api → :8080)
cd ui && npm ci && npm run dev              # :5173

# E2E (boots the stack itself via webServer; needs 8080 + 5173 free)
cd e2e && npm ci && npx playwright test
```

Demo logins (password `1234`): `admin@` / `coord@` / `teacher@` / `student@univh2c.ma`.
Swagger: `http://localhost:8080/swagger-ui/index.html` (dev only).
H2 console: `http://localhost:8080/h2-console` (`jdbc:h2:mem:defensedb`).

## Production

```bash
cp .env.example .env   # fill real secrets, then: sh scripts/check-env.sh
docker compose up --build -d
```

First boot must create the schema (`DDL_AUTO=update`), then flip back to
`validate`; empty prod DBs get a one-shot admin from `BOOTSTRAP_ADMIN_*`.
Full procedure, backup/restore, rollback: [`docs/RUNBOOK.md`](docs/RUNBOOK.md).
E2E details (backends, layout): [`e2e/README.md`](e2e/README.md).

## CI

- `api-ci.yml` — paths `api/**`, JDK 25: Spotless/Checkstyle/Spotbugs/PMD + `test`.
- `ui-ci.yml` — paths `ui/**`, Node 22: lint + coverage + build.
- `e2e.yml` — paths `api|ui|e2e`: MySQL 8.4 service, prod-profile API, static UI, `%{http_code}` health gates.
