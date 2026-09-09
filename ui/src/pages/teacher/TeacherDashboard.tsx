import { useMemo } from "react";
import { ClipboardCheck, Clock3, FileCheck2, ShieldCheck } from "lucide-react";

import { useTeacherStats, useTeacherSchedule, useTeacherEvaluations } from "@/hooks/queries";


import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { StatsCard } from "@/components/ui/stats-card";

export default function TeacherDashboard() {
  const statsQuery = useTeacherStats();
  const scheduleQuery = useTeacherSchedule();
  const evaluationsQuery = useTeacherEvaluations();
  const stats = statsQuery.data ?? null;
  const schedule = useMemo(() => scheduleQuery.data?.slots ?? [], [scheduleQuery.data]);
  const evaluations = useMemo(() => evaluationsQuery.data?.items ?? [], [evaluationsQuery.data]);
  const isLoading = statsQuery.isLoading || scheduleQuery.isLoading || evaluationsQuery.isLoading;

  const upcomingDefenses = useMemo(() => schedule.slice(0, 3), [schedule]);
  const pendingEvaluations = useMemo(() => evaluations.filter(
    (evaluation) => evaluation.status === "pending",
  ), [evaluations]);

  return (
    <div className="flex flex-col gap-6">
      <section className="rounded-lg border bg-card">
        <div className="grid gap-6 px-6 py-8 md:grid-cols-[1.4fr_1fr] md:px-8">
          <div className="flex flex-col gap-4">
            <Badge className="w-fit" variant="secondary" data-testid="teacher-dashboard-hero-badge">
              Session de soutenance
            </Badge>
            <div className="flex flex-col gap-2">
              <h1 className="text-3xl font-bold tracking-tight md:text-4xl" data-testid="teacher-dashboard-hero-title">
                Un espace enseignant clair pour suivre jurys, planning et notes.
              </h1>
              <p className="max-w-2xl text-sm text-muted-foreground md:text-base" data-testid="teacher-dashboard-hero-description">
                Retrouvez vos passages à venir, les évaluations à rendre et vos
                indisponibilités depuis un point d'entrée unique.
              </p>
            </div>
          </div>

          <Card className="bg-secondary/40 shadow-none" data-testid="teacher-dashboard-quick-view">
            <CardHeader className="pb-3">
              <CardTitle className="text-base">Vue rapide</CardTitle>
              <CardDescription>
                Les indicateurs utiles avant le début des soutenances.
              </CardDescription>
            </CardHeader>
            <CardContent className="grid gap-3 sm:grid-cols-2">
              <div className="rounded-lg border bg-background/80 p-4">
                <p className="text-xs uppercase tracking-widest text-muted-foreground">
                  À venir
                </p>
                <div className="mt-2 text-2xl font-semibold">
                  {isLoading ? (
                    <Skeleton className="h-8 w-12" />
                  ) : (
                    stats?.upcomingDefenses
                  )}
                </div>
              </div>
              <div className="rounded-lg border bg-background/80 p-4">
                <p className="text-xs uppercase tracking-widest text-muted-foreground">
                  Notes
                </p>
                <div className="mt-2 text-2xl font-semibold">
                  {isLoading ? (
                    <Skeleton className="h-8 w-12" />
                  ) : (
                    stats?.pendingEvaluations
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatsCard label="Soutenances à venir" value={stats?.upcomingDefenses} icon={Clock3} loading={isLoading} data-testid="teacher-dashboard-stats-upcoming" />
        <StatsCard label="Évaluations en attente" value={stats?.pendingEvaluations} icon={FileCheck2} loading={isLoading} data-testid="teacher-dashboard-stats-pending" />
        <StatsCard label="Créneaux bloqués" value={stats?.declaredUnavailabilitySlots} icon={ClipboardCheck} loading={isLoading} data-testid="teacher-dashboard-stats-unavailability" />
        <StatsCard label="Jurys assignés" value={stats?.juryAssignments} icon={ShieldCheck} loading={isLoading} data-testid="teacher-dashboard-stats-juries" />
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <Card data-testid="teacher-dashboard-upcoming-card">
          <CardHeader>
            <CardTitle>Prochaines soutenances</CardTitle>
            <CardDescription>
              Les passages où votre présence est requise prochainement.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {upcomingDefenses.length > 0 ? (
              upcomingDefenses.map((defense, index) => (
              <div key={`${defense.date}-${defense.time}-${index}`} className="rounded-lg border p-4" data-testid={`teacher-dashboard-upcoming-item-${index}`}>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="font-medium">{defense.projectTitle}</p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {defense.studentNames.join(", ")}
                    </p>
                  </div>
                </div>
                <div className="mt-3 text-sm text-muted-foreground">
                  {defense.date} · {defense.time} ·{" "}
                  {defense.roomName}
                </div>
              </div>
            ))
            ) : (
              <EmptyState description="Aucune soutenance prévue." />
            )}
          </CardContent>
        </Card>

        <Card data-testid="teacher-dashboard-evaluations-card">
          <CardHeader>
            <CardTitle>Évaluations à rendre</CardTitle>
            <CardDescription>
              Les dossiers qui demandent encore une note ou un commentaire.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {pendingEvaluations.map((evaluation) => (
              <div key={evaluation.id} className="rounded-lg border p-4" data-testid={`teacher-dashboard-evaluations-item-${evaluation.id}`}>
                <p className="font-medium">{evaluation.projectTitle}</p>
              </div>
            ))}
            {!isLoading && pendingEvaluations.length === 0 && (
              <EmptyState variant="card" description="Aucune évaluation en attente." />
            )}
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

