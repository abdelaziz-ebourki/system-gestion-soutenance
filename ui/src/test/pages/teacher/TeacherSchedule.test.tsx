import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import TeacherSchedule from "@/pages/teacher/TeacherSchedule";
import type { UseQueryResult } from "@tanstack/react-query";
import type { TeacherDefense } from "@/types";

vi.mock("@/hooks/queries", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/hooks/queries")>();
  return {
    ...actual,
    useTeacherSchedule: vi.fn(),
  };
});

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
    {
      date: "2025-06-15",
      time: "14:00",
      roomName: "Salle B01",
      projectTitle: "Gestion de Stocks",
      studentNames: ["Sara"],
    },
  ] as TeacherDefense[],
};

function renderSchedule() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <TeacherSchedule />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("TeacherSchedule", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading state", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: null, isLoading: true } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    renderSchedule();
    expect(screen.getByTestId("teacher-schedule-header")).toBeInTheDocument();
    expect(screen.getByText("Planning détaillé")).toBeInTheDocument();
  });

  it("renders header and description", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    renderSchedule();
    expect(screen.getByTestId("teacher-schedule-header")).toHaveTextContent("Mon planning");
    expect(screen.getByTestId("teacher-schedule-description")).toHaveTextContent("Voici le planning des soutenances auxquelles vous participez.");
  });

  it("calculates and renders stats correctly", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    renderSchedule();
    
    expect(screen.getByTestId("teacher-schedule-stats-upcoming")).toHaveTextContent("3");
    expect(screen.getByTestId("teacher-schedule-stats-jury")).toHaveTextContent("3");
    expect(screen.getByTestId("teacher-schedule-stats-supervisor")).toHaveTextContent("3");
  });

  it("renders the data table with items", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    renderSchedule();
    
    expect(screen.getAllByText("Application CI/CD").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("IA pour la santé").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("Gestion de Stocks").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("Salle A01").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("Salle B01").length).toBeGreaterThanOrEqual(1);
  });

  it("renders upcoming defense cards", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: mockSchedule, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    renderSchedule();
    
    expect(screen.getAllByText("Ali, Fatima").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("Mohammed").length).toBeGreaterThanOrEqual(1);
  });

  it("shows empty state when no scheduled defenses", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useTeacherSchedule).mockReturnValue({ data: { slots: [] }, isLoading: false } as unknown as UseQueryResult<{ slots: TeacherDefense[] }, Error>);
    renderSchedule();
    
    expect(await screen.findByText("Aucune soutenance programmée pour la période sélectionnée.")).toBeInTheDocument();
  });
});

