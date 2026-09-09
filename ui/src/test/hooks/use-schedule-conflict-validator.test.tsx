import { renderHook } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { useScheduleConflictValidator } from "@/hooks/defense/use-schedule-conflict-validator";

const mockBuildConflictContext = vi.fn();
const mockValidateSlotAssignment = vi.fn();

vi.mock("@/lib/conflict-engine", () => ({
  buildConflictContext: (...args: unknown[]) => mockBuildConflictContext(...args),
  validateSlotAssignment: (...args: unknown[]) => mockValidateSlotAssignment(...args),
}));

describe("useScheduleConflictValidator", () => {
  const defaultProps = {
    schedule: {},
    juries: [],
    rooms: [],
    projects: [],
    teachers: [],
    unavailabilities: [],
    currentSession: { startDate: "2026-06-01", endDate: "2026-06-15", breakDuration: 15 },
    allTimeSlots: ["08:00", "10:00"],
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockBuildConflictContext.mockReturnValue({ mockContext: true });
    mockValidateSlotAssignment.mockReturnValue({ isValid: true, issues: [] });
  });

  it("builds context on mount", () => {
    renderHook(() => useScheduleConflictValidator(defaultProps));
    expect(mockBuildConflictContext).toHaveBeenCalledWith(
      defaultProps.schedule,
      defaultProps.juries,
      defaultProps.rooms,
      defaultProps.projects,
      defaultProps.teachers,
      defaultProps.unavailabilities,
      defaultProps.currentSession,
      defaultProps.allTimeSlots,
    );
  });

  it("exposes the built context", () => {
    const { result } = renderHook(() => useScheduleConflictValidator(defaultProps));
    expect(result.current.context).toEqual({ mockContext: true });
  });

  it("validateSlot delegates to validateSlotAssignment", () => {
    const { result } = renderHook(() => useScheduleConflictValidator(defaultProps));
    const outcome = result.current.validateSlot(42, "2026-06-01|1|08:00");
    expect(mockValidateSlotAssignment).toHaveBeenCalledWith(42, "2026-06-01|1|08:00", { mockContext: true });
    expect(outcome).toEqual({ isValid: true, issues: [] });
  });

  it("rebuilds context when props change", () => {
    const { rerender } = renderHook(
      (props) => useScheduleConflictValidator(props),
      { initialProps: defaultProps },
    );
    expect(mockBuildConflictContext).toHaveBeenCalledTimes(1);

    rerender({ ...defaultProps, allTimeSlots: ["08:00", "14:00"] });
    expect(mockBuildConflictContext).toHaveBeenCalledTimes(2);
  });

  it("does not rebuild context when props are the same", () => {
    const { rerender } = renderHook(
      (props) => useScheduleConflictValidator(props),
      { initialProps: defaultProps },
    );
    expect(mockBuildConflictContext).toHaveBeenCalledTimes(1);

    rerender(defaultProps);
    expect(mockBuildConflictContext).toHaveBeenCalledTimes(1);
  });
});
