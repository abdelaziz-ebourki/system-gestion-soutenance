import { type ColumnDef, type PaginationState } from "@tanstack/react-table";
import { Link } from "react-router-dom";
import { Plus, GraduationCap, Loader2 } from "lucide-react";

import {
  useStudents, useMajors, useLevels, useDepartments,
} from "@/hooks/queries";
import { DEFAULT_API_LIMIT, MAX_TEACHER_FETCH_LIMIT } from "@/lib/constants";
import { type Student, type Major, type Department, type Level } from "@/types";
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
import { useStudentCrud } from "@/hooks/entities/use-student-crud";
import { CrudActions } from "@/components/admin/CrudActions";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { useMemo, useState } from "react";

export default function Students() {
  const [pagination, setPagination] = useState<PaginationState>({
    pageIndex: 0,
    pageSize: DEFAULT_API_LIMIT,
  });
  const [isFiltering, setIsFiltering] = useState(false);
  const [selectedStudents, setSelectedStudents] = useState<Student[]>([]);

  const { data: studentsData, isLoading, refetch } = useStudents({
    page: isFiltering ? 0 : pagination.pageIndex,
    limit: isFiltering ? MAX_TEACHER_FETCH_LIMIT : pagination.pageSize,
  });
  const { data: majorsData } = useMajors();
  const { data: levelsData } = useLevels();
  const { data: departmentsData } = useDepartments();
  const majors = useMemo(() => majorsData?.items ?? [], [majorsData]);
  const levels = useMemo(() => levelsData?.items ?? [], [levelsData]);
  const departments = useMemo(() => departmentsData?.items ?? [], [departmentsData]);
  const crud = useStudentCrud();

  const data = studentsData?.items ?? [];
  const pageCount = studentsData?.pageCount ?? 0;

  const columns = useMemo<ColumnDef<Student>[]>(() => [
    { accessorKey: "cne", header: "CNE", cell: ({ row }) => <code className="font-bold">{row.getValue("cne")}</code> },
    { accessorKey: "codeApogee", header: "Code Apogée", cell: ({ row }) => <code className="text-muted-foreground">{row.getValue("codeApogee") || "—"}</code> },
    { accessorKey: "lastName", header: "Nom", cell: ({ row }) => <div className="font-medium">{row.original.lastName}</div> },
    { accessorKey: "firstName", header: "Prénom", cell: ({ row }) => <div className="font-medium">{row.original.firstName}</div> },
    { accessorKey: "email", header: "Email" },
    {
      accessorKey: "majorId",
      header: "Filière",
      filterFn: "equalsString",
       cell: ({ row }) => {
         const id = row.getValue("majorId") as number;
         return majors.find((f: Major) => f.id === id)?.name || id;
       },
    },
    {
      accessorKey: "departmentId",
      header: "Département",
      filterFn: "equalsString",
       cell: ({ row }) => {
         const id = row.getValue("departmentId") as number;
         if (!id) return "—";
         const name = departments.find((d: Department) => d.id === id)?.name || row.original.departmentName;
         return name || "—";
       },
    },
    {
      accessorKey: "levelId",
      header: "Niveau",
      filterFn: "equalsString",
       cell: ({ row }) => {
         const id = row.getValue("levelId") as number;
         const name = levels.find((l: Level) => l.id === id)?.name || id;
         return <Badge variant="secondary">{name}</Badge>;
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
  ], [crud, majors, levels, departments]);

  if (majors.length === 0 || levels.length === 0) {
    const parts: string[] = [];
    if (majors.length === 0) parts.push("filières");
    if (levels.length === 0) parts.push("niveaux");
    return (
      <div className="flex flex-col gap-6">
        <EmptyState
          icon={GraduationCap}
          title="Configuration requise"
          description={`Vous devez d'abord configurer ${parts.join(" et ")} avant de pouvoir gérer les étudiants.`}
          action={<Button asChild><Link to="/admin/config">Configurer</Link></Button>}
        />
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 pb-20" data-testid="admin-students-page">
      <div className="flex items-center justify-between">
        <div className="relative pb-4">
          <h1 className="text-4xl font-bold tracking-tight">Étudiants</h1>
          <div className="absolute bottom-0 left-0 h-1 w-20 bg-primary rounded-full" />
          <p className="text-muted-foreground mt-2">Gestion des étudiants et affectations.</p>
        </div>
        <div className="flex gap-2">
          <BulkImportDialog entity="student" triggerButtonText="Importation en masse" onSuccess={refetch} />
          <Button onClick={crud.openCreate} data-testid="admin-students-add-button">
            <Plus data-icon="inline-start" /> Nouvel Étudiant
          </Button>
        </div>
      </div>

        <DataTable columns={columns} data={data} loading={isLoading} getRowId={(row) => row.id} enableRowSelection onSelectedRowsChange={setSelectedStudents}
          manualPagination={!isFiltering} pageCount={!isFiltering ? pageCount : undefined}
          pagination={!isFiltering ? pagination : undefined} onPaginationChange={!isFiltering ? setPagination : undefined}
          onFiltering={setIsFiltering}
          filterColumns={["lastName", "firstName", "email", "cne"]} filterPlaceholder="Rechercher par nom, prénom, email ou CNE..."
          filters={[
             { column: "majorId", label: "Filière", options: majors.map((f: Major) => ({ value: String(f.id), label: f.name })) },
             { column: "levelId", label: "Niveau", options: levels.map((l: Level) => ({ value: String(l.id), label: l.name })) },
             { column: "departmentId", label: "Département", options: departments.map((d: Department) => ({ value: String(d.id), label: d.name })) },
          ]} />

      <BatchActionsBar
        selectedCount={selectedStudents.length}
        entityLabel="étudiant(s)"
        actions={[{ key: "major", label: "Modifier la filière" }, { key: "level", label: "Modifier le niveau" }, { key: "delete", label: "Supprimer" }]}
        fieldOptionsMap={{
           major: majors.map((f: Major) => ({ value: String(f.id), label: f.name })),
           level: levels.map((l: Level) => ({ value: String(l.id), label: l.name })),
        }}
        onUpdateField={async (field, value) => {
          if (field === "major") {
            await Promise.all(selectedStudents.map((s) => crud.updateMutation(s.id, { lastName: s.lastName, firstName: s.firstName, email: s.email, majorId: Number(value), role: "STUDENT" })));
          } else if (field === "level") {
            await Promise.all(selectedStudents.map((s) => crud.updateMutation(s.id, { lastName: s.lastName, firstName: s.firstName, email: s.email, levelId: Number(value), role: "STUDENT" })));
          }
        }}
        onDeleteSelected={async () => {
          await Promise.all(selectedStudents.map((s) => crud.deleteMutation(s.id)));
        }}
        isPending={crud.isDeletePending}
        onClearSelection={() => setSelectedStudents([])}
      />

      <Dialog open={crud.isDialogOpen} onOpenChange={crud.setIsDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{crud.selected ? "Modifier" : "Ajouter"} Étudiant</DialogTitle>
            <DialogDescription>Remplissez les informations académiques de l'étudiant.</DialogDescription>
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
              <Field>
                <FieldLabel>CNE</FieldLabel>
                <Input value={crud.formData.cne}
                  onChange={(e) => crud.setFormData({ ...crud.formData, cne: e.target.value })}
                  required error={crud.fieldErrors?.cne} />
              </Field>
              <Field>
                <FieldLabel>Niveau</FieldLabel>
                <Select value={crud.formData.levelId ? String(crud.formData.levelId) : ""}
                  onValueChange={(v) => crud.setFormData({ ...crud.formData, levelId: v || "" })}>
                  <SelectTrigger><SelectValue placeholder="Choisir un niveau" /></SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {levels.map((n: Level) => <SelectItem key={n.id} value={String(n.id)}>{n.name}</SelectItem>)}
                    </SelectGroup>
                  </SelectContent>
                </Select>
                {crud.fieldErrors?.levelId && <p className="text-sm font-medium text-destructive">{crud.fieldErrors.levelId}</p>}
              </Field>
              <Field className="col-span-2">
                <FieldLabel>Filière</FieldLabel>
                <Select value={crud.formData.majorId ? String(crud.formData.majorId) : ""}
                  onValueChange={(v) => crud.setFormData({ ...crud.formData, majorId: v || "" })}>
              <SelectTrigger><SelectValue placeholder="Choisir une filière" /></SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                        {majors.map((f: Major) => <SelectItem key={f.id} value={String(f.id)}>{f.name}</SelectItem>)}
                    </SelectGroup>
                  </SelectContent>
                </Select>
                {crud.fieldErrors?.majorId && <p className="text-sm font-medium text-destructive">{crud.fieldErrors.majorId}</p>}
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
