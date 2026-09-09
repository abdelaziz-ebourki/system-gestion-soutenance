import { useState } from "react";
import { useSaveSchedules, useTransitionDefenseSession } from "@/hooks/queries";
import { useScheduleAutoGenerator } from "@/hooks/defense/use-schedule-auto-generator";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { Room, Jury } from "@/types";
import type { ScheduleEntry } from "@/hooks/defense/use-schedule-draft";
import type { ScheduleSlot } from "@/lib/api-coordinator";
import type { DefenseSession } from "@/types";

interface UseScheduleActionsOptions {
  schedule: Record<string, ScheduleEntry>;
  setSchedule: (schedule: Record<string, ScheduleEntry>) => void;
  currentSession?: DefenseSession | null;
  juries: Jury[];
  rooms: Room[];
  days: Date[];
  timeSlots: string[];
}

export function useScheduleActions({
  schedule,
  setSchedule,
  currentSession,
  juries,
  rooms,
  days,
  timeSlots,
}: UseScheduleActionsOptions) {
  const [isPublishDialogOpen, setIsPublishDialogOpen] = useState(false);
  const saveSchedule = useSaveSchedules();
  const transitionSession = useTransitionDefenseSession();

  const { generateSchedule } = useScheduleAutoGenerator(rooms, days, timeSlots, juries);

  const handleSave = async () => {
    if (Object.keys(schedule).length === 0) {
      toast.error("Aucune modification à enregistrer");
      return;
    }
    if (!currentSession) return;

    try {
      const slots: ScheduleSlot[] = Object.entries(schedule).map(([key, s]) => {
        const juryId = Number(key);
        const jury = juries.find((j) => j.id === juryId);
        return {
          title: jury?.projectTitle ?? "",
          date: s.date,
          time: s.time,
          projectId: jury?.projectId ?? 0,
          roomId: Number(s.roomId),
        };
      });
      await saveSchedule.mutateAsync({ defenseSessionId: currentSession.id, slots });
      toast.success("Planning enregistré avec succès");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de l'enregistrement"));
    }
  };

  const handleAutoGenerate = () => {
    const newSchedule = generateSchedule();
    setSchedule(newSchedule);
    toast.success(`Planning généré pour ${Object.keys(newSchedule).length} jury(s)`);
  };

  const handlePublish = async () => {
    if (!currentSession) return;
    try {
      await transitionSession.mutateAsync({ id: currentSession.id, toStatus: "active" });
      toast.success("Session publiée avec succès");
      setIsPublishDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la publication"));
    }
  };

  return {
    handleSave,
    handleAutoGenerate,
    handlePublish,
    isPublishDialogOpen,
    setIsPublishDialogOpen,
    saveSchedule,
    transitionSession,
  };
}
