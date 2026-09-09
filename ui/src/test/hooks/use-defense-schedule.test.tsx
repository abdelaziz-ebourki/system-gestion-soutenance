import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { UseQueryResult, UseMutationResult } from "@tanstack/react-query";
import type { DefenseSession, Jury, Room, Project, Teacher } from "@/types";
import type { UnavailabilityEntry, ScheduleSlot, ScheduleResponse } from "@/lib/api-coordinator";
import type { PaginatedResponse } from "@/types";
import { useDefenseSchedule } from "@/hooks/use-defense-schedule";
import * as queries from "@/hooks/queries";
import { toast } from "sonner";
import type { ReactNode } from "react";
import type { DragStartEvent, DragEndEvent } from "@dnd-kit/core";

const updateSlot = vi.hoisted(() => vi.fn());
const removeSlot = vi.hoisted(() => vi.fn());
const validateSlot = vi.hoisted(() => vi.fn().mockReturnValue({ isValid: true, issues: [] }));

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

vi.mock("@/hooks/queries", () => ({
  useJuries: vi.fn(),
  useRooms: vi.fn(),
  useProjects: vi.fn(),
  useCoordinatorTeachersList: vi.fn(),
  useSaveSchedules: vi.fn(),
  useCoordinatorDefenseSessions: vi.fn(),
  useCoordinatorUnavailability: vi.fn(),
  useTransitionDefenseSession: vi.fn(),
  useDefenseSettings: vi.fn(),
}));

vi.mock("@/hooks/defense/use-schedule-draft", () => ({
  useScheduleDraft: vi.fn(() => ({
    schedule: {},
    setSchedule: vi.fn(),
    updateSlot,
    removeSlot,
  })),
}));

vi.mock("@/hooks/defense/use-schedule-auto-generator", () => ({
  useScheduleAutoGenerator: vi.fn(() => ({
    generateSchedule: vi.fn(() => ({})),
  })),
}));

vi.mock("@/hooks/defense/use-schedule-conflict-validator", () => ({
  useScheduleConflictValidator: vi.fn(() => ({
    validateSlot,
  })),
}));

function createWrapper() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
  };
}

const mockSessions = [
  {
    id: 1,
    name: "Session 1",
    defenseType: "pfe",
    status: "draft",
    maxGroupSize: 3,
    defenseDuration: 60,
    breakDuration: 15,
    submissionDeadline: "2026-06-01",
    evaluationCoefficients: {},
    juryRoleTemplateId: 1,
    startDate: "2026-06-01",
    endDate: "2026-06-15",
  },
];

const mockJuries = [
  { id: 1, projectTitle: "AI in Health", defenseType: "pfe", projectId: 1, members: [{ teacherId: 1, teacherName: "T1", roleName: "President" }] },
  { id: 2, projectTitle: "Blockchain Logistics", defenseType: "pfe", projectId: 2, members: [{ teacherId: 1, teacherName: "T2", roleName: "Member" }] },
];

const mockRooms = [
  { id: 1, name: "Salle A", capacity: 30, departmentId: 1 },
  { id: 2, name: "Salle B", capacity: 20, departmentId: 1 },
];

const mockUnavailabilities: UnavailabilityEntry[] = [];

const mockSaveMutate = vi.fn().mockResolvedValue({});
const mockTransitionMutate = vi.fn().mockResolvedValue({});

