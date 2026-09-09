import { useState } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { AlertCircle, ShieldCheck, UserPlus, Users, AlertTriangle } from "lucide-react";
import { toast } from "sonner";

import { useJuries, useProjects, useDeleteJury } from "@/hooks/queries";
import type { Jury } from "@/types";
import { getErrorMessage } from "@/lib/utils";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { DataTable } from "@/components/ui/data-table";
import { Skeleton } from "@/components/ui/skeleton";
import { CreateJuryDialog } from "@/components/coordinator/CreateJuryDialog";
import { DeleteAlert } from "@/components/admin/DeleteAlert";

export default function Jurys() {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [juryToDelete, setJuryToDelete] = useState<Jury | null>(null);

  const { data: juriesData, isLoading, isError, error } = useJuries();
  const juries = juriesData?.items ?? [];
  const { data: projectsData } = useProjects();
  const projects = projectsData?.items ?? [];
  const deleteJury = useDeleteJury();

  const columns: ColumnDef<Jury>[] = [
    {
      accessorKey: "projectTitle",
      header: "Projet",
      cell: ({ row }) => (
        <div className="max-w-[300px] truncate font-medium" title={row.original.projectTitle}>
          {row.original.projectTitle}
        </div>
      ),
    },
    {
      accessorKey: "members",
      header: "Jury",
      cell: ({ row }) => (
        <div className="flex flex-col gap-1">
          {row.original.members.map((m) => (
            <div key={m.teacherId} className="text-xs">
              <span className="font-bold text-muted-foreground mr-1 uppercase">{m.roleName}:</span>
              {m.teacherName}
            </div>
          ))}
        </div>
      ),
    },
    {
      id: "actions",
      cell: ({ row }) => (
        <Button
          variant="ghost"
          size="sm"
          className="text-destructive hover:text-destructive hover:bg-destructive/10"
          onClick={() => setJuryToDelete(row.original)}
        >
          Supprimer
        </Button>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6" data-testid="coord-juries-page">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Gestion des Jurys</h1>
          <p className="text-muted-foreground">
            Configurez les commissions d'examen pour chaque soutenance.
          </p>
        </div>
        <Button onClick={() => setIsCreateOpen(true)} className="gap-2" data-testid="coord-juries-add-button">
          <UserPlus data-icon="inline-start" /> Nouveau Jury
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Jurys</CardTitle>
            <Users className="text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {isLoading ? <Skeleton className="h-7 w-8" /> : juries.length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Projets sans Jury</CardTitle>
            <AlertTriangle className="text-warning" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {isLoading ? <Skeleton className="h-7 w-8" /> : projects.filter((p) => !juries.find((j) => j.projectId === p.id)).length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Vérification</CardTitle>
            <ShieldCheck className="text-success" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-success">OK</div>
          </CardContent>
        </Card>
      </div>

      {isError && (
        <Alert variant="destructive" data-testid="coord-juries-error">
          <AlertCircle />
          <AlertDescription>
            {getErrorMessage(error, "Impossible de charger les jurys.")}
          </AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Liste des Commissions</CardTitle>
        </CardHeader>
        <CardContent>
          <DataTable
            columns={columns}
            data={juries}
            loading={isLoading}
            filterColumns="projectTitle"
            filterPlaceholder="Filtrer par projet..."
          />
        </CardContent>
      </Card>

      <CreateJuryDialog
        open={isCreateOpen}
        onOpenChange={setIsCreateOpen}
        onSuccess={() => setIsCreateOpen(false)}
      />

      <DeleteAlert
        isOpen={!!juryToDelete}
        onOpenChange={(open) => !open && setJuryToDelete(null)}
        entityName={`le jury pour "${juryToDelete?.projectTitle}"`}
        onDelete={async () => {
          if (!juryToDelete) return;
          try {
            await deleteJury.mutateAsync(juryToDelete.id);
            toast.success("Jury supprimé avec succès");
            setJuryToDelete(null);
          } catch (error) {
             toast.error(getErrorMessage(error, "Erreur lors de la suppression du jury"));
          }
        }}
        isPending={deleteJury.isPending}
      />
    </div>
  );
}

