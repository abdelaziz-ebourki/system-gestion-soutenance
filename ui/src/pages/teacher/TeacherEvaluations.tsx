import { useMemo } from "react";
import { FileCheck2, MessageSquareText, PencilLine, Loader2 } from "lucide-react";

import { useTeacherEvaluations } from "@/hooks/queries";
import { useEvaluationForm } from "@/hooks/use-evaluation-form";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import { EmptyState } from "@/components/ui/empty-state";
import { StatsCard } from "@/components/ui/stats-card";

export default function TeacherEvaluations() {
  const evaluationsQuery = useTeacherEvaluations();
  const evaluations = useMemo(() => evaluationsQuery.data?.items ?? [], [evaluationsQuery.data]);
  const isLoading = evaluationsQuery.isLoading;

  const form = useEvaluationForm();

  const pendingEvaluations = useMemo(() => evaluations.filter(
    (evaluation) => evaluation.status === "pending",
  ), [evaluations]);
  const submittedEvaluations = useMemo(() => evaluations.filter(
    (evaluation) => evaluation.status === "submitted",
  ), [evaluations]);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight" data-testid="teacher-evaluations-header">Évaluations</h1>
        <p className="text-muted-foreground" data-testid="teacher-evaluations-description">
          Gérez les notes et les appréciations des soutenances.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatsCard label="À compléter" value={pendingEvaluations.length} icon={PencilLine} data-testid="teacher-evaluations-stats-pending" />
        <StatsCard label="Soumises" value={submittedEvaluations.length} icon={FileCheck2} data-testid="teacher-evaluations-stats-submitted" />
        <StatsCard label="Commentaires" value={evaluations.filter((e) => e.comment).length} icon={MessageSquareText} data-testid="teacher-evaluations-stats-comments" />
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <Card data-testid="teacher-evaluations-pending-card">
          <CardHeader>
            <CardTitle>Évaluations en attente</CardTitle>
            <CardDescription>
              Saisissez une note et une appréciation pour chaque dossier.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {isLoading ? (
              <div className="flex flex-col gap-3">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="rounded-lg border p-4">
                    <div className="flex flex-col gap-2">
                      <Skeleton className="h-4 w-56" />
                      <Skeleton className="h-3 w-40" />
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              pendingEvaluations.map((evaluation) => (
                <div key={evaluation.id} className="rounded-lg border p-4" data-testid={`teacher-evaluations-pending-item-${evaluation.id}`}>
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <p className="font-medium">{evaluation.projectTitle}</p>
                    </div>
                  </div>
                  <div className="mt-4">
                    <Button onClick={() => form.openEdit(evaluation)} data-testid={`teacher-evaluations-pending-btn-${evaluation.id}`}>
                      Saisir l'évaluation
                    </Button>
                  </div>
                </div>
              ))
            )}
          </CardContent>
        </Card>

        <Card data-testid="teacher-evaluations-submitted-card">
          <CardHeader>
            <CardTitle>Historique récent</CardTitle>
            <CardDescription>
              Les évaluations déjà transmises au système.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {submittedEvaluations.length > 0 ? (
              submittedEvaluations.map((evaluation) => (
              <div key={evaluation.id} className="rounded-lg border p-4" data-testid={`teacher-evaluations-submitted-item-${evaluation.id}`}>
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="font-medium">{evaluation.projectTitle}</p>
                    <p className="mt-1 text-sm text-muted-foreground">
                      Note: {evaluation.finalGrade}/20
                    </p>
                  </div>
                </div>
                {evaluation.comment && (
                  <p className="mt-3 text-sm text-muted-foreground">
                    {evaluation.comment}
                  </p>
                )}
              </div>
            ))
            ) : (
              <EmptyState description="Aucune évaluation soumise." />
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog
        open={form.isDialogOpen}
        onOpenChange={form.setIsDialogOpen}
      >
        <DialogContent className="sm:max-w-lg" data-testid="teacher-evaluations-dialog">
          <DialogHeader>
            <DialogTitle data-testid="teacher-evaluations-dialog-title">Compléter une évaluation</DialogTitle>
            <DialogDescription data-testid="teacher-evaluations-dialog-description">
              Enregistrez votre note et votre appréciation pour ce dossier.
            </DialogDescription>
          </DialogHeader>
          <form
            id="teacher-evaluation-form"
            onSubmit={form.handleSubmit}
            data-testid="teacher-evaluations-form"
          >
            <FieldGroup>
              <div className="rounded-lg border bg-secondary/40 p-4">
                <p className="font-medium">{form.selected?.projectTitle}</p>
              </div>
              <Field>
                <FieldLabel htmlFor="teacher-score">Note / 20</FieldLabel>
                <Input
                  id="teacher-score"
                  type="number"
                  min="0"
                  max="20"
                  step="0.5"
                  value={String(form.formData.score)}
                  onChange={(event) => form.setFormData({ ...form.formData, score: Number(event.target.value) })}
                  required
                  error={form.fieldErrors?.score}
                  data-testid="teacher-evaluations-score"
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="teacher-comment">Appréciation</FieldLabel>
                <Textarea
                  id="teacher-comment"
                  value={form.formData.comment}
                  onChange={(event) => form.setFormData({ ...form.formData, comment: event.target.value })}
                  className="min-h-28"
                  required
                  data-testid="teacher-evaluations-comment"
                />
              </Field>
            </FieldGroup>
          </form>
          <DialogFooter>
            <Button
              type="submit"
              form="teacher-evaluation-form"
              disabled={form.isPending}
              data-testid="teacher-evaluations-save"
            >
              {form.isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
              {form.isPending ? "Enregistrement..." : "Enregistrer"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

