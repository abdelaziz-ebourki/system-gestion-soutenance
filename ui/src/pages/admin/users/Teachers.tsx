import { type ColumnDef, type PaginationState } from "@tanstack/react-table";
import { Link } from "react-router-dom";
import { Plus, Users, Loader2 } from "lucide-react";

import { useTeachers, useDepartments } from "@/hooks/queries";
import type { Teacher, Department } from "@/types";
import { DataTable } from "@/components/ui/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { EmptyState } from "@/components/ui/empty-state";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { BulkImportDialog } from "@/components/admin/BulkImportDialog";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { useTeacherCrud } from "@/hooks/entities/use-teacher-crud";
import { CrudActions } from "@/components/admin/CrudActions";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { useMemo, useState } from "react";
import { DEFAULT_API_LIMIT, MAX_TEACHER_FETCH_LIMIT } from "@/lib/constants";

export default function Teachers() {
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: 0,
    pageSize: DEFAULT_API_LIMIT,
  });
  const [isFiltering, setIsFiltering] = useState(false);
  const [selectedTeachers, setSelectedTeachers] = useState<Teacher[]>([]);

  const { data: teachersData, isLoading, refetch } = useTeachers({
    page: isFiltering ? 0 : pagination.pageIndex,
    limit: isFiltering ? MAX_TEACHER_FETCH_LIMIT : pagination.pageSize,
  });
  const { data: departmentsData } = useDepartments();
  const departments = useMemo(() => departmentsData?.items ?? [], [departmentsData]);
  const crud = useTeacherCrud();

  const data = teachersData?.items ?? [];
  const pageCount = teachersData?.pageCount ?? 0;

  const columns = useMemo<ColumnDef<Teacher>[]>(() => [
    { accessorKey: "lastName", header: "Nom", cell: ({ row }) => <div className="font-medium">{row.original.lastName}</div> },
    { accessorKey: "firstName", header: "Prénom", cell: ({ row }) => <div className="font-medium">{row.original.firstName}</div> },
    { accessorKey: "email", header: "Email" },
    {
      accessorKey: "departmentId",
      header: "Département",
      filterFn: "equalsString",
       cell: ({ row }) => {
         const id = row.getValue("departmentId") as number;
         return departments.find((d: Department) => d.id === id)?.name || id;
       },
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
  ], [crud, departments]);

  if (departments.length === 0) {
    return (
      <div className="flex flex-col gap-6">
        <EmptyState
          icon={Users}
          title="Configuration requise"
          description="Vous devez d'abord configurer les départements avant de pouvoir gérer les enseignants."
          action={<Button asChild><Link to="/admin/departments">Départements</Link></Button>}
        />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 pb-20" data-testid="admin-teachers-page">
      <div className="flex items-center justify-between">
        <div className="relative pb-4">
          <h1 className="text-4xl font-bold tracking-tight">Enseignants</h1>
          <div className="absolute bottom-0 left-0 h-1 w-20 bg-primary rounded-full" />
          <p className="text-muted-foreground mt-2">Gestion du corps enseignant.</p>
        </div>
        <div className="flex gap-2">
          <BulkImportDialog entity="teacher" triggerButtonText="Importation en masse" onSuccess={refetch} />
          <Button onClick={crud.openCreate} data-testid="admin-teachers-add-button">
            <Plus data-icon="inline-start" /> Nouvel Enseignant
          </Button>
        </div>
      </div>

        <DataTable columns={columns} data={data} loading={isLoading} getRowId={(row) => row.id} enableRowSelection onSelectedRowsChange={setSelectedTeachers}
          manualPagination={!isFiltering} pageCount={!isFiltering ? pageCount : undefined}
          pagination={!isFiltering ? pagination : undefined} onPaginationChange={!isFiltering ? setPagination : undefined}
          onFiltering={setIsFiltering}
          filterColumns={["lastName", "firstName", "email"]} filterPlaceholder="Rechercher par nom, prénom ou email..."
          filters={[
            { column: "departmentId", label: "Département", options: departments.map((d: Department) => ({ value: String(d.id), label: d.name })) },
          ]} />

      <BatchActionsBar
        selectedCount={selectedTeachers.length}
        entityLabel="enseignant(s)"
        actions={[{ key: "department", label: "Modifier le département" }, { key: "delete", label: "Supprimer" }]}
        fieldOptionsMap={{
          department: departments.map((d: Department) => ({ value: String(d.id), label: d.name })),
        }}
        onUpdateField={async (_field, value) => {
          await Promise.all(selectedTeachers.map((t) => crud.updateMutation(t.id, { lastName: t.lastName, firstName: t.firstName, email: t.email, departmentId: Number(value), role: "TEACHER" })));
        }}
        onDeleteSelected={async () => {
          await Promise.all(selectedTeachers.map((t) => crud.deleteMutation(t.id)));
        }}
        isPending={crud.isDeletePending}
        onClearSelection={() => setSelectedTeachers([])}
      />

      <Dialog open={crud.isDialogOpen} onOpenChange={crud.setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{crud.selected ? "Modifier" : "Ajouter"} Enseignant</DialogTitle>
            <DialogDescription>Remplissez les informations de l'enseignant.</DialogDescription>
          </DialogHeader>
          <form onSubmit={crud.handleSubmit}>
            <FieldGroup className="py-4 flex flex-col gap-4">
              <div className="grid grid-cols-2 gap-4">
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
              </div>
              <Field>
                <FieldLabel>Email</FieldLabel>
                <Input type="email" value={crud.formData.email}
                  onChange={(e) => crud.setFormData({ ...crud.formData, email: e.target.value })}
                  required error={crud.fieldErrors?.email} />
              </Field>
              <Field>
                <FieldLabel>Département</FieldLabel>
                <Select value={crud.formData.departmentId ? String(crud.formData.departmentId) : ""}
                  onValueChange={(v) => crud.setFormData({ ...crud.formData, departmentId: v || "" })}>
                  <SelectTrigger><SelectValue placeholder="Choisir un département" /></SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {departments.map((d: Department) => <SelectItem key={d.id} value={String(d.id)}>{d.name}</SelectItem>)}
                    </SelectGroup>
                  </SelectContent>
                </Select>
                {crud.fieldErrors?.departmentId && <p className="text-sm font-medium text-destructive">{crud.fieldErrors.departmentId}</p>}
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

