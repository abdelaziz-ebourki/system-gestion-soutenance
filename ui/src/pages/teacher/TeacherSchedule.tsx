import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { CalendarDays, MapPin, Timer } from "lucide-react";

import { useTeacherSchedule } from "@/hooks/queries";
import type { TeacherDefense } from "@/types";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { StatsCard } from "@/components/ui/stats-card";
import { DataTable } from "@/components/ui/data-table";

export default function TeacherSchedule() {
  const scheduleQuery = useTeacherSchedule();
  const schedule = useMemo(() => scheduleQuery.data?.slots ?? [], [scheduleQuery.data]);
  const isLoading = scheduleQuery.isLoading;

  const columns: ColumnDef<TeacherDefense>[] = [
    {
      accessorKey: "projectTitle",
      header: "Projet",
      cell: ({ row }) => (
        <div className="flex flex-col gap-1">
          <div className="font-medium">{row.original.projectTitle}</div>
          <div className="text-xs text-muted-foreground">
            {row.original.studentNames.join(", ")}
          </div>
        </div>
      ),
    },
    {
      accessorKey: "date",
      header: "Date",
    },
    {
      accessorKey: "time",
      header: "Horaire",
    },
    {
      accessorKey: "roomName",
      header: "Salle",
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight" data-testid="teacher-schedule-header">Mon planning</h1>
        <p className="text-muted-foreground" data-testid="teacher-schedule-description">
          Voici le planning des soutenances auxquelles vous participez.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatsCard label="Soutenances à venir" value={schedule.length} icon={CalendarDays} data-testid="teacher-schedule-stats-upcoming" />
        <StatsCard label="Créneaux" value={schedule.length} icon={Timer} data-testid="teacher-schedule-stats-jury" />
        <StatsCard label="Salles uniques" value={new Set(schedule.map((d) => d.roomName)).size} icon={MapPin} data-testid="teacher-schedule-stats-supervisor" />
      </div>

      <Card data-testid="teacher-schedule-table-card">
        <CardHeader>
          <CardTitle>Planning détaillé</CardTitle>
          <CardDescription>
            Chaque passage indique la salle et le groupe
            d'étudiants.
          </CardDescription>
        </CardHeader>
        <CardContent>
            <DataTable
              columns={columns}
              data={schedule}
              loading={isLoading}
              filterColumns="projectTitle"
              filterPlaceholder="Rechercher un projet..."
            />
        </CardContent>
      </Card>

      {schedule.length > 0 ? (
        <div className="grid gap-4 lg:grid-cols-2">
          {schedule.map((defense, index) => (
            <Card key={`${defense.date}-${defense.time}-${index}`} data-testid={`teacher-schedule-card-${index}`}>
              <CardContent className="flex items-start justify-between gap-4 p-5">
                <div>
                  <p className="font-medium">{defense.projectTitle}</p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {defense.studentNames.join(", ")}
                  </p>
                  <div className="mt-3 flex flex-wrap gap-3 text-sm text-muted-foreground">
                    <span>{defense.date}</span>
                    <span>{defense.time}</span>
                    <span className="inline-flex items-center gap-1">
                      <MapPin />
                      {defense.roomName}
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <EmptyState description="Aucune soutenance programmée pour la période sélectionnée." />
      )}
    </div>
  );
}

