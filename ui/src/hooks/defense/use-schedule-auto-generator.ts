import { useCallback } from "react";
import { format } from "date-fns";
import type { Room, Jury } from "@/types";
import type { ScheduleEntry } from "@/hooks/defense/use-schedule-draft";

export function useScheduleAutoGenerator(
  rooms: Room[],
  days: Date[],
  timeSlots: string[],
  juries: Jury[],
) {
  const generateSchedule = useCallback(() => {
    const newSchedule: Record<string, ScheduleEntry> = {};
    let juryIndex = 0;
    for (const room of rooms) {
      for (const day of days) {
        for (const time of timeSlots) {
          if (juryIndex >= juries.length) break;
          const jury = juries[juryIndex];
          newSchedule[jury.id] = { roomId: room.id, date: format(day, "yyyy-MM-dd"), time };
          juryIndex++;
        }
        if (juryIndex >= juries.length) break;
      }
      if (juryIndex >= juries.length) break;
    }
    return newSchedule;
  }, [rooms, days, timeSlots, juries]);

  return { generateSchedule };
}
