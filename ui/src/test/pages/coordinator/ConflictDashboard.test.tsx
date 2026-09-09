import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import ConflictDashboard from "@/pages/coordinator/ConflictDashboard";
import type { UseQueryResult } from "@tanstack/react-query";
import type { Project, Room, Jury, Group, DefenseSession, Teacher } from "@/types";
import type { UnavailabilityEntry, ScheduleResponse } from "@/lib/api-coordinator";
import type { PaginatedResponse } from "@/types";

vi.mock("@/lib/conflict-engine", () => ({
  getAllConflicts: vi.fn(),
  buildConflictContext: vi.fn(() => ({
    schedule: {},
    rooms: {},
    groups: {},
    projects: {},
    teachers: {},
    juries: {},
    unavailability: {},
  })),
}));

function createMockQueryData() {
  return {
    projects: {
      data: { items: [{ id: 1, title: "Application CI/CD", description: "", defenseType: "pfe", groupId: 1, supervisorName: "Supervisor", studentNames: ["Ali", "Fatima"] }], total: 1, pageCount: 1, currentPage: 0, size: 10 },
      isLoading: false,
    },
    rooms: {
      data: { items: [{ id: 1, name: "Salle A01", capacity: 30, departmentId: 1 }], total: 1, pageCount: 1, currentPage: 0, size: 10 },
      isLoading: false,
    },
    juries: {
      data: { items: [{ id: 1, projectId: 1, projectTitle: "Application CI/CD", defenseType: "pfe", members: [{ teacherId: 1, teacherName: "Dr. Alami", roleName: "Président" }, { teacherId: 2, teacherName: "Pr. Bennani", roleName: "Examinateur" }] }], total: 1, pageCount: 1, currentPage: 0, size: 10 },
      isLoading: false,
    },
    groups: {
      data: { items: [{ id: 1, groupName: "Groupe A", projectId: 1, memberCount: 2, studentNames: ["Ali", "Fatima"] }], total: 1, pageCount: 1, currentPage: 0, size: 10 },
      isLoading: false,
    },
    sessions: {
      data: { items: [{ id: 1, name: "Session PFE 2025", defenseType: "pfe", status: "draft", maxGroupSize: 3, defenseDuration: 30, breakDuration: 15, submissionDeadline: "2025-06-01", evaluationCoefficients: {}, juryRoleTemplateId: 1, startDate: "2025-06-15", endDate: "2025-06-30" }], total: 1, pageCount: 1, currentPage: 0, size: 10 },
      isLoading: false,
    },
    schedules: {
      data: [] as ScheduleResponse[],
      isLoading: false,
    },
    unavailability: {
      data: [],
      isLoading: false,
    },
  };
}

vi.mock("@/hooks/queries", () => ({
  useProjects: vi.fn(),
  useRooms: vi.fn(),
  useJuries: vi.fn(),
  useGroups: vi.fn(),
  useCoordinatorDefenseSessions: vi.fn(),
  useSchedules: vi.fn(),
  useCoordinatorUnavailability: vi.fn(),
  useCoordinatorTeachersList: vi.fn(),
}));