describe("useDefenseSchedule", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: { items: mockSessions, total: 1, pageCount: 1, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuries).mockReturnValue({ data: { items: mockJuries, total: 2, pageCount: 1, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Jury>, Error>);
    vi.mocked(queries.useRooms).mockReturnValue({ data: { items: mockRooms, total: 2, pageCount: 1, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Room>, Error>);
    vi.mocked(queries.useProjects).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    vi.mocked(queries.useCoordinatorTeachersList).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Teacher>, Error>);
    vi.mocked(queries.useCoordinatorUnavailability).mockReturnValue({ data: mockUnavailabilities, isLoading: false } as unknown as UseQueryResult<UnavailabilityEntry[], Error>);
    vi.mocked(queries.useDefenseSettings).mockReturnValue({ data: { id: 1, startTime: "08:00", endTime: "18:00", defenseDuration: 60, breakDuration: 15, groupCreationStartDate: "", groupCreationEndDate: "" }, isLoading: false } as unknown as UseQueryResult<{ id: number; startTime: string; endTime: string; defenseDuration: number; breakDuration: number; groupCreationStartDate: string; groupCreationEndDate: string }, Error>);
    vi.mocked(queries.useSaveSchedules).mockReturnValue({ mutateAsync: mockSaveMutate } as unknown as UseMutationResult<ScheduleResponse[], Error, { defenseSessionId: number; slots: ScheduleSlot[] }>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue({ mutateAsync: mockTransitionMutate } as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }>);
  });

  it("initializes with the first session selected", () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    expect(result.current.selectedSessionId).toBe(1);
    expect(result.current.currentSession).toEqual(mockSessions[0]);
  });

  it("filters juries based on search query", () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() => result.current.setSearchQuery("Health"));
    expect(result.current.filteredJuries).toHaveLength(1);
    expect(result.current.filteredJuries[0].id).toBe(1);

    act(() => result.current.setSearchQuery("Blockchain"));
    expect(result.current.filteredJuries).toHaveLength(1);
    expect(result.current.filteredJuries[0].id).toBe(2);
  });

  it("handles publish session transition", async () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    
    await act(async () => {
      await result.current.handlePublish();
    });
    
    expect(mockTransitionMutate).toHaveBeenCalledWith({ id: 1, toStatus: "active" });
    expect(toast.success).toHaveBeenCalledWith("Session publiée avec succès");
  });

  it("handleDragStart sets activeJuryId", () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() => result.current.handleDragStart({ active: { id: 2 } } as DragStartEvent));
    expect(result.current.activeJuryId).toBe(2);
  });

  it("handleDragEnd on valid slot calls updateSlot and shows success", () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() => result.current.handleDragStart({ active: { id: 1 } } as DragStartEvent));
    act(() =>
      result.current.handleDragEnd({
        active: { id: 1 },
        over: { id: "2026-06-01|1|08:00" },
      } as DragEndEvent),
    );
    expect(result.current.activeJuryId).toBeNull();
    expect(updateSlot).toHaveBeenCalledWith("1", { roomId: 1, date: "2026-06-01", time: "08:00" });
    expect(toast.success).toHaveBeenCalledWith("Positionné avec succès");
  });

  it("handleDragEnd without over just clears activeJuryId", () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() => result.current.handleDragStart({ active: { id: 1 } } as DragStartEvent));
    act(() =>
      result.current.handleDragEnd({ active: { id: 1 }, over: null } as DragEndEvent),
    );
    expect(result.current.activeJuryId).toBeNull();
    expect(updateSlot).not.toHaveBeenCalled();
  });

  it("handleDragEnd with unknown jury does not crash", () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() =>
      result.current.handleDragEnd({
        active: { id: 999 },
        over: { id: "2026-06-01|1|08:00" },
      } as DragEndEvent),
    );
    expect(updateSlot).not.toHaveBeenCalled();
  });

  it("handleDragEnd on slot with conflict shows error toast", () => {
    validateSlot.mockReturnValue({
      isValid: false,
      issues: [
        { severity: "error", type: "teacher_double_booked", message: "Conflit enseignant", slot: "2026-06-01|1|08:00" },
      ],
    });
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() =>
      result.current.handleDragEnd({
        active: { id: 1 },
        over: { id: "2026-06-01|1|08:00" },
      } as DragEndEvent),
    );
    expect(toast.error).toHaveBeenCalledWith("Conflit enseignant");
  });

  it("handleDragEnd with conflict that has suggestedResolution includes it in toast", () => {
    validateSlot.mockReturnValue({
      isValid: false,
      issues: [
        { severity: "error", type: "teacher_double_booked", message: "Salle pleine", slot: "x", suggestedResolution: "Essayez la salle B" },
      ],
    });
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() =>
      result.current.handleDragEnd({
        active: { id: 1 },
        over: { id: "2026-06-01|1|08:00" },
      } as DragEndEvent),
    );
    expect(toast.error).toHaveBeenCalledWith("Salle pleine Essayez la salle B", expect.any(Object));
  });

  it("handleRemove calls removeSlot", () => {
    const { result } = renderHook(() => useDefenseSchedule(), { wrapper: createWrapper() });
    act(() => result.current.handleRemove(1));
    expect(removeSlot).toHaveBeenCalledWith("1");
  });
});

