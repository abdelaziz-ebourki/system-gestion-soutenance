import { createSlotKey, parseSlotKey } from "@/lib/utils";
import type { Jury, Room, Project, Teacher } from "@/types";
import type { UnavailabilityEntry } from "@/lib/api-coordinator";
import { MAX_SUGGESTIONS } from "@/lib/constants";

export interface ConflictIssue {
  type: "teacher_double_booked" | "supervisor_conflict" | "out_of_bounds" | "slot_occupied" | "project_already_scheduled" | "teacher_unavailable";
  severity: "error" | "warning";
  message: string;
  slot: string;
  suggestedResolution?: string;
}

export interface SlotAssignment {
  id: number;
  title: string;
  date: string;
  time: string;
  roomId: number;
  studentIds?: number[];
  supervisorId?: number;
  juryTeacherIds?: number[];
}

export interface ConflictContext {
  schedule: Record<string, SlotAssignment>;
  rooms: Record<string, { id: number; name: string }>;
  groups: Record<string, { id: number; studentIds: number[] }>;
  projects: Record<string, { id: number; studentIds: number[]; supervisorId: number }>;
  teachers: Record<string, { id: number; name: string }>;
  juries: Record<string, { id: number; projectId: number; teacherIds: number[] }>;
  juriesByProjectId: Record<string, { id: number; projectId: number; teacherIds: number[] }>;
  unavailability: Record<string, { date: string; slots: string[]; teacherId: number }[]>;
  unavailabilitySet: Set<string>;
  defenseSession?: { startDate: string; endDate: string; breakDuration: number };
  allTimeSlots?: string[];
}

export function buildConflictContext(
  schedule: Record<string, { roomId: number; date: string; time: string }>,
  juries: Jury[],
  rooms: Room[],
  projects: Project[],
  teachers: Teacher[],
  unavailabilities: UnavailabilityEntry[],
  currentSession: { startDate: string; endDate: string; breakDuration: number } | undefined,
  allTimeSlots: string[],
): ConflictContext {
  const juriesMap = Object.fromEntries(
    juries.map((j) => [
      j.id,
      { id: j.id, projectId: j.projectId, teacherIds: j.members.map((m) => m.teacherId) },
    ]),
  );

  const juriesByProjectId = Object.fromEntries(
    juries.map((j) => [
      j.projectId,
      { id: j.id, projectId: j.projectId, teacherIds: j.members.map((m) => m.teacherId) },
    ]),
  );

  const unavailabilitySet = new Set(
    unavailabilities.flatMap((u) => u.slots.map((s) => `${u.teacherId}|${u.date}|${s}`)),
  );

  return {
    schedule: Object.fromEntries(
      Object.entries(schedule).map(([id, s]) => [
        createSlotKey(s.date, String(s.roomId), s.time),
        {
          id: Number(id),
          title: juries.find((j) => String(j.id) === id)?.projectTitle ?? "",
          date: s.date,
          time: s.time,
          roomId: s.roomId,
        },
      ]),
    ),
    rooms: Object.fromEntries(
      rooms.map((r) => [r.id, { id: r.id, name: r.name }]),
    ),
    groups: {},
    projects: Object.fromEntries(
      projects.map((p) => [p.id, { id: p.id, studentIds: [] as number[], supervisorId: 0 }]),
    ),
    teachers: Object.fromEntries(
      teachers.map((t) => [t.id, { id: t.id, name: `${t.firstName} ${t.lastName}` }]),
    ),
    juries: juriesMap,
    juriesByProjectId,
    unavailability: unavailabilities.reduce((acc, u) => {
      (acc[u.teacherId] ??= []).push({ date: u.date, slots: u.slots, teacherId: u.teacherId });
      return acc;
    }, {} as Record<string, { date: string; slots: string[]; teacherId: number }[]>),
    unavailabilitySet,
    defenseSession: currentSession
      ? { startDate: currentSession.startDate, endDate: currentSession.endDate, breakDuration: currentSession.breakDuration }
      : undefined,
    allTimeSlots,
  };
}

export function getSmartSuggestions(
  projectId: number,
  date: string,
  roomId: number,
  time: string,
  context: ConflictContext,
  issueType: ConflictIssue["type"],
): string | undefined {
  const canFit = (altRoomId: number, altTime: string) => {
    const altSlot = createSlotKey(date, String(altRoomId), altTime);
    if (context.schedule[altSlot]) return false;

    const projectJury = context.juriesByProjectId[projectId];
    const teacherIds = projectJury?.teacherIds ?? [];
    if (teacherIds.length > 0) {
      for (const tid of teacherIds) {
        if (context.unavailabilitySet.has(`${tid}|${date}|${altTime}`)) return false;
      }
    }
    return true;
  };

  if (issueType === "slot_occupied") {
    const betterRooms = Object.values(context.rooms)
      .filter((r) => r.id !== roomId && canFit(r.id, time))
      .map((r) => r.name);

    if (betterRooms.length > 0) {
      return `Suggestion intelligente : Essayez les salles libres : ${betterRooms.join(", ")}.`;
    }

    if (context.allTimeSlots) {
      const betterTimes = context.allTimeSlots
        .filter((t) => t !== time && canFit(roomId, t))
        .slice(0, MAX_SUGGESTIONS);
      if (betterTimes.length > 0) {
        return `Suggestion intelligente : Créneaux libres dans cette salle : ${betterTimes.join(", ")}.`;
      }
    }
  }

  if (issueType === "teacher_double_booked" || issueType === "teacher_unavailable") {
    if (context.allTimeSlots) {
      const freeTimes = context.allTimeSlots
        .filter((t) => t !== time && canFit(roomId, t))
        .slice(0, MAX_SUGGESTIONS);
      if (freeTimes.length > 0) {
        return `Suggestion intelligente : Le jury est disponible à : ${freeTimes.join(", ")}.`;
      }
    }
  }

  return undefined;
}

