import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { UseMutationResult } from "@tanstack/react-query";
import { useScheduleActions } from "@/hooks/defense/use-schedule-actions";
import * as queries from "@/hooks/queries";
import { toast } from "sonner";
import type { ReactNode } from "react";
import type { DefenseSession } from "@/types";
import type { ScheduleResponse, ScheduleSlot } from "@/lib/api-coordinator";

const mockGenerateSchedule = vi.hoisted(() => vi.fn());

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

vi.mock("@/hooks/queries", () => ({
  useSaveSchedules: vi.fn(),
  useTransitionDefenseSession: vi.fn(),
}));

vi.mock("@/hooks/defense/use-schedule-auto-generator", () => ({
  useScheduleAutoGenerator: vi.fn(() => ({
    generateSchedule: mockGenerateSchedule,
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

const mockJuries = [
  { id: 1, projectTitle: "AI", defenseType: "pfe", projectId: 10, members: [] },
  { id: 2, projectTitle: "Blockchain", defenseType: "pfe", projectId: 20, members: [] },
];

const mockRooms = [
  { id: 1, name: "Salle A", capacity: 30, departmentId: 1 },
];

const mockSession = {
  id: 5,
  name: "Session 1",
  defenseType: "pfe" as const,
  status: "draft",
  maxGroupSize: 3,
  defenseDuration: 60,
  breakDuration: 15,
  submissionDeadline: "2026-06-01",
  evaluationCoefficients: {},
  juryRoleTemplateId: 1,
  startDate: "2026-06-01",
  endDate: "2026-06-15",
};

function defaultProps(overrides = {}) {
  return {
    schedule: {},
    setSchedule: vi.fn(),
    currentSession: mockSession,
    juries: mockJuries,
    rooms: mockRooms,
    days: [new Date("2026-06-01")],
    timeSlots: ["08:00", "10:00"],
    ...overrides,
  };
}

describe("useScheduleActions", () => {
  const mockSaveMutate = vi.fn();
  const mockTransitionMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(queries.useSaveSchedules).mockReturnValue({
      mutateAsync: mockSaveMutate,
    } as unknown as UseMutationResult<ScheduleResponse[], Error, { defenseSessionId: number; slots: ScheduleSlot[] }>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue({
      mutateAsync: mockTransitionMutate,
    } as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }>);
  });

  it("handleSave shows error when schedule is empty", async () => {
    const { result } = renderHook(() => useScheduleActions(defaultProps()), {
      wrapper: createWrapper(),
    });
    await act(async () => {
      await result.current.handleSave();
    });
    expect(toast.error).toHaveBeenCalledWith("Aucune modification à enregistrer");
    expect(mockSaveMutate).not.toHaveBeenCalled();
  });

  it("handleSave returns early when no currentSession", async () => {
    const { result } = renderHook(
      () =>
        useScheduleActions(
          defaultProps({ currentSession: null, schedule: { "1": { roomId: 1, date: "2026-06-01", time: "08:00" } } }),
        ),
      { wrapper: createWrapper() },
    );
    await act(async () => {
      await result.current.handleSave();
    });
    expect(mockSaveMutate).not.toHaveBeenCalled();
  });

  it("handleSave calls mutateAsync with mapped slots", async () => {
    const schedule = { "1": { roomId: 1, date: "2026-06-01", time: "08:00" } };
    const { result } = renderHook(() => useScheduleActions(defaultProps({ schedule })), {
      wrapper: createWrapper(),
    });
    await act(async () => {
      await result.current.handleSave();
    });
    expect(mockSaveMutate).toHaveBeenCalledWith({
      defenseSessionId: 5,
      slots: [
        {
          title: "AI",
          date: "2026-06-01",
          time: "08:00",
          projectId: 10,
          roomId: 1,
        },
      ],
    });
    expect(toast.success).toHaveBeenCalledWith("Planning enregistré avec succès");
  });

  it("handleSave shows error toast on failure", async () => {
    mockSaveMutate.mockRejectedValueOnce(new Error("Network error"));
    const schedule = { "1": { roomId: 1, date: "2026-06-01", time: "08:00" } };
    const { result } = renderHook(() => useScheduleActions(defaultProps({ schedule })), {
      wrapper: createWrapper(),
    });
    await act(async () => {
      await result.current.handleSave();
    });
    expect(toast.error).toHaveBeenCalledWith("Network error");
  });

  it("handleAutoGenerate calls generateSchedule and setSchedule", () => {
    const generatedSchedule = { "1": { roomId: 1, date: "2026-06-01", time: "08:00" } };
    mockGenerateSchedule.mockReturnValue(generatedSchedule);

    const setSchedule = vi.fn();
    const { result } = renderHook(() => useScheduleActions(defaultProps({ setSchedule })), {
      wrapper: createWrapper(),
    });
    act(() => result.current.handleAutoGenerate());
    expect(mockGenerateSchedule).toHaveBeenCalled();
    expect(setSchedule).toHaveBeenCalledWith(generatedSchedule);
    expect(toast.success).toHaveBeenCalledWith("Planning généré pour 1 jury(s)");
  });

  it("handlePublish calls transitionSession and shows success", async () => {
    const { result } = renderHook(() => useScheduleActions(defaultProps()), {
      wrapper: createWrapper(),
    });
    await act(async () => {
      await result.current.handlePublish();
    });
    expect(mockTransitionMutate).toHaveBeenCalledWith({ id: 5, toStatus: "active" });
    expect(toast.success).toHaveBeenCalledWith("Session publiée avec succès");
  });

  it("handlePublish returns early when no currentSession", async () => {
    const { result } = renderHook(
      () => useScheduleActions(defaultProps({ currentSession: null })),
      { wrapper: createWrapper() },
    );
    await act(async () => {
      await result.current.handlePublish();
    });
    expect(mockTransitionMutate).not.toHaveBeenCalled();
  });

  it("handlePublish shows error toast on failure", async () => {
    mockTransitionMutate.mockRejectedValueOnce(new Error("Forbidden"));
    const { result } = renderHook(() => useScheduleActions(defaultProps()), {
      wrapper: createWrapper(),
    });
    await act(async () => {
      await result.current.handlePublish();
    });
    expect(toast.error).toHaveBeenCalledWith("Forbidden");
  });

  it("manages isPublishDialogOpen state", () => {
    const { result } = renderHook(() => useScheduleActions(defaultProps()), {
      wrapper: createWrapper(),
    });
    expect(result.current.isPublishDialogOpen).toBe(false);
    act(() => result.current.setIsPublishDialogOpen(true));
    expect(result.current.isPublishDialogOpen).toBe(true);
  });
});
