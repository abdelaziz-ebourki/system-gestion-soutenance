import { useMemo, useState } from "react";
import { type ColumnDef } from "@tanstack/react-table";
import { Plus, Loader2 } from "lucide-react";

import { useDepartments, useTeachersList } from "@/hooks/queries";
import { type Department } from "@/types";
import { DataTable } from "@/components/ui/data-table";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { useDepartmentCrud } from "@/hooks/entities/use-department-crud";
import { CrudActions } from "@/components/admin/CrudActions";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";

export default function Departments() {
  const { data: departmentsData, isLoading } = useDepartments();
  const [selectedDepartments, setSelectedDepartments] = useState<Department[]>([]);
  const { data: teachersData } = useTeachersList();
  const teachers = useMemo(() => teachersData?.items ?? [], [teachersData]);
  const crud = useDepartmentCrud();

  const data = departmentsData?.items ?? [];

  const columns = useMemo<ColumnDef<Department>[]>(() => [
    {
      accessorKey: "code",
      header: "Code",
      cell: ({ row }) => (
        <div className="font-mono font-bold">{row.getValue("code")}</div>
      ),
    },
    {
      accessorKey: "name",
      header: "Nom du Département",
    },
    {
      accessorKey: "headId",
      header: "Chef de Département",
      cell: ({ row }) => {
        const id = row.getValue("headId") as number | undefined;
        if (!id) return <span className="text-muted-foreground italic">Non assigné</span>;
        const teacher = teachers.find((t) => t.id === id);
        return teacher ? `${teacher.lastName} ${teacher.firstName}` : id;
      },
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
  ], [crud, teachers]);

  return (
    <div className="flex flex-col gap-6 pb-20" data-testid="admin-departments-page">
      <div className="flex items-center justify-between">
        <div className="relative pb-4">
          <h1 className="text-4xl font-bold tracking-tight">Départements</h1>
          <div className="absolute bottom-0 left-0 h-1 w-20 bg-primary rounded-full" />
          <p className="text-muted-foreground mt-2">Structure académique.</p>
        </div>
        <Button onClick={crud.openCreate} data-testid="admin-departments-add-button">
          <Plus data-icon="inline-start" /> Nouveau Département
        </Button>
      </div>

         <DataTable
           columns={columns}
           data={data}
           loading={isLoading}
           getRowId={(row) => row.id}
           enableRowSelection
           onSelectedRowsChange={setSelectedDepartments}
           filterColumns="name"
           filterPlaceholder="Rechercher par nom..."
         />

      <BatchActionsBar
        selectedCount={selectedDepartments.length}
        entityLabel="département(s)"
        actions={[{ key: "delete", label: "Supprimer" }]}
        onDeleteSelected={async () => {
          await Promise.all(selectedDepartments.map((d) => crud.deleteMutation(d.id)));
        }}
        isPending={crud.isDeletePending}
        onClearSelection={() => setSelectedDepartments([])}
      />
      
      <Dialog open={crud.isDialogOpen} onOpenChange={crud.setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {crud.selected ? "Modifier" : "Ajouter"} Département
            </DialogTitle>
            <DialogDescription>
              Détails de l'unité académique.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={crud.handleSubmit}>
            <FieldGroup className="py-4">
              <Field>
                <FieldLabel>Nom du Département</FieldLabel>
                <Input
                  placeholder="ex: Informatique"
                  value={crud.formData.name}
                  onChange={(e) =>
                    crud.setFormData({ ...crud.formData, name: e.target.value })
                  }
                  required
                  error={crud.fieldErrors?.name}
                />
              </Field>
              <Field>
                <FieldLabel>Code</FieldLabel>
                <Input
                  placeholder="ex: INFO"
                  value={crud.formData.code}
                  onChange={(e) =>
                    crud.setFormData({ ...crud.formData, code: e.target.value })
                  }
                  required
                  error={crud.fieldErrors?.code}
                />
              </Field>
              <Field>
                <FieldLabel>Chef de Département</FieldLabel>
                <Select
                  value={crud.formData.headId ? String(crud.formData.headId) : "none"}
                  onValueChange={(v) =>
                    crud.setFormData({ ...crud.formData, headId: v === "none" ? "" : v })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Choisir un enseignant" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectItem value="none">Aucun</SelectItem>
                      {teachers.map((t) => (
                        <SelectItem key={t.id} value={String(t.id)}>
                          {t.lastName} {t.firstName}
                        </SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </Field>
            </FieldGroup>
            <DialogFooter>
              <Button
                type="submit"
                disabled={crud.isCreatePending || crud.isUpdatePending}
              >
                {(crud.isCreatePending || crud.isUpdatePending) && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {(crud.isCreatePending || crud.isUpdatePending) ? "Enregistrement..." : "Enregistrer"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <DeleteAlert
        isOpen={crud.isDeleteDialogOpen}
        onOpenChange={crud.setIsDeleteDialogOpen}
        onDelete={crud.handleDelete}
        entityName={crud.selected?.name}
        isPending={crud.isDeletePending}
      />
    </div>
  );
}

