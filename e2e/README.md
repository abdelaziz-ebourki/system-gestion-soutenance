# E2E

Playwright suite (`e2e/tests`) against the full stack.

## Backends

| Env | API | UI | DB |
|---|---|---|---|
| Local (`npx playwright test`) | `webServer`: `spring-boot:run` on `:8080`, **test profile (H2)** | Vite dev on `:5173` | H2 mem |
| CI (`e2e.yml`) | jar, **prod profile + `DDL_AUTO=update`** | static `ui/dist` via `serve` | **MySQL 8.4 service** + Mailpit |

CI deliberately runs prod-profile-on-MySQL for prod parity (this caught real MySQL-only bugs before: reserved-word columns, `validate`-on-empty-DB). Local uses H2 for speed. Journeys must avoid DB-specific behavior; any MySQL-only failure is treated as a bug, not a test issue.

Seed data (`DataInitializer`) provides demo accounts (password `1234`):
`admin@univh2c.ma`, `coord@univh2c.ma`, `teacher@univh2c.ma`, `student@univh2c.ma`.

## Layout

- `auth.setup.ts` — one API login per role → `playwright/.auth/*.json` storage states (specs reuse them; only `auth.spec` touches the login form).
- `pages/` — page objects, `data-testid` selectors only (no CSS/XPath).
- `tests/` — `example.spec` (smoke), `auth.spec` (login/logout/RBAC), `coordinator-journey.spec`, `documents.spec` (PDF downloads), `grades.spec`, `bulk-import.spec`, `notifications.spec`.
