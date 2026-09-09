import { useMemo, useState } from "react";
import { type ColumnDef } from "@tanstack/react-table";
import { Plus, BuildingIcon, Loader2 } from "lucide-react";

import { Link } from "react-router-dom";

import { useRooms, useDepartments } from "@/hooks/queries";
import type { Room } from "@/types";
import { DataTable } from "@/components/ui/data-table";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { EmptyState } from "@/components/ui/empty-state";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { BulkImportDialog } from "@/components/admin/BulkImportDialog";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { useRoomCrud } from "@/hooks/entities/use-room-crud";
import { CrudActions } from "@/components/admin/CrudActions";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";

export default function Rooms() {
  const { data: roomsData, isLoading, refetch } = useRooms();
  const [selectedRooms, setSelectedRooms] = useState<Room[]>([]);
  const { data: departmentsData } = useDepartments();
  const departments = useMemo(() => departmentsData?.items ?? [], [departmentsData]);
  const crud = useRoomCrud();

  const data = roomsData?.items ?? [];

  const columns = useMemo<ColumnDef<Room>[]>(() => {
    const getDepartmentName = (id: number) =>
      departments.find((d) => d.id === id)?.name || id;

    return [
      {
        accessorKey: "name",
        header: "Nom de la Salle",
        cell: ({ row }) => <div className="font-medium">{row.getValue("name")}</div>,
      },
      {
        accessorKey: "departmentId",
        header: "Département",
        cell: ({ row }) => (
          <div className="flex items-center text-muted-foreground">
            <BuildingIcon data-icon="inline-start" />
            {getDepartmentName(row.getValue("departmentId"))}
          </div>
        ),
      },
      {
        accessorKey: "capacity",
        header: "Capacité",
        cell: ({ row }) => (
          <div className="font-mono">{row.getValue("capacity")} places</div>
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
    ];
  }, [crud, departments]);

  if (departments.length === 0) {
    return (
      <div className="flex flex-col gap-6">
        <EmptyState
          icon={BuildingIcon}
          title="Aucun département configuré"
          description="Vous devez d'abord configurer au moins un département avant de pouvoir gérer les salles."
          action={<Button asChild><Link to="/admin/departments">Configurer les départements</Link></Button>}
        />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 pb-20" data-testid="admin-rooms-page">
      <div className="flex items-center justify-between">
        <div className="relative pb-4">
          <h1 className="text-4xl font-bold tracking-tight">Salles</h1>
          <div className="absolute bottom-0 left-0 h-1 w-20 bg-primary rounded-full" />
          <p className="text-muted-foreground mt-2">Gérez les espaces physiques pour les soutenances.</p>
        </div>
        <div className="flex gap-2">
          <BulkImportDialog entity="room" triggerButtonText="Importation en masse" onSuccess={refetch} />
          <Button onClick={crud.openCreate} data-testid="admin-rooms-add-button"><Plus data-icon="inline-start" />Nouvelle Salle</Button>
        </div>
      </div>

      <DataTable columns={columns} data={data ?? []} loading={isLoading} getRowId={(row) => row.id} enableRowSelection onSelectedRowsChange={setSelectedRooms} filterColumns="name" filterPlaceholder="Rechercher une salle..." />
      
      <BatchActionsBar
        selectedCount={selectedRooms.length}
        entityLabel="salle(s)"
        actions={[{ key: "delete", label: "Supprimer" }]}
        onDeleteSelected={async () => {
          await Promise.all(selectedRooms.map((r) => crud.deleteMutation(r.id)));
        }}
        isPending={crud.isDeletePending}
        onClearSelection={() => setSelectedRooms([])}
      />
      
      <DeleteAlert isOpen={crud.isDeleteDialogOpen} onOpenChange={crud.setIsDeleteDialogOpen}
        onDelete={crud.handleDelete} entityName={crud.selected?.name} isPending={crud.isDeletePending} />

      <Dialog open={crud.isDialogOpen} onOpenChange={crud.setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{crud.selected ? "Modifier la salle" : "Ajouter une Salle"}</DialogTitle>
            <DialogDescription>
              {crud.selected ? "Mettez à jour les informations de la salle." : "Créez une nouvelle salle pour les examens et soutenances."}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={crud.handleSubmit}>
            <FieldGroup className="py-4">
              <Field>
                <FieldLabel>Nom de la Salle</FieldLabel>
                <Input placeholder="ex: Salle 101" value={crud.formData.name}
                  onChange={(e) => crud.setFormData({ ...crud.formData, name: e.target.value })}
                  required error={crud.fieldErrors?.name} />
              </Field>
              <Field>
                <FieldLabel>Département</FieldLabel>
                <Select value={crud.formData.departmentId ? String(crud.formData.departmentId) : ""}
                  onValueChange={(v) => crud.setFormData({ ...crud.formData, departmentId: v || "" })}>
                  <SelectTrigger><SelectValue placeholder="Choisir un département" /></SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {departments.map((d) => (
                        <SelectItem key={d.id} value={String(d.id)}>{d.name}</SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
                {crud.fieldErrors?.departmentId && <p className="text-sm font-medium text-destructive">{crud.fieldErrors.departmentId}</p>}
              </Field>
              <Field>
                <FieldLabel>Capacité (places)</FieldLabel>
                <Input type="number" min={0} placeholder="ex: 30" value={crud.formData.capacity}
                  onChange={(e) => crud.setFormData({ ...crud.formData, capacity: Number(e.target.value) || 0 })}
                  required error={crud.fieldErrors?.capacity} />
              </Field>
            </FieldGroup>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => crud.setIsDialogOpen(false)}>Annuler</Button>
              <Button type="submit" disabled={crud.isCreatePending || crud.isUpdatePending}>
                {(crud.isCreatePending || crud.isUpdatePending) && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {(crud.isCreatePending || crud.isUpdatePending) ? "Enregistrement..." : "Enregistrer"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

