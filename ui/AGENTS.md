# AGENTS.md — Système Gestion Soutenance

Multi-package project (not a monorepo workspace — each package has its own install/dev cycle):

| Package | Path | Stack | Port |
|---|---|---|---|
| API | `system-gestion-soutenance-api/` | Spring Boot 3.4.6, Java 17, Maven, H2 dev, JWT, Mailpit | 8080 |
| UI | `system-gestion-soutenance-ui/` | React 19, TypeScript 6, Vite 8, Tailwind 4, shadcn/ui, TanStack Query v5 | 5173 |
| E2E | `system-gestion-soutenance-e2e/` | Playwright 1.52 | — |
| Docs | `system-gestion-soutenance-docs/` | LaTeX, PlantUML | — |

## Commands

### API (`system-gestion-soutenance-api/`)
```bash
docker compose up --build          # recommended (includes Mailpit on :1025/:8025)
cd api && ./mvnw spring-boot:run    # without Docker
```
Swagger: `http://localhost:8080/swagger-ui/index.html`
H2 console: `http://localhost:8080/h2-console` (`jdbc:h2:mem:defensedb`)

### UI (`system-gestion-soutenance-ui/`)
```bash
npm run dev          # dev server, proxies /api -> localhost:8080
npm run build        # tsc -b && vite build
npm run lint         # eslint .
npm run lint:spell   # cspell on src/**/*.{ts,tsx}
npm run preview      # serve dist locally
```

### E2E (`system-gestion-soutenance-e2e/`)
```bash
npm run test           # npx playwright test (headless, 1 worker)
npm run test:headed    # headed mode
npm run test:ui        # Playwright UI mode
npm run codegen        # Playwright codegen
```

## Key details

- **Start order**: API first (needed at :8080), then UI (:5173).
- **Auth**: JWT stored in `localStorage` under `"token"`. All API calls go through `src/lib/api.ts` which attaches the token header automatically.
- **Demo accounts**: `admin@univh2c.ma` / `coord@univh2c.ma` / `teacher@univh2c.ma` / `student@univh2c.ma` — all password `1234`.
- **Routing by role**: `/admin/*`, `/coordinator/*`, `/teacher/*`, `/student/*`. Each section has a `ProtectedRoute` guard in `src/App.tsx`.
- **Path alias**: `@/` maps to `src/` (configured in both `vite.config.ts` and `tsconfig.json`).
- **Email**: Mock mode by default (logged to console). Configure SMTP via env vars or use Mailpit with Docker.
- **UI in French, code in English**.
- **API layers per module**: `controller/` → `service/` → `repository/` → `entity/` → `dto/`.
- **Node**: 22 (see `.nvmrc`). TS strict mode, `noUnusedLocals`/`noUnusedParameters` on.
- **CSS**: Tailwind 4 with CSS variables, no `tailwind.config.js`.
- **shadcn components**: Use app abstractions from `src/components/ui/` (not raw shadcn CLI components). Aliases in `components.json`.

## CI / E2E sync

Both API and UI repos have a `trigger-e2e.yml` workflow: on push to `main`, they rsync source into the e2e repo and push. The e2e repo is the single integration point — E2E tests run from there.

## Docs conventions

- PlantUML sources in `diagrams/src/`, export SVGs to `diagrams/svg/`.
- Include theme: `!include puml-theme-pfe.puml`.
- LaTeX macro for diagrams: `\pumlsvg[width]{filename}`.
- `shell-escape` may be needed for LaTeX compilation (Inkscape `svg` package).
- Language: French for docs/diagrams/reports.
- CDC (specs) PDF is the source of truth: `reports/cahier-des-charges/CDC_Licence_G7.pdf`.

## See also

- `system-gestion-soutenance-ui/GEMINI.md` — detailed UI conventions
- `system-gestion-soutenance-docs/GEMINI.md` — detailed docs conventions
- `system-gestion-soutenance-e2e/api/` holds the synced API source for e2e
- `system-gestion-soutenance-e2e/ui/` holds the synced UI source (uses MSW for mocks)

## Guidelines

- If you come across a code smell while reading/writing code, report back to user.
- don't strictly follow what's in `system-gestion-soutenance-docs`, it was a failed attempt to preemptively design the app.
- Never speculate, ask user if not sure about something
- If you reach a point where a commit is needed (following the convention of committing often) report to user with a suggestion of commit message. Report what should be tested to confirm the commit
- Follow commit messages conventions (ex: prefixing it with feat:, fix:... )
- Follow the standard industry practices