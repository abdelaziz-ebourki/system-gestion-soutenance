import { useDroppable } from "@dnd-kit/core";
import { Users, X } from "lucide-react";

import type { Project, Jury } from "@/types";
import type { ScheduleMode } from "./ModeToggle";

export type ScheduledCard = {
  id: number;
  title: string;
  roomName: string;
  date: string;
  time: string;
};

interface SlotRowProps {
  slotKey: string;
  time: string;
  scheduled: ScheduledCard | undefined;
  projects: Project[];
  juries: Jury[];
  mode: ScheduleMode;
  selectedProjectId: number | null;
  onPlace: (slotKey: string) => void;
  onRemove: (slotKey: string) => void;
}

export function SlotRow({
  slotKey,
  time,
  scheduled,
  projects,
  juries,
  mode,
  selectedProjectId,
  onPlace,
  onRemove,
}: SlotRowProps) {
  const { setNodeRef, isOver } = useDroppable({
    id: slotKey,
    data: { type: "slot", slotKey },
  });

  const showDropHighlight = mode === "dnd" && isOver;
  const showClickHighlight = mode === "click" && selectedProjectId && !scheduled;
  const isInteractive = !scheduled && (mode === "click" ? !!selectedProjectId : true);

  const scheduledProject = scheduled
    ? projects.find((p) => p.id === scheduled.id)
    : null;
  const jury = scheduled
    ? juries.find((j) => j.projectId === scheduled.id)
    : null;

  return (
    <div
      ref={setNodeRef}
      onClick={() => {
        if (isInteractive && mode === "click") onPlace(slotKey);
      }}
      className={
        showDropHighlight
          ? "min-h-32 rounded-xl border-2 border-dashed border-primary bg-primary/5 p-4 transition"
          : showClickHighlight
            ? "min-h-32 cursor-pointer rounded-xl border-2 border-dashed border-primary bg-primary/5 p-4 transition"
            : scheduled
              ? "min-h-32 rounded-xl border-2 border-dashed border-border bg-card p-4 transition"
              : "min-h-32 rounded-xl border-2 border-dashed border-border bg-card p-4 transition"
      }
      data-testid={`coord-slot-${slotKey}`}
    >
      <div className="flex items-center justify-between text-sm text-muted-foreground">
        <span data-testid={`coord-slot-time-${slotKey}`}>{time}</span>
        <span data-testid={`coord-slot-status-${slotKey}`}>{scheduled ? "Occupé" : "Libre"}</span>
      </div>

      {scheduled && (
        <div className="mt-4 rounded-lg border bg-primary p-4 text-primary-foreground">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="font-medium">{scheduled.title}</p>
              <p className="mt-1 text-sm text-primary-foreground/80">
                {scheduledProject?.studentNames?.join(", ") || "Groupe non renseigné"}
              </p>
            </div>
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onRemove(slotKey);
              }}
              className="rounded-full p-1 text-primary-foreground/70 transition hover:bg-primary-foreground/10 hover:text-primary-foreground"
              data-testid={`coord-slot-remove-${slotKey}`}
            >
              <X />
            </button>
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-primary-foreground/80">
             <Users className="shrink-0" />
             {jury?.members?.map((m) => `${m.roleName}: ${m.teacherName}`).join(" | ") ?? ""}
          </div>
        </div>
      )}
    </div>
  );
}
