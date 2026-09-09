import { useState, useMemo } from "react";
import {
  useJuries,
  useRooms,
  useProjects,
  useCoordinatorTeachersList,
  useCoordinatorUnavailability,
  useDefenseSettings,
} from "@/hooks/queries";

import { createSlotKey, parseSlotKey } from "@/lib/utils";
import type { DragStartEvent, DragEndEvent } from "@dnd-kit/core";
import { useScheduleDraft } from "@/hooks/defense/use-schedule-draft";
import { useScheduleConflictValidator } from "@/hooks/defense/use-schedule-conflict-validator";
import { useScheduleSession } from "@/hooks/defense/use-schedule-session";
import { useScheduleActions } from "@/hooks/defense/use-schedule-actions";
import { TOAST_DURATION_MS } from "@/lib/constants";
import { toast } from "sonner";

export function useDefenseSchedule() {
  const [activeJuryId, setActiveJuryId] = useState<number | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  const { data: juriesData, isLoading: juriesLoading } = useJuries();
  const juries = useMemo(() => juriesData?.items ?? [], [juriesData]);
  const { data: roomsPage, isLoading: roomsLoading } = useRooms();
  const rooms = roomsPage?.items ?? [];
  const { data: projectsData, isLoading: projectsLoading } = useProjects();
  const projects = useMemo(() => projectsData?.items ?? [], [projectsData]);
  const { data: teachersData, isLoading: teachersLoading } = useCoordinatorTeachersList();
  const teachers = teachersData?.items ?? [];
  const { data: unavailabilitiesData, isLoading: unavailLoading } = useCoordinatorUnavailability();
  const unavailabilities = unavailabilitiesData ?? [];
  const { isLoading: settingsLoading } = useDefenseSettings();

  const {
    sessions,
    sessionsLoading,
    selectedSessionId,
    setSelectedSessionId,
    currentSession,
    days,
    timeSlots,
    defenseDuration,
  } = useScheduleSession();

  const { schedule, setSchedule, updateSlot, removeSlot } = useScheduleDraft();

  const { handleSave, handleAutoGenerate, handlePublish, isPublishDialogOpen, setIsPublishDialogOpen, saveSchedule, transitionSession } = useScheduleActions({
    schedule,
    setSchedule,
    currentSession,
    juries,
    rooms,
    days,
    timeSlots,
  });

  const { validateSlot } = useScheduleConflictValidator({
    schedule,
    juries,
    rooms,
    projects,
    teachers,
    unavailabilities,
    currentSession,
    allTimeSlots: timeSlots,
  });

  const filteredJuries = useMemo(
    () =>
      juries.filter(
        (j) =>
          !schedule[j.id] &&
          (j.projectTitle.toLowerCase().includes(searchQuery.toLowerCase()) ||
            projects.some(
              (p) =>
                p.id === j.projectId &&
                p.studentNames.some((n) =>
                  n.toLowerCase().includes(searchQuery.toLowerCase()),
                ),
            )),
      ),
    [juries, searchQuery, schedule, projects],
  );

  const handleDragStart = (event: DragStartEvent) => {
    setActiveJuryId(event.active.id as number);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveJuryId(null);

    if (over) {
      const { date, room, time } = parseSlotKey(over.id as string);
      const roomId = Number(room);

      const juryId = active.id as number;
      const jury = juries.find((j) => j.id === juryId);
      if (!jury) return;

      const slotKey = createSlotKey(date, room, time);
      const result = validateSlot(jury.projectId, slotKey);

      if (!result.isValid) {
        const error = result.issues.find(i => i.severity === "error") || result.issues[0];
        if (error.suggestedResolution) {
          toast.error(`${error.message} ${error.suggestedResolution}`, { duration: TOAST_DURATION_MS });
        } else {
          toast.error(error.message);
        }
        return;
      }

      updateSlot(String(juryId), { roomId, date, time });
      toast.success("Positionné avec succès");
    }
  };

  const handleRemove = (juryId: number) => {
    removeSlot(String(juryId));
  };


  return {
    sessions,
    juries,
    rooms,
    projects,
    allLoading: sessionsLoading || juriesLoading || roomsLoading || unavailLoading || projectsLoading || teachersLoading || settingsLoading,
    selectedSessionId,
    setSelectedSessionId,
    currentSession,
    days,
    timeSlots,
    defenseDuration,
    searchQuery,
    setSearchQuery,
    filteredJuries,
    activeJuryId,
    schedule,
    handleDragStart,
    handleDragEnd,
    handleRemove,
    handleSave,
    handleAutoGenerate,
    handlePublish,
    isPublishDialogOpen,
    setIsPublishDialogOpen,
    saveSchedule,
    transitionSession,
  };
}
