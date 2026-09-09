import { useMemo } from "react";
import * as React from "react";
import { Ban, CalendarClock, Save, Loader2 } from "lucide-react";

import { useTeacherSchedule, useTeacherUnavailability, useSaveTeacherUnavailability } from "@/hooks/queries";
import type { TeacherUnavailability } from "@/types";
import { unavailabilitySchema, validate } from "@/lib/validations";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import AvailabilityCalendar from "@/components/coordinator/AvailabilityCalendar";
import { Button } from "@/components/ui/button";
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { StatsCard } from "@/components/ui/stats-card";

type CalendarSession = {
  dateKey: string;
  time: string;
  student: string;
  room: string;
};

export default function TeacherUnavailability() {
  const scheduleQuery = useTeacherSchedule();
  const unavailabilityQuery = useTeacherUnavailability();
  const saveMutation = useSaveTeacherUnavailability();
  const schedule = useMemo(() => scheduleQuery.data?.slots ?? [], [scheduleQuery.data]);
  const isLoading = scheduleQuery.isLoading || unavailabilityQuery.isLoading;
  const [unavailability, setUnavailability] =
    React.useState<TeacherUnavailability>({
      slotsByDate: {},
    });

  React.useEffect(() => {
    if (unavailabilityQuery.data) {
      setUnavailability(unavailabilityQuery.data);
    }
  }, [unavailabilityQuery.data]);

  const handleToggleSlot = (dateKey: string, slot: string) => {
    setUnavailability((current) => {
      const currentSlots = current.slotsByDate[dateKey] || [];
      const nextSlots = currentSlots.includes(slot)
        ? currentSlots.filter((item) => item !== slot)
        : [...currentSlots, slot];
      const nextState = { ...current.slotsByDate };

      if (nextSlots.length === 0) {
        delete nextState[dateKey];
      } else {
        nextState[dateKey] = nextSlots;
      }

      return { slotsByDate: nextState };
    });
  };

  const handleSave = async () => {
    const errors = validate(unavailabilitySchema, unavailability);
    if (errors) {
      toast.error("Données d'indisponibilité invalides");
      return;
    }
    try {
      const slots = Object.entries(unavailability.slotsByDate).map(
        ([date, slots]) => ({ date, slots }),
      );
      await saveMutation.mutateAsync({ slots });
      toast.success("Indisponibilités enregistrées");
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de l'enregistrement"));
    }
  };

  const totalBlockedSlots = Object.values(unavailability.slotsByDate).reduce(
    (total, slots) => total + slots.length,
    0,
  );

  const sessions: CalendarSession[] = useMemo(() => schedule.map((defense) => ({
    dateKey: defense.date,
    time: defense.time,
    student: defense.studentNames.join(", "),
    room: defense.roomName,
  })), [schedule]);

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight" data-testid="teacher-unavailability-header">
            Mes indisponibilités
          </h1>
          <p className="text-muted-foreground" data-testid="teacher-unavailability-description">
            Gérez vos créneaux d'indisponibilité pour la planification des
            soutenances.
          </p>
        </div>
        <Button
          onClick={handleSave}
          disabled={saveMutation.isPending}
          data-testid="teacher-unavailability-save"
        >
          {saveMutation.isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
          {saveMutation.isPending ? "Enregistrement..." : <><Save data-icon="inline-start" /> Enregistrer</>}
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatsCard label="Créneaux bloqués" value={totalBlockedSlots} icon={Ban} data-testid="teacher-unavailability-stats-blocked" />
        <StatsCard label="Jours concernés" value={Object.keys(unavailability.slotsByDate).length} icon={CalendarClock} data-testid="teacher-unavailability-stats-days" />
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Rappel</CardTitle>
            <CardDescription>
              Les créneaux déjà associés à une soutenance sont verrouillés.
            </CardDescription>
          </CardHeader>
        </Card>
      </div>

      {isLoading ? (
        <div className="flex flex-col gap-4">
          <Skeleton className="h-96 w-full rounded-xl" />
        </div>
      ) : (
        <AvailabilityCalendar
          unavailableSlots={unavailability.slotsByDate}
          onToggleSlot={handleToggleSlot}
          sessions={sessions}
          onSave={handleSave}
        />
      )}
    </div>
  );
}
