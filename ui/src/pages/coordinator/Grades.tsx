import { FileCheck2, Clock, AlertCircle } from "lucide-react";

import { useProjectGrades } from "@/hooks/queries";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { StatsCard } from "@/components/ui/stats-card";

const STATUS_LABELS: Record<string, string> = {
  completed: "Complété",
  pending: "En attente",
  no_evaluations: "Non évalué",
};

const STATUS_BADGE: Record<string, "default" | "secondary" | "outline"> = {
  completed: "default",
  pending: "secondary",
  no_evaluations: "outline",
};

export default function Grades() {
  const { data: gradesData, isLoading } = useProjectGrades();
  const grades = gradesData ?? [];

  const completed = grades.filter((g) => g.status === "completed").length;

  return (
    <div className="flex flex-col gap-6" data-testid="coord-grades-page">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Notes</h1>
        <p className="text-muted-foreground">
          Notes finales calculées à partir des évaluations des jurys.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatsCard label="Projets notés" value={completed} icon={FileCheck2} />
        <StatsCard label="En attente" value={grades.filter((g) => g.status === "pending").length} icon={Clock} />
        <StatsCard label="Non évalués" value={grades.filter((g) => g.status === "no_evaluations").length} icon={AlertCircle} />
      </div>

      {isLoading ? (
        <div className="grid gap-3">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-24 rounded-xl" />
          ))}
        </div>
      ) : grades.length === 0 ? (
        <Card>
          <CardContent>
            <EmptyState description="Aucune évaluation disponible pour le moment." />
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-3">
          {grades.map((grade) => (
            <Card key={grade.projectId}>
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="text-base">{grade.projectTitle}</CardTitle>
                    <CardDescription>
                      {grade.defenseDate ? `Soutenu le ${grade.defenseDate}` : "Pas encore soutenu"}
                    </CardDescription>
                  </div>
                  <Badge variant={STATUS_BADGE[grade.status]}>
                    {STATUS_LABELS[grade.status]}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex flex-col gap-2">
                  {grade.status === "completed" && grade.finalScore !== null && (
                    <div className="flex items-center justify-between rounded-lg border bg-muted/50 p-3">
                      <span className="font-medium">Note finale</span>
                      <span className="text-2xl font-bold">{grade.finalScore}/20</span>
                    </div>
                  )}
                  {grade.individualScores.length > 0 && (
                    <div className="flex flex-col gap-1">
                      {grade.individualScores.map((item, i) => (
                        <div key={i} className="flex items-center justify-between rounded-md border px-3 py-1.5 text-sm">
                          <div className="flex items-center gap-2">
                            <Badge variant="outline" className="text-xs">{item.roleName}</Badge>
                            <span className="text-muted-foreground">{item.teacherName}</span>
                          </div>
                          <span className="font-medium">
                            {item.score !== undefined ? `${item.score}/20` : "—"}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                  {Object.keys(grade.evaluationCoefficients).length > 0 && (
                    <div className="flex flex-wrap gap-1 text-xs text-muted-foreground">
                      {Object.entries(grade.evaluationCoefficients).map(([role, coeff]) => (
                        <span key={role} className="rounded bg-muted px-1.5 py-0.5">
                          {role}: {coeff}%
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

