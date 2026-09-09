import { type ColumnDef, type PaginationState } from "@tanstack/react-table";
import { Plus, Loader2 } from "lucide-react";

import { useCoordinators } from "@/hooks/queries";
import type { Coordinator } from "@/types";
import { DataTable } from "@/components/ui/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { BulkImportDialog } from "@/components/admin/BulkImportDialog";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { useCoordinatorCrud } from "@/hooks/entities/use-coordinator-crud";
import { CrudActions } from "@/components/admin/CrudActions";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { useMemo, useState } from "react";
import { DEFAULT_API_LIMIT, MAX_TEACHER_FETCH_LIMIT } from "@/lib/constants";

export default function Coordinators() {
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: 0,
    pageSize: DEFAULT_API_LIMIT,
  });
  const [isFiltering, setIsFiltering] = useState(false);
  const [selectedCoordinators, setSelectedCoordinators] = useState<Coordinator[]>([]);

  const { data: coordinatorsData, isLoading, refetch } = useCoordinators({
    page: isFiltering ? 0 : pagination.pageIndex,
    limit: isFiltering ? MAX_TEACHER_FETCH_LIMIT : pagination.pageSize,
  });
  const crud = useCoordinatorCrud();

  const data = coordinatorsData?.items ?? [];
  const pageCount = coordinatorsData?.pageCount ?? 0;

  const columns = useMemo<ColumnDef<Coordinator>[]>(() => [
    {
      accessorKey: "lastName",
      header: "Nom",
    },
    {
      accessorKey: "firstName",
      header: "Prénom",
    },
    {
      accessorKey: "email",
      header: "Email",
    },
    {
      accessorKey: "isActive",
      header: "Statut",
      cell: ({ row }) => (
        <Badge variant={row.getValue("isActive") ? "default" : "secondary"}>
          {row.getValue("isActive") ? "Actif" : "Inactif"}
        </Badge>
      ),
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="text-right">
          <CrudActions entity={row.original} onEdit={crud.openEdit} onDelete={crud.openDelete} />
        </div>
      ),
    },
  ], [crud]);

  return (
    <div className="flex flex-col gap-6 pb-20" data-testid="admin-coordinators-page">
      <div className="flex items-center justify-between">
        <div className="relative pb-4">
          <h1 className="text-4xl font-bold tracking-tight">Coordinateurs</h1>
          <div className="absolute bottom-0 left-0 h-1 w-20 bg-primary rounded-full" />
          <p className="text-muted-foreground mt-2">Gestion des coordinateurs de filières.</p>
        </div>
        <div className="flex gap-2">
          <BulkImportDialog entity="coordinator" triggerButtonText="Importation en masse" onSuccess={refetch} />
          <Button onClick={crud.openCreate} data-testid="admin-coordinators-add-button">
            <Plus data-icon="inline-start" /> Nouveau Coordinateur
          </Button>
        </div>
      </div>

        <DataTable columns={columns} data={data} loading={isLoading} getRowId={(row) => row.id} enableRowSelection onSelectedRowsChange={setSelectedCoordinators}
          manualPagination={!isFiltering} pageCount={!isFiltering ? pageCount : undefined}
          pagination={!isFiltering ? pagination : undefined} onPaginationChange={!isFiltering ? setPagination : undefined}
          onFiltering={setIsFiltering}
          filterColumns={["lastName", "firstName", "email"]} filterPlaceholder="Rechercher par nom, prénom ou email..." />

      <BatchActionsBar
        selectedCount={selectedCoordinators.length}
        entityLabel="coordinateur(s)"
        actions={[{ key: "delete", label: "Supprimer" }]}
        onDeleteSelected={async () => {
          await Promise.all(selectedCoordinators.map((c) => crud.deleteMutation(c.id)));
          setSelectedCoordinators([]);
        }}
        isPending={crud.isDeletePending}
        onClearSelection={() => setSelectedCoordinators([])}
      />

      <Dialog open={crud.isDialogOpen} onOpenChange={crud.setIsDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{crud.selected ? "Modifier" : "Ajouter"} Coordinateur</DialogTitle>
            <DialogDescription>Compte administratif de gestion de major.</DialogDescription>
          </DialogHeader>
          <form onSubmit={crud.handleSubmit}>
            <FieldGroup className="grid grid-cols-2 gap-4 py-4">
              <Field>
                <FieldLabel>Nom</FieldLabel>
                <Input value={crud.formData.lastName}
                  onChange={(e) => crud.setFormData({ ...crud.formData, lastName: e.target.value })}
                  required error={crud.fieldErrors?.lastName} />
              </Field>
              <Field>
                <FieldLabel>Prénom</FieldLabel>
                <Input value={crud.formData.firstName}
                  onChange={(e) => crud.setFormData({ ...crud.formData, firstName: e.target.value })}
                  required error={crud.fieldErrors?.firstName} />
              </Field>
              <Field className="col-span-2">
                <FieldLabel>Email</FieldLabel>
                <Input type="email" value={crud.formData.email}
                  onChange={(e) => crud.setFormData({ ...crud.formData, email: e.target.value })}
                  required error={crud.fieldErrors?.email} />
              </Field>
            </FieldGroup>
            <DialogFooter>
              <Button type="submit" disabled={crud.isCreatePending || crud.isUpdatePending}>
                {(crud.isCreatePending || crud.isUpdatePending) && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {(crud.isCreatePending || crud.isUpdatePending) ? "Enregistrement..." : "Enregistrer"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <DeleteAlert isOpen={crud.isDeleteDialogOpen} onOpenChange={crud.setIsDeleteDialogOpen}
        onDelete={crud.handleDelete} entityName={crud.selected ? `${crud.selected.lastName} ${crud.selected.firstName}` : undefined} isPending={crud.isDeletePending} />
    </div>
  );
}

