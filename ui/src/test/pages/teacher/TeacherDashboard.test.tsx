import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import TeacherDashboard from "@/pages/teacher/TeacherDashboard";
import type { UseQueryResult } from "@tanstack/react-query";
import type { TeacherStats, TeacherDefense, TeacherEvaluation, PaginatedResponse } from "@/types";

vi.mock("@/hooks/queries", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/hooks/queries")>();
  return {
    ...actual,
    useTeacherStats: vi.fn(),
    useTeacherSchedule: vi.fn(),
    useTeacherEvaluations: vi.fn(),
  };
});

const mockStats: TeacherStats = {
  upcomingDefenses: 2,
  pendingEvaluations: 3,
  declaredUnavailabilitySlots: 5,
  juryAssignments: 1,
};

const mockSchedule = {
  slots: [
    {
      date: "2025-06-16",
      time: "09:00",
      roomName: "Salle A01",
      projectTitle: "Application CI/CD",
      studentNames: ["Ali", "Fatima"],
    },
    {
      date: "2025-06-16",
      time: "10:00",
      roomName: "Salle A02",
      projectTitle: "IA pour la santé",
      studentNames: ["Mohammed"],
    },
  ] as TeacherDefense[],
};

const mockEvaluations: PaginatedResponse<TeacherEvaluation> = {
  items: [
    {
      id: 1,
      projectId: 1,
      projectTitle: "Application CI/CD",
      finalGrade: 0,
      comment: "",
      status: "pending",
    },
    {
      id: 2,
      projectId: 2,
      projectTitle: "IA pour la santé",
      finalGrade: 15,
      comment: "Bon travail",
      status: "submitted",
    },
  ],
  total: 2,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

function renderDashboard() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <TeacherDashboard />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("TeacherDashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading state", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherStats).mockReturnValue({ data: null, isLoading: true } as unknown as UseQueryResult<TeacherStats, Error>);
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: null, isLoading: true } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(queries.useTeacherEvaluations).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: true } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderDashboard();
    expect(screen.getByTestId("teacher-dashboard-stats-upcoming")).toBeInTheDocument();
  });

  it("renders hero section and stat cards", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherStats).mockReturnValue({ data: mockStats, isLoading: false } as unknown as UseQueryResult<TeacherStats, Error>);
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(queries.useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderDashboard();
    expect(await screen.findByText("Un espace enseignant clair pour suivre jurys, planning et notes.")).toBeInTheDocument();
    expect(screen.getAllByText("2").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("3").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("5").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("1").length).toBeGreaterThanOrEqual(1);
  });

  it("renders upcoming defenses list", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherStats).mockReturnValue({ data: mockStats, isLoading: false } as unknown as UseQueryResult<TeacherStats, Error>);
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(queries.useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderDashboard();
    expect((await screen.findAllByText("Application CI/CD")).length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("IA pour la santé").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("Ali, Fatima").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("Mohammed").length).toBeGreaterThanOrEqual(1);
  });

  it("renders pending evaluations list", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherStats).mockReturnValue({ data: mockStats, isLoading: false } as unknown as UseQueryResult<TeacherStats, Error>);
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(queries.useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderDashboard();
    expect((await screen.findAllByText("Application CI/CD")).length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("IA pour la santé").length).toBeGreaterThanOrEqual(1);
  });

  it("shows empty states when no data", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherStats).mockReturnValue({ data: { ...mockStats, upcomingDefenses: 0, pendingEvaluations: 0 }, isLoading: false } as unknown as UseQueryResult<TeacherStats, Error>);
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: { slots: [] }, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    vi.mocked(queries.useTeacherEvaluations).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderDashboard();
    expect(await screen.findByText("Aucune soutenance prévue.")).toBeInTheDocument();
    expect(screen.getByText("Aucune évaluation en attente.")).toBeInTheDocument();
  });
});

