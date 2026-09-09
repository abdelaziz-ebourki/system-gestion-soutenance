import { useMemo, useState } from "react";
import { type ColumnDef } from "@tanstack/react-table";
import { Plus, BookOpen, Layers, BuildingIcon, Loader2 } from "lucide-react";

import { useMajors, useDepartments } from "@/hooks/queries";
import type { Major, Level } from "@/types";
import { DataTable } from "@/components/ui/data-table";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { useMajorCrud } from "@/hooks/entities/use-major-crud";
import { useLevelCrud } from "@/hooks/entities/use-level-crud";
import { CrudActions } from "@/components/admin/CrudActions";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";
import { useLevels } from "@/hooks/queries";

export default function Configuration() {
  const { data: majorsData, isLoading: majorsLoading } = useMajors();
  const { data: levelsData, isLoading: levelsLoading } = useLevels();
  const { data: departmentsData } = useDepartments();
  const [selectedMajors, setSelectedMajors] = useState<Major[]>([]);
  const [selectedLevels, setSelectedLevels] = useState<Level[]>([]);
  const majorCrud = useMajorCrud();
  const levelCrud = useLevelCrud();

  const departments = useMemo(() => departmentsData?.items ?? [], [departmentsData]);

  const majorColumns = useMemo<ColumnDef<Major>[]>(() => [
    {
      accessorKey: "name",
      header: "Nom de la Filière",
      cell: ({ row }) => <div className="font-medium">{row.getValue("name")}</div>,
    },
    {
      accessorKey: "departmentId",
      header: "Département",
      cell: ({ row }) => {
        const id = row.getValue("departmentId") as number | undefined;
        if (!id) return <span className="text-muted-foreground italic">—</span>;
        const dept = departments.find((d) => d.id === id);
        return (
          <div className="flex items-center text-muted-foreground">
            <BuildingIcon data-icon="inline-start" />
            {dept?.name ?? id}
          </div>
        );
      },
    },
    {
      accessorKey: "studentCount",
      header: "Étudiants",
      cell: ({ row }) => (
        <div className="font-mono">{row.getValue("studentCount") ?? 0}</div>
      ),
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="text-right">
          <CrudActions entity={row.original} onEdit={majorCrud.openEdit} onDelete={majorCrud.openDelete} />
        </div>
      ),
    },
  ], [majorCrud, departments]);

  const levelColumns = useMemo<ColumnDef<Level>[]>(() => [
    {
      accessorKey: "name",
      header: "Nom du Niveau",
      cell: ({ row }) => <div className="font-medium">{row.getValue("name")}</div>,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="text-right">
          <CrudActions entity={row.original} onEdit={levelCrud.openEdit} onDelete={levelCrud.openDelete} />
        </div>
      ),
    },
  ], [levelCrud]);

  return (
    <div className="flex flex-col gap-6 pb-20" data-testid="admin-configuration-page">
      <div>
        <div className="relative pb-4">
          <h1 className="text-4xl font-bold tracking-tight">Configuration</h1>
          <div className="absolute bottom-0 left-0 h-1 w-20 bg-primary rounded-full" />
        </div>
        <p className="text-muted-foreground mt-2">
          Gérez les entités fondamentales du système.
        </p>
      </div>

      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold flex items-center gap-2">
              <BookOpen data-icon="inline-start" /> Filières
            </h2>
            <Button size="sm" onClick={majorCrud.openCreate}>
              <Plus data-icon="inline-start" /> Ajouter
            </Button>
          </div>
            <DataTable
              columns={majorColumns}
              data={majorsData?.items ?? []}
              loading={majorsLoading}
              getRowId={(row) => row.id}
              enableRowSelection
              onSelectedRowsChange={setSelectedMajors}
              filterColumns="name"
              filterPlaceholder="Rechercher une filière..."
            />
        </div>

        <div className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold flex items-center gap-2">
              <Layers data-icon="inline-start" /> Niveaux
            </h2>
            <Button size="sm" onClick={levelCrud.openCreate}>
              <Plus data-icon="inline-start" /> Ajouter
            </Button>
          </div>
            <DataTable
              columns={levelColumns}
              data={levelsData?.items ?? []}
              loading={levelsLoading}
              getRowId={(row) => row.id}
              enableRowSelection
              onSelectedRowsChange={setSelectedLevels}
              filterColumns="name"
              filterPlaceholder="Rechercher un niveau..."
            />
        </div>
      </div>

      {/* Batch actions for majors */}
      <BatchActionsBar
        selectedCount={selectedMajors.length}
        entityLabel="filière(s)"
        actions={[{ key: "delete", label: "Supprimer" }]}
        onDeleteSelected={async () => {
          await Promise.all(selectedMajors.map((m) => majorCrud.deleteMutation(m.id)));
        }}
        isPending={majorCrud.isDeletePending}
        onClearSelection={() => setSelectedMajors([])}
      />

      {/* Batch actions for levels */}
      <BatchActionsBar
        selectedCount={selectedLevels.length}
        entityLabel="niveau(x)"
        actions={[{ key: "delete", label: "Supprimer" }]}
        onDeleteSelected={async () => {
          await Promise.all(selectedLevels.map((l) => levelCrud.deleteMutation(l.id)));
        }}
        isPending={levelCrud.isDeletePending}
        onClearSelection={() => setSelectedLevels([])}
      />

      {/* Major create/edit dialog */}
      <Dialog open={majorCrud.isDialogOpen} onOpenChange={majorCrud.setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {majorCrud.selected ? "Modifier" : "Ajouter"} une Filière
            </DialogTitle>
            <DialogDescription>
              {majorCrud.selected
                ? "Mettez à jour les informations de la filière."
                : "Créez une nouvelle filière de formation."}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={majorCrud.handleSubmit}>
            <FieldGroup className="py-4">
              <Field>
                <FieldLabel>Nom de la Filière</FieldLabel>
                <Input
                  placeholder="ex: Génie Informatique"
                  value={majorCrud.formData.name}
                  onChange={(e) =>
                    majorCrud.setFormData({ ...majorCrud.formData, name: e.target.value })
                  }
                  required
                  error={majorCrud.fieldErrors?.name}
                />
              </Field>
              <Field>
                <FieldLabel>Département</FieldLabel>
                <Select
                  value={majorCrud.formData.departmentId || "none"}
                  onValueChange={(v) =>
                    majorCrud.setFormData({ ...majorCrud.formData, departmentId: v === "none" ? "" : v })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Choisir un département" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectItem value="none">Aucun</SelectItem>
                      {departments.map((d) => (
                        <SelectItem key={d.id} value={String(d.id)}>
                          {d.name}
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
                disabled={majorCrud.isCreatePending || majorCrud.isUpdatePending}
              >
                {(majorCrud.isCreatePending || majorCrud.isUpdatePending) && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {(majorCrud.isCreatePending || majorCrud.isUpdatePending) ? "Enregistrement..." : "Enregistrer"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Level create/edit dialog */}
      <Dialog open={levelCrud.isDialogOpen} onOpenChange={levelCrud.setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {levelCrud.selected ? "Modifier" : "Ajouter"} un Niveau
            </DialogTitle>
            <DialogDescription>
              {levelCrud.selected
                ? "Mettez à jour les informations du niveau."
                : "Créez un nouveau niveau universitaire."}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={levelCrud.handleSubmit}>
            <FieldGroup className="py-4">
              <Field>
                <FieldLabel>Nom du Niveau</FieldLabel>
                <Input
                  placeholder="ex: L3, M1, Doctorat"
                  value={levelCrud.formData.name}
                  onChange={(e) =>
                    levelCrud.setFormData({ ...levelCrud.formData, name: e.target.value })
                  }
                  required
                  error={levelCrud.fieldErrors?.name}
                />
              </Field>
            </FieldGroup>
            <DialogFooter>
              <Button
                type="submit"
                disabled={levelCrud.isCreatePending || levelCrud.isUpdatePending}
              >
                {(levelCrud.isCreatePending || levelCrud.isUpdatePending) && <Loader2 data-icon="inline-start" className="animate-spin" />}
                {(levelCrud.isCreatePending || levelCrud.isUpdatePending) ? "Enregistrement..." : "Enregistrer"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Delete confirmations */}
      <DeleteAlert
        isOpen={majorCrud.isDeleteDialogOpen}
        onOpenChange={majorCrud.setIsDeleteDialogOpen}
        onDelete={majorCrud.handleDelete}
        entityName={majorCrud.selected?.name}
        isPending={majorCrud.isDeletePending}
      />
      <DeleteAlert
        isOpen={levelCrud.isDeleteDialogOpen}
        onOpenChange={levelCrud.setIsDeleteDialogOpen}
        onDelete={levelCrud.handleDelete}
        entityName={levelCrud.selected?.name}
        isPending={levelCrud.isDeletePending}
      />
    </div>
  );
}
