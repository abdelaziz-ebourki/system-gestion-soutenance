import { useMemo } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  BookOpen,
  CalendarDays,
  ClipboardCheck,
  Clock3,
  Users,
} from "lucide-react";

import { useCoordinatorStats, useProjects, useJuries } from "@/hooks/queries";

import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/ui/empty-state";
import { Skeleton } from "@/components/ui/skeleton";
import { StatsCard } from "@/components/ui/stats-card";

const actionCards = [
  {
    title: "Projets & Groupes",
    description: "Affectations, encadrants et suivi des depots.",
    to: "/coordinator/projects",
    icon: BookOpen,
  },
  {
    title: "Jurys",
    description: "Composer les jurys et équilibrer les rôles.",
    to: "/coordinator/juries",
    icon: Users,
  },
  {
    title: "Planification",
    description: "Orchestrer les salles, créneaux et passages.",
    to: "/coordinator/schedule",
    icon: CalendarDays,
  },
];

export default function CoordinatorDashboard() {
  const statsQuery = useCoordinatorStats();
  const projectsQuery = useProjects();
  const juriesQuery = useJuries();
  const stats = statsQuery.data;
  const statsLoading = statsQuery.isLoading;
  const projects = useMemo(() => projectsQuery.data?.items ?? [], [projectsQuery.data]);
  const juries = useMemo(() => juriesQuery.data?.items ?? [], [juriesQuery.data]);
  const contentLoading = projectsQuery.isLoading || juriesQuery.isLoading;

  const projectsWithJury = useMemo(() => projects.filter(
    (project) => juries.some((jury) => jury.projectId === project.id),
  ), [projects, juries]);
  const projectsWithoutJury = useMemo(() => projects.filter(
    (project) => !juries.some((jury) => jury.projectId === project.id),
  ), [projects, juries]);
  const juryCoverage = useMemo(() =>
    projects.length > 0
      ? Math.round((juries.length / projects.length) * 100)
      : 0,
  [projects.length, juries.length]);

  return (
    <div className="flex flex-col gap-6" data-testid="coord-dashboard-page">
      <section className="overflow-hidden rounded-3xl border bg-card shadow-sm" data-testid="coord-dashboard-hero">
        <div className="grid gap-6 px-6 py-8 md:grid-cols-[1.5fr_1fr] md:px-8">
          <div className="flex flex-col gap-4">
            <Badge className="w-fit" variant="secondary">
              Campagne de soutenance {new Date().getFullYear()}
            </Badge>
            <div className="flex flex-col gap-2">
              <h1 className="max-w-2xl text-3xl font-bold tracking-tight md:text-4xl">
                Coordination des soutenances, pensee comme une salle de
                contrôle.
              </h1>
              <p className="max-w-2xl text-sm text-muted-foreground md:text-base">
                Suivez la progression des projets, la couverture des jurys et la
                preparation du planning depuis un seul point d'entree.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Link
                to="/coordinator/schedule"
                className={buttonVariants({ size: "lg" })}
                data-testid="coord-dashboard-open-planner"
              >
                Ouvrir le planificateur
              </Link>
              <Link
                to="/coordinator/juries"
                className={buttonVariants({ variant: "outline", size: "lg" })}
                data-testid="coord-dashboard-check-juries"
              >
                Verifier les jurys
              </Link>
            </div>
          </div>

          <Card className="bg-secondary/40 shadow-none">
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center gap-2 text-base">
                État de preparation
              </CardTitle>
              <CardDescription>
                Les points qui demandent encore une intervention.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              <div className="rounded-lg border bg-background/80 p-4">
                <div className="flex items-center justify-between text-sm text-muted-foreground">
                  <span>Couverture des jurys</span>
                  <span>{contentLoading ? <Skeleton className="h-4 w-8 inline-block" /> : `${juryCoverage}%`}</span>
                </div>
                <div className="mt-3 h-2 rounded-full bg-secondary">
                    <div
                      className="h-2 rounded-full bg-primary transition-all"
                      style={{ width: `${Math.min(juryCoverage, 100)}%` }}
                      data-testid="coord-dashboard-jury-coverage"
                    />
                </div>
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <div className="rounded-lg border bg-background/80 p-4">
                  <p className="text-xs uppercase tracking-widest text-muted-foreground">
                    Prêts
                  </p>
                  <div className="mt-2 text-2xl font-semibold">
                    {contentLoading ? <Skeleton className="h-7 w-8" /> : projectsWithJury.length}
                  </div>
                  <p className="mt-1 text-sm text-muted-foreground">
                    Projets avec jury
                  </p>
                </div>
                <div className="rounded-lg border bg-background/80 p-4">
                  <p className="text-xs uppercase tracking-widest text-muted-foreground">
                    À completer
                  </p>
                  <div className="mt-2 text-2xl font-semibold">
                    {contentLoading ? <Skeleton className="h-7 w-8" /> : projectsWithoutJury.length}
                  </div>
                  <p className="mt-1 text-sm text-muted-foreground">
                    Projets sans jury
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4" data-testid="coord-dashboard-stats">
        <StatsCard label="Projets" value={stats?.totalProjects} icon={BookOpen} loading={statsLoading} />
        <StatsCard label="Groupes" value={stats?.totalGroups} icon={Users} loading={statsLoading} />
        <StatsCard label="Jurys" value={stats?.totalJuries} icon={ClipboardCheck} loading={statsLoading} />
        <StatsCard label="Créneaux planifiés" value={stats?.scheduledDefenses} icon={Clock3} loading={statsLoading} />
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]">
        <Card data-testid="coord-dashboard-quick-access">
          <CardHeader>
            <CardTitle>Acces directs</CardTitle>
            <CardDescription>
              Des points d'entree clairs pour boucler la campagne plus vite.
            </CardDescription>
          </CardHeader>
          <CardContent className="grid gap-4 md:grid-cols-3">
            {actionCards.map((card) => (
              <Link
                key={card.title}
                to={card.to}
                className="group rounded-xl border bg-card p-5 transition hover:-translate-y-0.5"
              >
                <div className="mb-4 inline-flex rounded-lg bg-secondary p-3 text-primary">
                  <card.icon />
                </div>
                <div className="flex flex-col gap-2">
                  <h2 className="font-semibold">{card.title}</h2>
                  <p className="text-sm text-muted-foreground">
                    {card.description}
                  </p>
                </div>
                <div className="mt-6 flex items-center text-sm font-medium text-foreground">
                  Ouvrir
                  <ArrowRight data-icon="inline-end" className="transition group-hover:translate-x-1" />
                </div>
              </Link>
            ))}
          </CardContent>
        </Card>

        <Card data-testid="coord-dashboard-attention-points">
          <CardHeader>
            <CardTitle>Points d'attention</CardTitle>
            <CardDescription>
              Les dossiers qui bloquent le passage a la planification finale.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {contentLoading ? (
              <>
                {[1, 2, 3].map((i) => (
                  <div key={i} className="rounded-lg border border-dashed p-4">
                    <Skeleton className="h-4 w-3/4" />
                    <Skeleton className="h-3 w-1/2 mt-2" />
                  </div>
                ))}
              </>
            ) : projectsWithoutJury.slice(0, 4).map((project) => (
              <div
                key={project.id}
                className="rounded-lg border border-dashed p-4"
              >
                <p className="font-medium">{project.title}</p>
                <p className="mt-1 text-sm text-muted-foreground">
                  Encadrant: {project.supervisorName}
                </p>
              </div>
            ))}
            {!contentLoading && projectsWithoutJury.length === 0 && (
              <EmptyState variant="card" description="Tous les projets disposent déjà d'un jury." />
            )}
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

