# Système Gestion Soutenance — UI

Frontend de la plateforme de gestion, planification et suivi des soutenances universitaires.

## Stack

- **React 19** + **TypeScript 6**
- **Vite 8** (build tool)
- **Tailwind CSS 4** (styling) + **shadcn/ui** (component primitives)
- **TanStack React Query v5** (server state) + **React Table v8** (data grids)
- **React Router v7** (routing)
- **Zod** (validation)
- **Recharts** (charts), **DnD Kit** (drag & drop), **Sonner** (toasts)

## Quick Start

```bash
npm install
npm run dev
```

Opens at `http://localhost:5173`.

### Demo accounts

| Email | Password | Role |
|---|---|---|
| `admin@univh2c.ma` | `1234` | Administrateur |
| `coord@univh2c.ma` | `1234` | Coordinateur |
| `teacher@univh2c.ma` | `1234` | Enseignant |
| `student@univh2c.ma` | `1234` | Étudiant |

## Available Scripts

| Command | Description |
|---|---|
| `npm run dev` | Start dev server with Vite proxy to API |
| `npm run build` | TypeScript check + production build |
| `npm run lint` | ESLint on all source files |
| `npm run lint:spell` | Spell check on all source files |
| `npm run preview` | Preview production build |

## Architecture

```
src/
  pages/        — Route-level components (lazy-loaded by role)
  components/   — UI primitives (shadcn) + business components
  hooks/        — React Query wrappers + entity CRUD hooks
  lib/          — API client, Zod validations, utilities
  types/        — TypeScript interfaces & types
  contexts/     — Auth, theme, sidebar providers
  config/       — App configuration (site name, links)
```

Route structure follows roles: `/admin/*`, `/coordinator/*`, `/teacher/*`, `/student/*`. Each role has its own layout, sidebar navigation, and protected routes.

## Backend API

All `/api/*` requests are proxied to the backend server via Vite. Start the API with `./mvnw spring-boot:run` from the `system-gestion-soutenance-api` directory.
