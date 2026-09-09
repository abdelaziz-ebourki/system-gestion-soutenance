import { renderHook, act } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";
import { useScheduleSession } from "@/hooks/defense/use-schedule-session";
import * as queries from "@/hooks/queries";
import type { ReactNode } from "react";
import type { DefenseSession } from "@/types";
import type { PaginatedResponse } from "@/types";

vi.mock("@/hooks/queries", () => ({
  useCoordinatorDefenseSessions: vi.fn(),
  useDefenseSettings: vi.fn(),
}));

function createWrapper() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={qc}>{children}</QueryClientProvider>;
  };
}

const mockSessions: DefenseSession[] = [
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
  {
    id: 2,
    name: "Session 2",
    defenseType: "memoire",
    status: "active",
    maxGroupSize: 4,
    defenseDuration: 45,
    breakDuration: 10,
    submissionDeadline: "2026-07-01",
    evaluationCoefficients: {},
    juryRoleTemplateId: 1,
    startDate: "2026-07-01",
    endDate: "2026-07-10",
  },
];

const defaultSettings = {
  id: 1,
  startTime: "08:00",
  endTime: "18:00",
  defenseDuration: 60,
  breakDuration: 15,
  groupCreationStartDate: "",
  groupCreationEndDate: "",
};

describe("useScheduleSession", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({
      data: { items: mockSessions, total: 2, pageCount: 1, currentPage: 0, size: 10 },
      isLoading: false,
    } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useDefenseSettings).mockReturnValue({
      data: defaultSettings,
      isLoading: false,
    } as unknown as UseQueryResult<typeof defaultSettings, Error>);
  });

  it("auto-selects the first session", () => {
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    expect(result.current.selectedSessionId).toBe(1);
    expect(result.current.currentSession?.name).toBe("Session 1");
  });

  it("allows switching selected session", () => {
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    act(() => result.current.setSelectedSessionId(2));
    expect(result.current.selectedSessionId).toBe(2);
    expect(result.current.currentSession?.name).toBe("Session 2");
  });

  it("computes days from current session date range", () => {
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    expect(result.current.days).toHaveLength(15);
    expect(result.current.days[0]).toEqual(new Date("2026-06-01"));
    expect(result.current.days[14]).toEqual(new Date("2026-06-15"));
  });

  it("returns empty days when no session is selected", () => {
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({
      data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 },
      isLoading: false,
    } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    expect(result.current.days).toEqual([]);
  });

  it("uses defenseDuration directly from session", () => {
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    expect(result.current.defenseDuration).toBe(60);
  });

  it("computes time slots based on duration and settings", () => {
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    expect(result.current.timeSlots).toEqual([
      "08:00",
      "09:15",
      "10:30",
      "11:45",
      "13:00",
      "14:15",
      "15:30",
      "16:45",
    ]);
  });

  it("returns empty time slots when no session is selected", () => {
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({
      data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 },
      isLoading: false,
    } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    expect(result.current.timeSlots).toEqual([]);
  });

  it("exposes sessions and loading state", () => {
    const { result } = renderHook(() => useScheduleSession(), { wrapper: createWrapper() });
    expect(result.current.sessions).toHaveLength(2);
    expect(result.current.sessionsLoading).toBe(false);
  });
});
