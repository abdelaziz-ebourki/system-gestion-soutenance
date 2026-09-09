import { useState, useMemo } from "react";
import {
  Plus,
  Users,
  FolderKanban,
  UserPlus,
  CheckCircle2,
} from "lucide-react";
import type { ColumnDef } from "@tanstack/react-table";

import { useProjects, useUpdateProject, useDeleteProject, useGroups } from "@/hooks/queries";
import type { Project, Group } from "@/types";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { StatsCard } from "@/components/ui/stats-card";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { CrudActions } from "@/components/admin/CrudActions";
import { DataTable } from "@/components/ui/data-table";
import { ProjectDialog } from "@/components/coordinator/ProjectDialog";
import { AssignProjectDialog } from "@/components/coordinator/AssignProjectDialog";
import { BulkImportDialog } from "@/components/admin/BulkImportDialog";

export default function CoordinatorProjects() {
  const projectsQuery = useProjects();
  const updateProjectMutation = useUpdateProject();
  const deleteProjectMutation = useDeleteProject();
  const projects = useMemo(() => projectsQuery.data?.items ?? [], [projectsQuery.data]);
  const isLoading = projectsQuery.isLoading;
  const [selectedProjects, setSelectedProjects] = useState<Project[]>([]);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Project | null>(null);

  const columns = useMemo<ColumnDef<Project>[]>(() => [
    {
      accessorKey: "title",
      header: "Projet",
      cell: ({ row }) => (
        <div className="flex flex-col gap-1">
          <div className="font-medium">{row.original.title}</div>
          <div className="text-xs text-muted-foreground">
            {row.original.description || "Aucune description"}
          </div>
        </div>
      ),
    },
    {
      accessorKey: "studentNames",
      header: "Groupe",
      cell: ({ row }) => (
        <div className="max-w-65 text-sm">
          {row.original.studentNames?.join(", ") || "Étudiants non renseignés"}
        </div>
      ),
    },
    {
      accessorKey: "supervisorName",
      header: "Encadrant",
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="text-right">
          <CrudActions entity={row.original} onEdit={(p) => setEditingProject(p)} onDelete={setDeleteTarget} />
        </div>
      ),
    },
  ], []);

  const groupsQuery = useGroups();
  const studentGroups = groupsQuery.data?.items ?? [];
  const [assignTarget, setAssignTarget] = useState<Group | null>(null);

  const multiMemberGroups = useMemo(() => projects.filter(
    (project) => (project.studentNames?.length || 0) > 1,
  ), [projects]);

  return (
    <div className="flex flex-col gap-6 pb-20" data-testid="coord-projects-page">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Projets & Groupes
          </h1>
          <p className="text-muted-foreground">
            Pilotez les sujets, les groupes d'étudiants et la maturité des
            dossiers avant composition des jurys.
          </p>
        </div>
        <Button onClick={() => setIsCreateOpen(true)} data-testid="coord-projects-add-button">
          <Plus data-icon="inline-start" />
          Ajouter un projet
        </Button>
        <BulkImportDialog entity="project" onSuccess={() => projectsQuery.refetch()} />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <StatsCard label="Portefeuille" value={projects.length} icon={FolderKanban} />
        <StatsCard label="Groupes complets" value={multiMemberGroups.length} icon={Users} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Catalogue des projets</CardTitle>
          <CardDescription>
            Recherchez, mettez à jour ou supprimez un dossier depuis la même
            table.
          </CardDescription>
        </CardHeader>
        <CardContent>
            <DataTable
              columns={columns}
              data={projects}
              loading={isLoading}
              getRowId={(row) => String(row.id)}
              enableRowSelection
              onSelectedRowsChange={setSelectedProjects}
              filterColumns="title"
              filterPlaceholder="Rechercher un projet..."
              filters={[]}
            />
        </CardContent>
      </Card>

      <Card data-testid="coord-projects-groups-section">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users data-icon="inline-start" /> Groupes étudiants
          </CardTitle>
          <CardDescription>
            Groupes formés par les étudiants. Assignez-leur un projet pour activer la suite (jury, planning).
          </CardDescription>
        </CardHeader>
        <CardContent>
          {groupsQuery.isLoading ? (
            <div className="flex flex-col gap-3">
              {[1, 2].map((i) => (
                <Skeleton key={i} className="h-16 w-full rounded-lg" />
              ))}
            </div>
          ) : studentGroups.length === 0 ? (
            <EmptyState variant="dashed" description="Aucun groupe étudiant pour le moment." />
          ) : (
            <div className="flex flex-col gap-3">
              {studentGroups.map((g: Group) => {
                const hasProject = !!g.projectId;
                const assignedProject = hasProject ? projects.find((p) => p.id === g.projectId) : null;
                return (
                  <div key={g.id} className="flex items-center justify-between rounded-lg border p-4">
                    <div className="flex flex-col gap-1">
                      <p className="font-medium">{g.groupName}</p>
                      <p className="text-xs text-muted-foreground">
                        {g.studentNames.join(", ")}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      {hasProject ? (
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                          <CheckCircle2 className="text-primary" />
                          {assignedProject?.title ?? `Projet #${g.projectId}`}
                        </div>
                      ) : (
                        <Button size="sm" variant="outline" onClick={() => setAssignTarget(g)} data-testid={`coord-projects-assign-${g.id}`}>
                          <UserPlus data-icon="inline-start" />
                          Assigner un projet
                        </Button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      <BatchActionsBar
        selectedCount={selectedProjects.length}
        entityLabel="projet(s)"
        actions={[
          { key: "delete", label: "Supprimer" },
        ]}
        fieldOptionsMap={{}}
        onUpdateField={async () => {}}
        onDeleteSelected={async () => {
          await Promise.all(selectedProjects.map((p) => deleteProjectMutation.mutateAsync(p.id)));
        }}
        isPending={updateProjectMutation.isPending || deleteProjectMutation.isPending}
        onClearSelection={() => setSelectedProjects([])}
      />

      <DeleteAlert
        isOpen={deleteTarget !== null}
        onOpenChange={(o) => { if (!o) setDeleteTarget(null); }}
        entityName={deleteTarget?.title}
        onDelete={async () => {
          if (!deleteTarget) return;
          try {
            await deleteProjectMutation.mutateAsync(deleteTarget.id);
            toast.success("Projet supprimé");
            setDeleteTarget(null);
          } catch (error) {
             toast.error(getErrorMessage(error, "Erreur lors de la suppression"));
          }
        }}
        isPending={deleteProjectMutation.isPending}
      />

      <ProjectDialog
        open={isCreateOpen || Boolean(editingProject)}
        onOpenChange={(open) => {
          if (!open) {
            setIsCreateOpen(false);
            setEditingProject(null);
          }
        }}
        project={editingProject}
        onSuccess={() => {}}
      />

      <AssignProjectDialog
        group={assignTarget}
        open={assignTarget !== null}
        onOpenChange={(open) => { if (!open) setAssignTarget(null); }}
      />
    </div>
  );
}

