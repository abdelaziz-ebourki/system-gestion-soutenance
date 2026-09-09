import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { useScheduleDraft } from "@/hooks/defense/use-schedule-draft";

describe("useScheduleDraft", () => {
  it("initializes with empty schedule", () => {
    const { result } = renderHook(() => useScheduleDraft());
    expect(result.current.schedule).toEqual({});
  });

  it("initializes with provided schedule", () => {
    const initial = { "1": { roomId: 1, date: "2026-06-01", time: "08:00" } };
    const { result } = renderHook(() => useScheduleDraft(initial));
    expect(result.current.schedule).toEqual(initial);
  });

  it("updateSlot adds a new entry", () => {
    const { result } = renderHook(() => useScheduleDraft());
    act(() => result.current.updateSlot("1", { roomId: 2, date: "2026-06-02", time: "10:00" }));
    expect(result.current.schedule).toEqual({
      "1": { roomId: 2, date: "2026-06-02", time: "10:00" },
    });
  });

  it("updateSlot overwrites an existing entry", () => {
    const { result } = renderHook(() => useScheduleDraft());
    act(() => result.current.updateSlot("1", { roomId: 2, date: "2026-06-02", time: "10:00" }));
    act(() => result.current.updateSlot("1", { roomId: 3, date: "2026-06-03", time: "14:00" }));
    expect(result.current.schedule["1"]).toEqual({ roomId: 3, date: "2026-06-03", time: "14:00" });
  });

  it("removeSlot deletes an existing entry", () => {
    const { result } = renderHook(() => useScheduleDraft());
    act(() => result.current.updateSlot("1", { roomId: 2, date: "2026-06-02", time: "10:00" }));
    act(() => result.current.updateSlot("2", { roomId: 1, date: "2026-06-02", time: "08:00" }));
    act(() => result.current.removeSlot("1"));
    expect(result.current.schedule).toEqual({
      "2": { roomId: 1, date: "2026-06-02", time: "08:00" },
    });
  });

  it("removeSlot does nothing for non-existent key", () => {
    const { result } = renderHook(() => useScheduleDraft());
    act(() => result.current.updateSlot("1", { roomId: 2, date: "2026-06-02", time: "10:00" }));
    act(() => result.current.removeSlot("99"));
    expect(result.current.schedule).toEqual({
      "1": { roomId: 2, date: "2026-06-02", time: "10:00" },
    });
  });

  it("resetSchedule clears all entries", () => {
    const { result } = renderHook(() => useScheduleDraft());
    act(() => result.current.updateSlot("1", { roomId: 2, date: "2026-06-02", time: "10:00" }));
    act(() => result.current.resetSchedule());
    expect(result.current.schedule).toEqual({});
  });
});