export function validateSlotAssignment(
  projectId: number,
  slot: string,
  context: ConflictContext,
): { isValid: boolean; issues: ConflictIssue[] } {
  const issues: ConflictIssue[] = [];

  let date: string, roomId: number, time: string;
  try {
    const parsed = parseSlotKey(slot);
    date = parsed.date;
    roomId = Number(parsed.room);
    time = parsed.time;
  } catch {
    return { isValid: false, issues: [{ type: "slot_occupied", severity: "error", message: "Format de créneau invalide.", slot }] };
  }

  const isAlreadyScheduled = Object.values(context.schedule).some((p) => p.id === projectId);
  if (isAlreadyScheduled) {
    issues.push({
      type: "project_already_scheduled",
      severity: "error",
      message: "Ce projet est déjà planifié sur un autre créneau.",
      slot,
    });
  }

  if (context.schedule[slot]) {
    issues.push({
      type: "slot_occupied",
      severity: "error",
      message: "Ce créneau est déjà occupé.",
      slot,
      suggestedResolution: getSmartSuggestions(projectId, date, roomId, time, context, "slot_occupied") || "Choisissez un autre créneau horaire ou une autre salle.",
    });
  }

  if (context.defenseSession) {
    if (date < context.defenseSession.startDate || date > context.defenseSession.endDate) {
      issues.push({
        type: "out_of_bounds",
        severity: "error",
        message: `La date ${date} est en dehors des limites de la session (${context.defenseSession.startDate} — ${context.defenseSession.endDate}).`,
        slot,
      });
    }
  }

  const projectJury = context.juriesByProjectId[projectId];
  const teacherIds = projectJury?.teacherIds ?? [];
  const project = context.projects[projectId];

  for (const [, existing] of Object.entries(context.schedule)) {
    if (existing.date !== date) continue;

    if (teacherIds.length > 0) {
      const existingJury = context.juriesByProjectId[existing.id];
      const existingTeachers = existingJury?.teacherIds ?? [];
      const overlap = teacherIds.filter((t) => existingTeachers.includes(t));
      if (overlap.length > 0) {
        for (const teacherId of overlap) {
          const teacher = context.teachers[teacherId];
          issues.push({
            type: "teacher_double_booked",
            severity: "error",
            message: `${teacher?.name ?? "Un enseignant"} est déjà affecté à "${existing.title}" le ${existing.date}.`,
            slot,
            suggestedResolution: getSmartSuggestions(projectId, date, roomId, time, context, "teacher_double_booked") || "Remplacez le membre du jury en conflit ou modifiez l'autre créneau.",
          });
        }
      }
    }

    if (project?.supervisorId) {
      const existingProject = context.projects[existing.id];
      if (existingProject?.supervisorId === project.supervisorId) {
        issues.push({
          type: "supervisor_conflict",
          severity: "warning",
          message: `L'encadrant est également encadrant de "${existing.title}" le même jour.`,
          slot,
          suggestedResolution: "Planifiez les deux passages le même jour avec suffisamment d'écart ou répartissez sur des jours différents.",
        });
      }
    }

  }

  if (teacherIds.length > 0) {
    for (const teacherId of teacherIds) {
      if (context.unavailabilitySet.has(`${teacherId}|${date}|${time}`)) {
        const teacher = context.teachers[teacherId];
        issues.push({
          type: "teacher_unavailable",
          severity: "error",
          message: `${teacher?.name ?? "Un enseignant"} est indisponible le ${date} à ${time}.`,
          slot,
          suggestedResolution: getSmartSuggestions(projectId, date, roomId, time, context, "teacher_unavailable") || "Choisissez un créneau où tous les membres du jury sont disponibles.",
        });
      }
    }
  }

  return {
    isValid: issues.filter((i) => i.severity === "error").length === 0,
    issues,
  };
}

export function getAllConflicts(
  schedule: Record<string, SlotAssignment>,
  context: ConflictContext,
): ConflictIssue[] {
  const all: ConflictIssue[] = [];
  for (const [slot, assignment] of Object.entries(schedule)) {
    const result = validateSlotAssignment(assignment.id, slot, context);
    all.push(...result.issues);
  }
  return all;
}
