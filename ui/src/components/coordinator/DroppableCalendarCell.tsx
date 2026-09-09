import { useDroppable } from "@dnd-kit/core";
import { cn } from "@/lib/utils";
import { X, Users } from "lucide-react";
import type { Jury } from "@/types";
import { Badge } from "@/components/ui/badge";

interface DroppableCalendarCellProps {
  id: string;
  jury: Jury | null;
  studentNames: string[];
  onRemove: () => void;
}

const DEFENSE_LABELS: Record<string, string> = {
  pfe: "PFE",
  memoire: "Mémoire",
  these: "Thèse",
};

const DEFENSE_COLORS: Record<string, string> = {
  pfe: "bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300",
  memoire: "bg-purple-100 text-purple-700 dark:bg-purple-900/40 dark:text-purple-300",
  these: "bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300",
};

export default function DroppableCalendarCell({ id, jury, studentNames, onRemove }: DroppableCalendarCellProps) {
  const { setNodeRef, isOver } = useDroppable({ id });

  return (
    <td
      ref={setNodeRef}
      className={cn("relative h-25 w-[180px] border p-1 transition-all duration-200", jury ? "bg-primary/5" : "bg-white/[0.02]", isOver && "bg-primary/10 ring-2 ring-primary/60 scale-[1.02] z-10")}
      data-testid={`coord-cell-${id}`}
    >
      <div className="h-full w-full overflow-hidden">
        {jury ? (
          <div className="group relative h-full rounded-lg bg-gradient-to-br from-primary/15 to-primary/5 p-1.5 text-xs shadow-sm">
            <button
              onClick={onRemove}
              className="absolute right-1 top-1 z-10 rounded p-0.5 opacity-0 transition-opacity hover:bg-destructive/20 group-hover:opacity-100"
              data-testid={`coord-cell-remove-${id}`}
            >
              <X className="text-destructive" />
            </button>

            <span className="line-clamp-2 font-medium leading-tight text-foreground/90 pr-4">
              {jury.projectTitle}
            </span>

            <span className={`mt-0.5 inline-block rounded px-1 py-[1px] text-[9px] font-semibold leading-normal ${DEFENSE_COLORS[jury.defenseType] ?? "bg-muted text-muted-foreground"}`}>
              {DEFENSE_LABELS[jury.defenseType] ?? jury.defenseType}
            </span>

            {studentNames.length > 0 && (
              <div className="mt-1 flex items-center gap-1 text-[10px] text-muted-foreground truncate">
                <Users className="shrink-0" />
                <span className="truncate">{studentNames.join(", ")}</span>
              </div>
            )}

            {jury.members && jury.members.length > 0 && (
              <div className="mt-1 flex flex-wrap gap-0.5">
                {jury.members.slice(0, 3).map((m) => (
                  <Badge key={m.teacherId} variant="outline" className="text-[7px] px-1 py-0 h-3.5 leading-none">
                    {m.teacherName.split(" ").pop()}
                  </Badge>
                ))}
                {jury.members.length > 3 && (
                  <span className="text-[7px] text-muted-foreground">+{jury.members.length - 3}</span>
                )}
              </div>
            )}
          </div>
        ) : (
          <div
            className={cn("flex h-full w-full cursor-default items-center justify-center rounded-lg border-2 border-dashed transition-all duration-200", isOver ? "border-primary/60 bg-primary/10 text-primary shadow-lg" : "border-muted-foreground/25 bg-muted/15 text-muted-foreground/60 hover:border-muted-foreground/40 hover:bg-muted/25")}
          >
            <span className="relative flex size-6 items-center justify-center rounded-full border border-current text-xs font-light leading-none">
              +
            </span>
          </div>
        )}
      </div>
    </td>
  );
}
