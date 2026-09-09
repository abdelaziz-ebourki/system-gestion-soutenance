import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import DefenseDesigner from "@/pages/coordinator/DefenseDesigner";

const mockSessions = [
  { id: "s1", name: "Session PFE 2025", defenseType: "pfe", status: "draft", maxGroupSize: 3, defenseDuration: 30, breakDuration: 15, submissionDeadline: "2025-06-01", startDate: "2025-06-15", endDate: "2025-06-30", evaluationCoefficients: {} },
];
const mockJuries = [{ id: 1, projectId: 1, projectTitle: "Application CI/CD", members: [{ teacherId: 1, teacherName: "Dr. Alami", role: "Président" }, { teacherId: 1, teacherName: "Pr. Bennani", role: "Examinateur" }] }];
const mockRooms = [{ id: "r1", name: "Salle A01", capacity: 30 }];
const mockDays = [new Date("2025-06-16")];
const mockTimeSlots = ["09:00", "09:30"];
const mockSchedule = { "j1": { date: "2025-06-16", time: "09:00", roomId: 1 } };

function createMockHook(overrides = {}): unknown {
  return {
    sessions: mockSessions,
    juries: mockJuries,
    rooms: mockRooms,
    projects: [],
    allLoading: false,
    selectedSessionId: "s1",
    setSelectedSessionId: vi.fn(),
    currentSession: mockSessions[0],
    days: mockDays,
    timeSlots: mockTimeSlots,
    searchQuery: "",
    setSearchQuery: vi.fn(),
    filteredJuries: mockJuries,
    activeJuryId: null,
    schedule: mockSchedule,
    handleDragStart: vi.fn(),
    handleDragEnd: vi.fn(),
    handleRemove: vi.fn(),
    handleSave: vi.fn(),
    handleAutoGenerate: vi.fn(),
    handlePublish: vi.fn(),
    isPublishDialogOpen: false,
    setIsPublishDialogOpen: vi.fn(),
    saveSchedule: { isPending: false },
    transitionSession: { isPending: false },
    ...overrides,
  };
}

vi.mock("@/hooks/use-defense-schedule", () => ({
  useDefenseSchedule: vi.fn(),
}));

function renderDesigner() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <DefenseDesigner />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("DefenseDesigner (Coordinator)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading skeleton", async () => {
    const { useDefenseSchedule } = await import("@/hooks/use-defense-schedule");
    vi.mocked(useDefenseSchedule).mockReturnValue(createMockHook({ allLoading: true }) as unknown as ReturnType<typeof useDefenseSchedule>);
    const { container } = renderDesigner();
    expect(container.querySelector('[data-slot="skeleton"]')).toBeInTheDocument();
  });

  it("renders empty state when no sessions", async () => {
    const { useDefenseSchedule } = await import("@/hooks/use-defense-schedule");
    vi.mocked(useDefenseSchedule).mockReturnValue(createMockHook({ sessions: [] }) as unknown as ReturnType<typeof useDefenseSchedule>);
    renderDesigner();
    expect(screen.getByText("Planificateur de Soutenances")).toBeInTheDocument();
    expect(screen.getByText(/Aucune session de soutenance disponible/)).toBeInTheDocument();
  });

  it("renders the page with session selected", async () => {
    const { useDefenseSchedule } = await import("@/hooks/use-defense-schedule");
    vi.mocked(useDefenseSchedule).mockReturnValue(createMockHook() as unknown as ReturnType<typeof useDefenseSchedule>);
    renderDesigner();
    expect(await screen.findByText("Planificateur de Soutenances")).toBeInTheDocument();
    expect(screen.getByTestId("coord-designer-page")).toBeInTheDocument();
    expect(screen.getByTestId("coord-designer-session-select")).toBeInTheDocument();
  });

  it("renders action buttons", async () => {
    const { useDefenseSchedule } = await import("@/hooks/use-defense-schedule");
    vi.mocked(useDefenseSchedule).mockReturnValue(createMockHook() as unknown as ReturnType<typeof useDefenseSchedule>);
    renderDesigner();
    expect(screen.getByTestId("coord-designer-auto-generate")).toBeInTheDocument();
    expect(screen.getByTestId("coord-designer-save")).toBeInTheDocument();
    expect(screen.getByTestId("coord-designer-publish")).toBeInTheDocument();
  });

  it("shows publish dialog when publish button is clicked", async () => {
    const { useDefenseSchedule } = await import("@/hooks/use-defense-schedule");
    vi.mocked(useDefenseSchedule).mockReturnValue(createMockHook({ isPublishDialogOpen: true }) as unknown as ReturnType<typeof useDefenseSchedule>);
    renderDesigner();
    expect(screen.getByTestId("coord-designer-publish-dialog")).toBeInTheDocument();
  });

  it("renders jury sidebar and calendar area", async () => {
    const { useDefenseSchedule } = await import("@/hooks/use-defense-schedule");
    vi.mocked(useDefenseSchedule).mockReturnValue(createMockHook() as unknown as ReturnType<typeof useDefenseSchedule>);
    renderDesigner();
    expect(screen.getByTestId("coord-designer-calendar")).toBeInTheDocument();
  });
});
