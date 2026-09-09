import { useState, useMemo, useEffect } from "react";
import { useCoordinatorDefenseSessions, useDefenseSettings } from "@/hooks/queries";
import { addDays, differenceInDays } from "date-fns";

export function useScheduleSession() {
  const [selectedSessionId, setSelectedSessionId] = useState<number | null>(null);

  const { data: sessionsData, isLoading: sessionsLoading } = useCoordinatorDefenseSessions();
  const sessions = useMemo(() => sessionsData?.items ?? [], [sessionsData]);
  const { data: defenseSettings } = useDefenseSettings();
 
  useEffect(() => {
    if (sessions.length && !selectedSessionId) {
      setSelectedSessionId(sessions[0].id);
    }
  }, [sessions, selectedSessionId]);
 
  const currentSession = useMemo(
    () => sessions.find((s) => s.id === selectedSessionId),
    [sessions, selectedSessionId],
  );

  const days = useMemo(() => {
    if (!currentSession) return [];
    const start = new Date(currentSession.startDate);
    const end = new Date(currentSession.endDate);
    const diffDays = Math.max(1, differenceInDays(end, start) + 1);
    return Array.from({ length: diffDays }).map((_, i) => addDays(start, i));
  }, [currentSession]);

  const timeSlots = useMemo(() => {
    if (!currentSession) return [];
    const { startTime = "08:00", endTime = "18:00" } = defenseSettings ?? {};
    const duration = currentSession.defenseDuration ?? 0;
    const breakDuration = currentSession.breakDuration ?? 0;
    const step = duration + breakDuration;
    const slots: string[] = [];
    let [h, m] = startTime.split(":").map(Number);
    const [endH, endM] = endTime.split(":").map(Number);
    const endMinutes = endH * 60 + endM;
    while (h * 60 + m + duration <= endMinutes) {
      slots.push(`${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`);
      m += step;
      if (m >= 60) {
        h += Math.floor(m / 60);
        m = m % 60;
      }
    }
    return slots;
  }, [currentSession, defenseSettings]);

  return {
    sessions: sessions ?? [],
    sessionsLoading,
    selectedSessionId,
    setSelectedSessionId,
    currentSession,
    days,
    timeSlots,
    defenseDuration: currentSession?.defenseDuration ?? 0,
  };
}