function renderDashboard() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <ConflictDashboard />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("ConflictDashboard (Coordinator)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading skeleton", async () => {
    const { getAllConflicts } = await import("@/lib/conflict-engine");
    vi.mocked(getAllConflicts).mockReturnValue([]);
    const queries = await import("@/hooks/queries");
    const data = createMockQueryData();
    vi.mocked(queries.useProjects).mockReturnValue({ ...data.projects, isLoading: true } as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    vi.mocked(queries.useRooms).mockReturnValue(data.rooms as unknown as UseQueryResult<PaginatedResponse<Room>, Error>);
    vi.mocked(queries.useJuries).mockReturnValue(data.juries as unknown as UseQueryResult<PaginatedResponse<Jury>, Error>);
    vi.mocked(queries.useGroups).mockReturnValue(data.groups as unknown as UseQueryResult<PaginatedResponse<Group>, Error>);
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue(data.sessions as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useSchedules).mockReturnValue(data.schedules as unknown as UseQueryResult<ScheduleResponse[], Error>);
    vi.mocked(queries.useCoordinatorUnavailability).mockReturnValue(data.unavailability as unknown as UseQueryResult<UnavailabilityEntry[], Error>);
    vi.mocked(queries.useCoordinatorTeachersList).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Teacher>, Error>);
    const { container } = renderDashboard();
    expect(container.querySelector('[data-slot="skeleton"]')).toBeInTheDocument();
  });

    it("renders page title and stats cards", async () => {
    const { getAllConflicts } = await import("@/lib/conflict-engine");
    vi.mocked(getAllConflicts).mockReturnValue([
      { type: "teacher_double_booked", severity: "error", message: "Conflit enseignant", suggestedResolution: "Déplacer", slot: "s1" },
      { type: "teacher_unavailable", severity: "warning", message: "Enseignant indisponible", suggestedResolution: "Changer de créneau", slot: "s1" },
    ]);
    const queries = await import("@/hooks/queries");
    const data = createMockQueryData();
    vi.mocked(queries.useProjects).mockReturnValue(data.projects as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    vi.mocked(queries.useRooms).mockReturnValue(data.rooms as unknown as UseQueryResult<PaginatedResponse<Room>, Error>);
    vi.mocked(queries.useJuries).mockReturnValue(data.juries as unknown as UseQueryResult<PaginatedResponse<Jury>, Error>);
    vi.mocked(queries.useGroups).mockReturnValue(data.groups as unknown as UseQueryResult<PaginatedResponse<Group>, Error>);
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue(data.sessions as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useSchedules).mockReturnValue(data.schedules as unknown as UseQueryResult<ScheduleResponse[], Error>);
    vi.mocked(queries.useCoordinatorUnavailability).mockReturnValue(data.unavailability as unknown as UseQueryResult<UnavailabilityEntry[], Error>);
    vi.mocked(queries.useCoordinatorTeachersList).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Teacher>, Error>);
    renderDashboard();
    expect(await screen.findByText("Conflits de planification")).toBeInTheDocument();
    expect(screen.getByTestId("coord-conflicts-page")).toBeInTheDocument();
    const twos = screen.getAllByText("2");
    expect(twos.length).toBeGreaterThanOrEqual(1);
    const ones = screen.getAllByText("1");
    expect(ones.length).toBeGreaterThanOrEqual(1);
  });

  it("renders empty state when no conflicts", async () => {
    const { getAllConflicts } = await import("@/lib/conflict-engine");
    vi.mocked(getAllConflicts).mockReturnValue([]);
    const queries = await import("@/hooks/queries");
    const data = createMockQueryData();
    vi.mocked(queries.useProjects).mockReturnValue(data.projects as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    vi.mocked(queries.useRooms).mockReturnValue(data.rooms as unknown as UseQueryResult<PaginatedResponse<Room>, Error>);
    vi.mocked(queries.useJuries).mockReturnValue(data.juries as unknown as UseQueryResult<PaginatedResponse<Jury>, Error>);
    vi.mocked(queries.useGroups).mockReturnValue(data.groups as unknown as UseQueryResult<PaginatedResponse<Group>, Error>);
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue(data.sessions as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useSchedules).mockReturnValue(data.schedules as unknown as UseQueryResult<ScheduleResponse[], Error>);
    vi.mocked(queries.useCoordinatorUnavailability).mockReturnValue(data.unavailability as unknown as UseQueryResult<UnavailabilityEntry[], Error>);
    vi.mocked(queries.useCoordinatorTeachersList).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Teacher>, Error>);
    renderDashboard();
    expect(await screen.findByText("Aucun conflit détecté. La planification est prête à être publiée.")).toBeInTheDocument();
  });

    it("renders grouped conflict cards", async () => {
    const { getAllConflicts } = await import("@/lib/conflict-engine");
    vi.mocked(getAllConflicts).mockReturnValue([
      { type: "teacher_double_booked", severity: "error", message: "Enseignant Dr. Alami doublonné", suggestedResolution: "Déplacer un passage", slot: "s1" },
      { type: "teacher_unavailable", severity: "warning", message: "Teacher Dr. Alami indisponible", suggestedResolution: "Changer de créneau", slot: "s1" },
    ]);
    const queries = await import("@/hooks/queries");
    const data = createMockQueryData();
    vi.mocked(queries.useProjects).mockReturnValue(data.projects as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    vi.mocked(queries.useRooms).mockReturnValue(data.rooms as unknown as UseQueryResult<PaginatedResponse<Room>, Error>);
    vi.mocked(queries.useJuries).mockReturnValue(data.juries as unknown as UseQueryResult<PaginatedResponse<Jury>, Error>);
    vi.mocked(queries.useGroups).mockReturnValue(data.groups as unknown as UseQueryResult<PaginatedResponse<Group>, Error>);
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue(data.sessions as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useSchedules).mockReturnValue(data.schedules as unknown as UseQueryResult<ScheduleResponse[], Error>);
    vi.mocked(queries.useCoordinatorUnavailability).mockReturnValue(data.unavailability as unknown as UseQueryResult<UnavailabilityEntry[], Error>);
    vi.mocked(queries.useCoordinatorTeachersList).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Teacher>, Error>);
    renderDashboard();
    expect(await screen.findByText("Conflit enseignant")).toBeInTheDocument();
    expect(screen.getByText("Teacher Dr. Alami indisponible")).toBeInTheDocument();
    expect(screen.getByText("Enseignant Dr. Alami doublonné")).toBeInTheDocument();
    expect(screen.getByText(/Suggestion : Déplacer un passage/)).toBeInTheDocument();
    expect(screen.getByText(/Suggestion : Changer de créneau/)).toBeInTheDocument();
  });
});

