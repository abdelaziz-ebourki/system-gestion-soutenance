import { renderHook } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { useScheduleAutoGenerator } from "@/hooks/defense/use-schedule-auto-generator";

describe("useScheduleAutoGenerator", () => {
  const rooms = [
    { id: 1, name: "Salle A", capacity: 30, departmentId: 1 },
    { id: 2, name: "Salle B", capacity: 20, departmentId: 1 },
  ];

  const days = [new Date("2026-06-01"), new Date("2026-06-02")];

  const timeSlots = ["08:00", "10:00"];

  const juries = [
    { id: 1, projectTitle: "AI", defenseType: "pfe", projectId: 10, members: [] },
    { id: 2, projectTitle: "Blockchain", defenseType: "pfe", projectId: 20, members: [] },
    { id: 3, projectTitle: "IoT", defenseType: "pfe", projectId: 30, members: [] },
  ];

  it("distributes juries across rooms x days x timeSlots", () => {
    const { result } = renderHook(() =>
      useScheduleAutoGenerator(rooms, days, timeSlots, juries),
    );
    const schedule = result.current.generateSchedule();

    expect(Object.keys(schedule)).toHaveLength(3);
    expect(schedule[1]).toEqual({ roomId: 1, date: "2026-06-01", time: "08:00" });
    expect(schedule[2]).toEqual({ roomId: 1, date: "2026-06-01", time: "10:00" });
    expect(schedule[3]).toEqual({ roomId: 1, date: "2026-06-02", time: "08:00" });
  });

  it("returns empty schedule when there are no juries", () => {
    const { result } = renderHook(() =>
      useScheduleAutoGenerator(rooms, days, timeSlots, []),
    );
    expect(result.current.generateSchedule()).toEqual({});
  });

  it("stops early when slots exceed juries", () => {
    const singleJury = [{ id: 1, projectTitle: "AI", defenseType: "pfe", projectId: 10, members: [] }];
    const { result } = renderHook(() =>
      useScheduleAutoGenerator(rooms, days, timeSlots, singleJury),
    );
    const schedule = result.current.generateSchedule();
    expect(Object.keys(schedule)).toHaveLength(1);
    expect(schedule[1]).toBeDefined();
  });

  it("assigns correct date format", () => {
    const singleJury = [{ id: 5, projectTitle: "ML", defenseType: "pfe", projectId: 50, members: [] }];
    const { result } = renderHook(() =>
      useScheduleAutoGenerator(rooms, days, timeSlots, singleJury),
    );
    const schedule = result.current.generateSchedule();
    expect(schedule[5].date).toBe("2026-06-01");
  });
});
