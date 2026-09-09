import { Loader2 } from "lucide-react";
import { useState, useMemo, useEffect } from "react";
import { useProjects, useGroups, useAssignProjectToGroup } from "@/hooks/queries";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { EmptyState } from "@/components/ui/empty-state";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import type { Group } from "@/types";

interface AssignProjectDialogProps {
  group: Group | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function AssignProjectDialog({
  group,
  open,
  onOpenChange,
}: AssignProjectDialogProps) {
  const projectsQuery = useProjects();
  const groupsQuery = useGroups();
  const assignMutation = useAssignProjectToGroup();
  const projects = useMemo(() => projectsQuery.data?.items ?? [], [projectsQuery.data]);
  const groups = useMemo(() => groupsQuery.data?.items ?? [], [groupsQuery.data]);
  const [selectedProjectId, setSelectedProjectId] = useState("");

  useEffect(() => {
    if (!open) {
      setSelectedProjectId("");
    }
  }, [open]);

  const assignedProjectIds = useMemo(
    () => new Set(
      groups.filter((g) => g.projectId).map((g) => g.projectId),
    ),
    [groups],
  );

  const availableProjects = useMemo(
    () => projects.filter((p) => !assignedProjectIds.has(p.id)),
    [projects, assignedProjectIds],
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProjectId || !group) return;
    try {
      await assignMutation.mutateAsync({ projectId: Number(selectedProjectId), groupId: group.id });
      toast.success(`Projet assigné au groupe "${group.groupName}"`);
      setSelectedProjectId("");
      onOpenChange(false);
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de l'assignation"));
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md" data-testid="coord-assign-project-dialog">
        <DialogHeader>
          <DialogTitle>Assigner un projet</DialogTitle>
          <DialogDescription>
            Groupe : <strong>{group?.groupName}</strong>
            {group?.studentNames && (
              <span className="block text-xs text-muted-foreground mt-1">
                {group.studentNames.join(", ")}
              </span>
            )}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <FieldGroup>
            <Field>
              <FieldLabel>Projet</FieldLabel>
              {projectsQuery.isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <div className="size-6 animate-spin rounded-full border-2 border-primary border-t-transparent" />
                </div>
              ) : availableProjects.length === 0 ? (
                <EmptyState description="Aucun projet disponible. Créez d'abord un projet." />
              ) : (
                <Select value={selectedProjectId} onValueChange={(v) => setSelectedProjectId(v ?? "")} data-testid="coord-assign-project-select">
                  <SelectTrigger>
                    <SelectValue placeholder="Choisir un projet" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {availableProjects.map((p) => (
                        <SelectItem key={p.id} value={String(p.id)}>
                          {p.title}
                        </SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
              )}
            </Field>
          </FieldGroup>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} data-testid="coord-assign-project-cancel">
              Annuler
            </Button>
            <Button type="submit" disabled={!selectedProjectId || assignMutation.isPending} data-testid="coord-assign-project-submit">
              {assignMutation.isPending ? <><Loader2 data-icon="inline-start" className="animate-spin" /> Assignation...</> : "Assigner"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

