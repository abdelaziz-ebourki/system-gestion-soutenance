import { useState, useCallback } from "react";

export interface ScheduleEntry {
  roomId: number;
  date: string;
  time: string;
}

export function useScheduleDraft(initialSchedule: Record<string, ScheduleEntry> = {}) {
  const [schedule, setSchedule] = useState<Record<string, ScheduleEntry>>(initialSchedule);

  const updateSlot = useCallback((juryId: string, assignment: ScheduleEntry) => {
    setSchedule((prev) => ({ ...prev, [juryId]: assignment }));
  }, []);

  const removeSlot = useCallback((juryId: string) => {
    setSchedule((prev) => {
      const next = { ...prev };
      delete next[juryId];
      return next;
    });
  }, []);

  const resetSchedule = useCallback(() => {
    setSchedule({});
  }, []);

  return {
    schedule,
    setSchedule,
    updateSlot,
    removeSlot,
    resetSchedule,
  };
}
