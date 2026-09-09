import { useMemo, useCallback } from "react";
import { validateSlotAssignment, buildConflictContext } from "@/lib/conflict-engine";
import type { Jury, Room, Project, Teacher } from "@/types";
import type { UnavailabilityEntry } from "@/lib/api-coordinator";
import type { ScheduleEntry } from "@/hooks/defense/use-schedule-draft";

interface ValidatorProps {
  schedule: Record<string, ScheduleEntry>;
  juries: Jury[];
  rooms: Room[];
  projects: Project[];
  teachers: Teacher[];
  unavailabilities: UnavailabilityEntry[];
  currentSession?: { startDate: string; endDate: string; breakDuration: number };
  allTimeSlots?: string[];
}

export function useScheduleConflictValidator({
  schedule,
  juries,
  rooms,
  projects,
  teachers,
  unavailabilities,
  currentSession,
  allTimeSlots,
}: ValidatorProps) {
  const context = useMemo(() => buildConflictContext(
    schedule,
    juries,
    rooms,
    projects,
    teachers,
    unavailabilities,
    currentSession,
    allTimeSlots ?? []
  ), [schedule, juries, rooms, projects, teachers, unavailabilities, currentSession, allTimeSlots]);

  const validateSlot = useCallback((projectId: number, slotKey: string) => {
    return validateSlotAssignment(projectId, slotKey, context);
  }, [context]);

  return {
    validateSlot,
    context,
  };
}
