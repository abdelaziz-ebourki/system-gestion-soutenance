# Project Instructions: System Gestion Soutenance UI

## Project Overview

A modern web application built for managing university thesis defenses (Soutenances). It provides a role-based dashboard for administrators, coordinators, teachers, and students to coordinate schedules, departments, and academic sessions.

### Tech Stack

- **Framework:** React 19 (TypeScript)
- **Build Tool:** Vite 8+
- **Styling:** Tailwind CSS v4
- **UI Primitives:** Radix UI & Base UI
- **Components:** Shadcn UI implementation
- **Data Fetching:** Standard `fetch` with a centralized API wrapper
- **Routing:** React Router 7+
- **State Management:** React `useState`/`useContext` (centralized site config in `src/config/site.ts`)
- **Notifications:** Sonner

## Building and Running

### Development

```bash
npm run dev
```

Starts the development server at `http://localhost:5173`. API requests are proxied to the backend at `http://localhost:8080`.

### Build

```bash
npm run build
```

Compiles TypeScript and bundles the application for production in the `dist` directory.

### Linting

```bash
npm run lint
```

Runs ESLint to check for code quality and potential issues.

### Preview

```bash
npm run preview
```

Serves the production build locally for verification.

## Development Conventions

### Directory Structure

- `src/components/ui/`: Reusable, atomic UI components (buttons, inputs, dialogs).
- `src/components/academic/`: Feature-specific academic components (calendars, dialogs, convocation).
- `src/pages/`: Page components organized by user role (admin, coordinator, etc.).
- `src/lib/`: Core utilities (`utils.ts`) and the API client (`api.ts`).

- `src/types/`: Centralized TypeScript interfaces and types.

### API Usage

Always use the centralized `api` function in `src/lib/api.ts` for network requests. It handles:

- Authorization headers automatically via `localStorage.getItem("token")`.
- `204 No Content` responses.
- Centralized error toast notifications.

### Authentication & Authorization

- **Protected Routes:** Uses `ProtectedRoute.tsx` to guard routes based on roles.
- **Roles:** `admin`, `coordinator`, `teacher`, `student`.
- **Session:** Tokens and user metadata are stored in `localStorage`.

### UI/UX Standards

- **Loading States:** Use `isSubmitting` or `isDeleting` states with the `Loader2` icon (spinning) for all async actions in buttons.
- **Toasts:** Use `toast.success` and `toast.error` from `sonner` for user feedback.
- **Data Tables:** Utilize the reusable `DataTable` component in `src/components/ui/data-table.tsx` for administrative lists.

### Coding styles

- React.ReactFrom is deprecated, use React.SubmitEvent use React.ChangeEvent, React.InputEvent, React.SubmitEvent, or just React.SyntheticEvent instead depending on the event type.
- Never duplicate code.
- Never use any Typescript type unless absolutely necessary.
- Never declare and not use a variable, if you declare a variable it should be used.
- Always use service function from `src/lib/api.ts`. If a service function does not exist create it.
- Always use CSS variables for styling, never hardcode values
- Styles must be consistent
- Typescript types must be imported as a type
- Following best practices is a must
- This is not a toy project, it's rather a scalable SaaS project
- UI in french, code in english
- Avoid importing shadcn's atomique ui components, always use there app abstractions from `src/components/primitive`
- Components should never fetch() directly, always use a wrapper function from `src/lib/api.ts`, if there are none, create one
