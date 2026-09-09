import { useState } from "react";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { ArrowRight, Calendar, ShieldCheck, Clock, FileText, CheckCircle2, Plus, Loader2 } from "lucide-react";
import { format, parseISO } from "date-fns";
import { fr } from "date-fns/locale";
import type { DefenseSession, DefenseSessionStatus, DefenseType, JuryRoleTemplate } from "@/types";
import {
  useCoordinatorDefenseSessions,
  useTransitionDefenseSession,
  useCreateDefenseSession,
  useUpdateDefenseSession,
  useDeleteDefenseSession,
  useJuryRoleTemplates,
} from "@/hooks/queries";
import { useEntityForm } from "@/hooks/use-entity-form";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { EmptyState } from "@/components/ui/empty-state";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import {
  DEFENSE_SESSION_STATUS_LABELS,
  DEFENSE_SESSION_STATUS_BADGE,
  DEFENSE_SESSION_LIFECYCLE,
  DEFENSE_TYPE_OPTIONS,
  DEFENSE_TYPE_SHORT_LABELS,
} from "@/lib/constants";
import { defenseSessionSchema } from "@/lib/validations";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { CrudActions } from "@/components/admin/CrudActions";

const DEFAULT_DURATION_BY_TYPE: Record<DefenseType, number> = {
  pfe: 30,
  memoire: 20,
  these: 45,
};

const defaultForm = {
  name: "",
  defenseType: "pfe" as DefenseType,
  status: "draft" as DefenseSession["status"],
  maxGroupSize: 3,
  defenseDuration: 30,
  breakDuration: 15,
  submissionDeadline: "",
  juryRoleTemplateId: "",
  startDate: "",
  endDate: "",
  evaluationCoefficients: {} as Record<string, number>,
};

const formatSessionDate = (dateStr: string) => {
  try {
    return format(parseISO(dateStr), "dd MMM yyyy", { locale: fr });
  } catch {
    return dateStr;
  }
};

const statusIcons: Record<string, typeof ShieldCheck> = {
  draft: FileText,
  active: Clock,
  scheduled: Calendar,
  completed: ShieldCheck,
  archived: CheckCircle2,
};

