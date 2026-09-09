import { useState, useMemo } from "react";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { MapPin, ChevronLeft, ChevronRight } from "lucide-react";
import { createSlotKey } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import DroppableCalendarCell from "@/components/coordinator/DroppableCalendarCell";
import type { Jury, Room, Project } from "@/types";

interface DefenseCalendarProps {
  days: Date[];
  timeSlots: string[];
  schedule: Record<string, { roomId: number; date: string; time: string }>;
  juries: Jury[];
  onRemove: (juryId: number) => void;
  rooms: Room[];
  defenseDuration: number;
  projects: Project[];
}

const ROOMS_PER_PAGE = 3;

function formatTimeRange(start: string, durationMinutes: number): string {
  const [h, m] = start.split(":").map(Number);
  const totalMinutes = h * 60 + m + durationMinutes;
  const endH = Math.floor(totalMinutes / 60);
  const endM = totalMinutes % 60;
  return `${start} – ${String(endH).padStart(2, "0")}:${String(endM).padStart(2, "0")}`;
}

export default function DefenseCalendar({
  days,
  timeSlots,
  schedule,
  juries,
  onRemove,
  rooms,
  defenseDuration,
  projects,
}: DefenseCalendarProps) {
  const [roomPage, setRoomPage] = useState(0);
  const [selectedDayIndex, setSelectedDayIndex] = useState(0);

  const totalPages = Math.max(1, Math.ceil(rooms.length / ROOMS_PER_PAGE));
  const startIndex = Math.min(roomPage * ROOMS_PER_PAGE, Math.max(0, rooms.length - ROOMS_PER_PAGE));
  const paginatedRooms = rooms.slice(startIndex, startIndex + ROOMS_PER_PAGE);

  const selectedDay = days[selectedDayIndex] ?? null;
  const dateStr = selectedDay ? format(selectedDay, "yyyy-MM-dd") : null;

  const scheduleLookup = useMemo(() => {
    const map = new Map<string, string>();
    for (const [juryId, entry] of Object.entries(schedule)) {
      const key = createSlotKey(entry.date, String(entry.roomId), entry.time);
      map.set(key, juryId);
    }
    return map;
  }, [schedule]);

  const juryLookup = useMemo(() => {
    const map = new Map<string, Jury>();
    for (const jury of juries) {
      map.set(String(jury.id), jury);
    }
    return map;
  }, [juries]);

  const projectLookup = useMemo(() => {
    const map = new Map<number, Project>();
    for (const project of projects) {
      map.set(project.id, project);
    }
    return map;
  }, [projects]);

  if (!rooms.length) {
    return (
      <div className="h-[500px] border-2 border-dashed rounded-3xl flex flex-col items-center justify-center text-muted-foreground gap-4 bg-muted/5" data-testid="coord-calendar-no-room">
        <div className="p-4 rounded-full bg-muted/20">
          <MapPin className="size-10 opacity-20" />
        </div>
        <p className="text-lg">Aucune salle disponible</p>
      </div>
    );
  }

  return (
    <div className="gap-4 h-full flex flex-col" data-testid="coord-calendar">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon"
            className="size-7"
            disabled={selectedDayIndex <= 0}
            onClick={() => setSelectedDayIndex((i) => Math.max(0, i - 1))}
            data-testid="coord-calendar-day-prev"
          >
            <ChevronLeft />
          </Button>
          {selectedDay && (
            <span className="text-sm font-medium min-w-[200px] text-center tabular-nums">
              {format(selectedDay, "EEEE dd MMMM yyyy", { locale: fr })}
            </span>
          )}
          <Button
            variant="outline"
            size="icon"
            className="size-7"
            disabled={selectedDayIndex >= days.length - 1}
            onClick={() => setSelectedDayIndex((i) => Math.min(days.length - 1, i + 1))}
            data-testid="coord-calendar-day-next"
          >
            <ChevronRight />
          </Button>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-muted-foreground">Salles:</span>
          <Button
            variant="outline"
            size="icon"
            className="size-7"
            disabled={roomPage === 0}
            onClick={() => setRoomPage((p) => Math.max(0, p - 1))}
            data-testid="coord-calendar-rooms-prev"
          >
            <ChevronLeft />
          </Button>
          <span className="text-xs text-muted-foreground tabular-nums min-w-[4rem] text-center">
            {startIndex + 1}–{Math.min(startIndex + ROOMS_PER_PAGE, rooms.length)} / {rooms.length}
          </span>
          <Button
            variant="outline"
            size="icon"
            className="size-7"
            disabled={roomPage >= totalPages - 1}
            onClick={() => setRoomPage((p) => Math.min(totalPages - 1, p + 1))}
            data-testid="coord-calendar-rooms-next"
          >
            <ChevronRight />
          </Button>
        </div>
      </div>

      {!selectedDay && (
        <div className="h-[200px] border-2 border-dashed rounded-3xl flex items-center justify-center text-muted-foreground">
          Aucun jour disponible dans cette session.
        </div>
      )}

      {selectedDay && dateStr && (
        <div className="flex-1 overflow-auto rounded-lg border">
          <table className="w-full table-fixed border-collapse" data-testid="coord-calendar-table">
            <thead>
              <tr className="bg-muted/50">
                <th className="p-3 border text-left font-medium text-xs uppercase tracking-wider text-muted-foreground w-36">
                  Horaire
                </th>
                {paginatedRooms.map((room) => (
                  <th
                    key={room.id}
                    className="p-3 border text-center font-medium text-sm min-w-[180px]"
                    data-testid={`coord-room-column-${room.id}`}
                  >
                    <div className="flex items-center justify-center gap-1.5">
                      <MapPin className="text-muted-foreground" />
                      {room.name}
                    </div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {timeSlots.map((slot) => (
                <tr key={slot}>
                  <td className="p-2 border font-mono text-xs font-medium bg-muted/20 text-center text-muted-foreground/70 whitespace-nowrap">
                    {formatTimeRange(slot, defenseDuration)}
                  </td>
                  {paginatedRooms.map((room) => {
                    const key = createSlotKey(dateStr, String(room.id), slot);
                    const scheduledJuryId = scheduleLookup.get(key);
                    const jury = scheduledJuryId ? juryLookup.get(scheduledJuryId) ?? null : null;

                    return (
                      <DroppableCalendarCell
                        key={`${room.id}|${key}`}
                        id={key}
                        jury={jury}
                        studentNames={jury ? projectLookup.get(jury.projectId)?.studentNames ?? [] : []}
                        onRemove={() => jury && onRemove(jury.id)}
                      />
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