export default function CoordinatorDefenseSessions() {
  const { data: sessionsData, isLoading } = useCoordinatorDefenseSessions();
  const sessions = sessionsData?.items ?? [];
  const templatesQuery = useJuryRoleTemplates();
  const templates = templatesQuery.data?.items ?? [];
  const createMutation = useCreateDefenseSession();
  const updateMutation = useUpdateDefenseSession();
  const deleteMutation = useDeleteDefenseSession();
  const transitionMutation = useTransitionDefenseSession();

  const form = useEntityForm(defenseSessionSchema, defaultForm);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<DefenseSession | null>(null);
  const [editing, setEditing] = useState<DefenseSession | null>(null);
  const [transitioningId, setTransitioningId] = useState<number | null>(null);

  const openCreate = () => {
    form.resetForm();
    setEditing(null);
    setDialogOpen(true);
  };

  const openEdit = (ds: DefenseSession) => {
    setEditing(ds);
    form.setFormData({
      name: ds.name,
      defenseType: ds.defenseType as DefenseType,
      status: ds.status as DefenseSession["status"],
      maxGroupSize: ds.maxGroupSize,
      defenseDuration: ds.defenseDuration,
      breakDuration: ds.breakDuration,
      submissionDeadline: ds.submissionDeadline,
      juryRoleTemplateId: String(ds.juryRoleTemplateId),
      startDate: ds.startDate,
      endDate: ds.endDate,
      evaluationCoefficients: ds.evaluationCoefficients,
    });
    setDialogOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.validateForm()) return;
    try {
      if (editing) {
        await updateMutation.mutateAsync({
          id: editing.id,
          data: {
            ...form.formData,
            defenseType: form.formData.defenseType as string,
            status: form.formData.status as string,
            juryRoleTemplateId: Number(form.formData.juryRoleTemplateId),
          },
        });
        toast.success("Session de soutenance modifiée");
      } else {
        await createMutation.mutateAsync({
          ...form.formData,
          defenseType: form.formData.defenseType as string,
          status: form.formData.status as string,
          juryRoleTemplateId: Number(form.formData.juryRoleTemplateId),
        });
        toast.success("Session de soutenance créée");
      }
      setDialogOpen(false);
      form.resetForm();
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de l'enregistrement"));
    }
  };

  const handleTransition = async (id: number, toStatus: DefenseSessionStatus) => {
    setTransitioningId(id);
    try {
      await transitionMutation.mutateAsync({ id, toStatus });
      toast.success(`Session passée en "${DEFENSE_SESSION_STATUS_LABELS[toStatus]}"`);
    } catch (error) {
      toast.error(getErrorMessage(error, "Transition impossible"));
    } finally {
      setTransitioningId(null);
    }
  };

  if (isLoading) {
    return (
      <div className="flex flex-col gap-4">
        <Skeleton className="h-8 w-64" />
        <div className="grid gap-4 md:grid-cols-2">
          {[1, 2].map((i) => (
            <Skeleton key={i} className="h-48 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6" data-testid="coord-sessions-page">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Sessions de soutenance</h1>
          <p className="text-muted-foreground">
            Gérez le cycle de vie de vos sessions : création, activation, planification, clôture et archivage.
          </p>
        </div>
        <Button onClick={openCreate} data-testid="coord-sessions-add-button">
          <Plus data-icon="inline-start" />
          Nouvelle session
        </Button>
      </div>

      {sessions.length === 0 ? (
        <EmptyState
          variant="card"
          description="Aucune session de soutenance pour le moment."
        />
       ) : (
         <div className="grid gap-6 md:grid-cols-2">
           {sessions?.map((session) => {
            const StatusIcon = statusIcons[session.status] || ShieldCheck;
            const nextStates = DEFENSE_SESSION_LIFECYCLE[session.status] ?? [];

            return (
              <Card key={session.id} className="relative overflow-hidden" data-testid={`coord-sessions-card-${session.id}`}>
                <div className="absolute right-0 top-0 h-1 w-full bg-primary" />
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex flex-col gap-1">
                      <CardTitle className="flex items-center gap-2">
                        <StatusIcon data-icon="inline-start" className="text-primary" />
                        {session.name}
                      </CardTitle>
                      <CardDescription>
                        <Badge variant="outline" className="mr-2">
                          {DEFENSE_TYPE_SHORT_LABELS[session.defenseType as DefenseType] ?? session.defenseType}
                        </Badge>
                        {session.defenseDuration} min par passage · {session.breakDuration} min de pause
                      </CardDescription>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant={DEFENSE_SESSION_STATUS_BADGE[session.status] ?? "secondary"} className="rounded-md capitalize">
                        {DEFENSE_SESSION_STATUS_LABELS[session.status]}
                      </Badge>
                      <CrudActions
                        entity={session}
                        onEdit={openEdit}
                        onDelete={setDeleteTarget}
                      />
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="flex flex-col gap-4">
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="rounded-lg bg-muted/50 p-3">
                      <p className="text-xs text-muted-foreground">Max groupe</p>
                      <p className="font-medium">{session.maxGroupSize} étudiants</p>
                    </div>
                    <div className="rounded-lg bg-muted/50 p-3">
                      <p className="text-xs text-muted-foreground">Dépôt avant</p>
                      <p className="font-medium">{formatSessionDate(session.submissionDeadline)}</p>
                    </div>
                    <div className="rounded-lg bg-muted/50 p-3">
                      <p className="text-xs text-muted-foreground">Début</p>
                      <p className="font-medium">
                        <Calendar data-icon="inline-start" className="inline" />
                        {formatSessionDate(session.startDate)}
                      </p>
                    </div>
                    <div className="rounded-lg bg-muted/50 p-3">
                      <p className="text-xs text-muted-foreground">Fin</p>
                      <p className="font-medium">
                        <Calendar data-icon="inline-start" className="inline" />
                        {formatSessionDate(session.endDate)}
                      </p>
                    </div>
                  </div>

                  {nextStates.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                      {nextStates.map((next) => (
                        <Button
                          key={next}
                          size="sm"
                          variant="outline"
                          onClick={() =>
                            handleTransition(session.id, next as DefenseSessionStatus)
                          }
                          disabled={transitioningId === session.id}
                          className="gap-1"
                        >
                          {transitioningId === session.id && <Loader2 data-icon="inline-start" className="animate-spin" />}
                          {transitioningId === session.id ? DEFENSE_SESSION_STATUS_LABELS[next] : <>{DEFENSE_SESSION_STATUS_LABELS[next]}<ArrowRight data-icon="inline-end" /></>}
                        </Button>
                      ))}
                    </div>
                  )}

                  {session.status === "archived" && (
                    <p className="text-xs text-muted-foreground">
                      Session archivée — toutes les données sont en lecture seule.
                    </p>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-lg" data-testid="coord-sessions-dialog">
          <DialogHeader>
            <DialogTitle>{editing ? "Modifier" : "Créer"} une session de soutenance</DialogTitle>
            <DialogDescription>
              Configurez les règles et paramètres de la session.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit}>
            <FieldGroup className="flex flex-col gap-4 py-4">
               <Field>
                 <FieldLabel>Nom</FieldLabel>
                 <Input
                   value={form.formData.name}
                   onChange={(e) => form.setFormData({ ...form.formData, name: e.target.value })}
                   required
                   error={form.fieldErrors?.name}
                   data-testid="coord-sessions-input-name"
                 />
               </Field>
               <Field>
                 <FieldLabel>Type de soutenance</FieldLabel>
                 <Select
                   value={form.formData.defenseType}
                   onValueChange={(v) => {
                     const type = v as DefenseType;
                     form.setFormData({
                       ...form.formData,
                       defenseType: type,
                       defenseDuration: DEFAULT_DURATION_BY_TYPE[type],
                     });
                   }}
                 >
                   <SelectTrigger data-testid="coord-sessions-input-defense-type">
                     <SelectValue />
                   </SelectTrigger>
                    <SelectContent>
                      <SelectGroup>
                        {DEFENSE_TYPE_OPTIONS.map((opt) => (
                          <SelectItem key={opt.value} value={opt.value}>
                            {opt.label}
                          </SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                 </Select>
               </Field>
               <div className="grid grid-cols-2 gap-4">
                 <Field>
                   <FieldLabel>Durée de passage (min)</FieldLabel>
                   <Input
                     type="number"
                     value={form.formData.defenseDuration}
                     onChange={(e) => form.setFormData({ ...form.formData, defenseDuration: Number(e.target.value) })}
                     min={5}
                     max={180}
                     error={form.fieldErrors?.defenseDuration}
                     data-testid="coord-sessions-input-duration"
                   />
                 </Field>
                 <Field>
                   <FieldLabel>Pause (min)</FieldLabel>
                   <Input
                     type="number"
                     value={form.formData.breakDuration}
                     onChange={(e) => form.setFormData({ ...form.formData, breakDuration: Number(e.target.value) })}
                     min={0}
                     error={form.fieldErrors?.breakDuration}
                     data-testid="coord-sessions-input-break"
                   />
                 </Field>
               </div>
               <div className="grid grid-cols-2 gap-4">
                 <Field>
                   <FieldLabel>Max étudiants/groupe</FieldLabel>
                   <Input
                     type="number"
                     value={form.formData.maxGroupSize}
                     onChange={(e) => form.setFormData({ ...form.formData, maxGroupSize: Number(e.target.value) })}
                     min={1}
                     max={10}
                     error={form.fieldErrors?.maxGroupSize}
                     data-testid="coord-sessions-input-max-group"
                   />
                 </Field>
                 <Field>
                   <FieldLabel>Date limite dépôt</FieldLabel>
                   <Input
                     type="date"
                     value={form.formData.submissionDeadline}
                     onChange={(e) => form.setFormData({ ...form.formData, submissionDeadline: e.target.value })}
                     required
                     error={form.fieldErrors?.submissionDeadline}
                     data-testid="coord-sessions-input-deadline"
                   />
                 </Field>
               </div>
               <Field>
                 <FieldLabel>Modèle de jury</FieldLabel>
                  <Select
                    value={form.formData.juryRoleTemplateId}
                    onValueChange={(v) => {
                       const tpl = templates.find((t: JuryRoleTemplate) => String(t.id) === v);
                      const coeffs: Record<string, number> = {};
                      if (tpl) {
                        for (const role of tpl.roles) {
                          coeffs[role.name] = role.coefficient;
                        }
                      }
                      form.setFormData({ ...form.formData, juryRoleTemplateId: v, evaluationCoefficients: coeffs });
                    }}
                  >
                    <SelectTrigger data-testid="coord-sessions-input-template"><SelectValue placeholder="Choisir un modèle" /></SelectTrigger>
                    <SelectContent>
                      <SelectGroup>
                        {templates.map((t: JuryRoleTemplate) => (
                          <SelectItem key={t.id} value={String(t.id)}>{t.name}</SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                 {form.fieldErrors?.juryRoleTemplateId && (
                   <p className="text-sm font-medium text-destructive">{form.fieldErrors.juryRoleTemplateId}</p>
                 )}
               </Field>
               <Field>
                 <FieldLabel>Coefficients d'évaluation</FieldLabel>
                 <div className="rounded-lg border bg-muted/50 p-3 text-sm">
                   {Object.keys(form.formData.evaluationCoefficients).length > 0 ? (
                     <div className="flex flex-wrap gap-2">
                       {Object.entries(form.formData.evaluationCoefficients).map(([role, coeff]) => (
                         <span key={role} className="inline-flex items-center gap-1 rounded-md bg-background px-2 py-1 text-xs font-medium">
                           {role}: {coeff}%
                         </span>
                       ))}
                     </div>
                   ) : (
                     <span className="text-muted-foreground">Sélectionnez un modèle de jury</span>
                   )}
                 </div>
               </Field>
               <div className="grid grid-cols-2 gap-4">
                 <Field>
                   <FieldLabel>Date début</FieldLabel>
                   <Input
                     type="date"
                     value={form.formData.startDate}
                     onChange={(e) => form.setFormData({ ...form.formData, startDate: e.target.value })}
                     required
                     error={form.fieldErrors?.startDate}
                     data-testid="coord-sessions-input-start"
                   />
                 </Field>
                 <Field>
                   <FieldLabel>Date fin</FieldLabel>
                   <Input
                     type="date"
                     value={form.formData.endDate}
                     onChange={(e) => form.setFormData({ ...form.formData, endDate: e.target.value })}
                     required
                     error={form.fieldErrors?.endDate}
                     data-testid="coord-sessions-input-end"
                   />
                 </Field>
               </div>
            </FieldGroup>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)} data-testid="coord-sessions-dialog-cancel">
                Annuler
              </Button>
              <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending} data-testid="coord-sessions-dialog-submit">
                {(createMutation.isPending || updateMutation.isPending) && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {(createMutation.isPending || updateMutation.isPending) ? "Enregistrer" : "Enregistrer"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <DeleteAlert
        isOpen={deleteTarget !== null}
        onOpenChange={(o) => { if (!o) setDeleteTarget(null); }}
        entityName={deleteTarget?.name}
        onDelete={async () => {
          if (!deleteTarget) return;
          try {
            await deleteMutation.mutateAsync(deleteTarget.id);
            toast.success("Session de soutenance supprimée");
            setDeleteTarget(null);
          } catch (error) {
             toast.error(getErrorMessage(error, "Erreur lors de la suppression"));
          }
        }}
        isPending={deleteMutation.isPending}
      />
    </div>
  );
}
