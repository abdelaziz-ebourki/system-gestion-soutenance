import * as React from "react";
import { Loader2 } from "lucide-react";

import { useCoordinatorTeachersList, useCoordinatorStudents, useCreateProject, useUpdateProject } from "@/hooks/queries";
import { useEntityForm } from "@/hooks/use-entity-form";
import { validate, projectSchema } from "@/lib/validations";
import type { DefenseType, Project } from "@/types";
import { DEFENSE_TYPE_OPTIONS, MAX_STUDENT_FETCH_LIMIT } from "@/lib/constants";
import { toast } from "sonner";
import { getFullName, getErrorMessage } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Combobox, ComboboxInput, ComboboxContent, ComboboxList, ComboboxItem, ComboboxEmpty, ComboboxValue } from "@/components/ui/combobox";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { MultiSelect } from "@/components/ui/multi-select";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";

interface ProjectDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
  project?: Project | null;
}

const defaultForm = { title: "", description: "", supervisorId: "", studentIds: [] as string[], defenseType: "pfe" as DefenseType };

export function ProjectDialog({
  open,
  onOpenChange,
  onSuccess,
  project,
}: ProjectDialogProps) {
  const teachersQuery = useCoordinatorTeachersList();
  const createProjectMutation = useCreateProject();
  const updateProjectMutation = useUpdateProject();
  const teachers = teachersQuery.data?.items ?? [];
  const studentsQuery = useCoordinatorStudents(MAX_STUDENT_FETCH_LIMIT);
  const studentOptions = React.useMemo(() => 
    (studentsQuery.data?.items ?? []).map((s) => ({
      value: String(s.id),
      label: getFullName(s),
    })),
    [studentsQuery.data?.items],
  );

  const form = useEntityForm(projectSchema, defaultForm);

  const isEdit = !!project;
  const formRef = React.useRef(form);
  formRef.current = form; // eslint-disable-line react-hooks/refs

  React.useEffect(() => {
    if (open) {
      const f = formRef.current;
      if (project) {
        f.resetForm();
        f.setFormData({
          title: project.title,
          description: project.description || "",
          supervisorId: "",
          studentIds: [],
          defenseType: project.defenseType as DefenseType,
        });
      } else {
        f.resetForm();
      }
    }
  }, [open, project, formRef]);

  const handleSubmit = async (event: React.SyntheticEvent) => {
    event.preventDefault();

    const errors = validate(projectSchema, form.formData);
    if (errors) {
      form.setFieldErrors(errors);
      return;
    }

    try {
      const supervisor = teachers.find(
        (teacher) => String(teacher.id) === form.formData.supervisorId,
      );

      if (!supervisor) {
        toast.error("Encadrant introuvable");
        return;
      }

      if (isEdit && project) {
        await updateProjectMutation.mutateAsync({
          id: project.id,
          data: {
            title: form.formData.title,
            description: form.formData.description ?? "",
            defenseType: form.formData.defenseType,
          },
        });
        toast.success("Projet mis à jour");
      } else {
        await createProjectMutation.mutateAsync({
          title: form.formData.title,
          description: form.formData.description ?? "",
          supervisorId: Number(form.formData.supervisorId),
          defenseType: form.formData.defenseType,
          studentIds: form.formData.studentIds.map(Number),
        });
        toast.success("Projet créé avec succès");
      }

      form.setFieldErrors({});
      onSuccess();
      onOpenChange(false);
    } catch (error) {
      toast.error(getErrorMessage(error, isEdit ? "Erreur lors de la mise à jour du projet" : "Erreur lors de la creation du projet"));
    }
  };

  const formId = isEdit ? "edit-project-form" : "create-project-form";

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-160" data-testid="coord-project-dialog">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Modifier le projet" : "Nouveau projet"}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? "Mettez à jour le sujet et l'encadrement."
              : "Ajoutez un sujet et son encadrant."}
          </DialogDescription>
        </DialogHeader>
         <form
            id={formId}
            onSubmit={handleSubmit}
            data-testid="coord-project-dialog-form"
          >
           <FieldGroup>
             <Field>
               <FieldLabel htmlFor={`${formId}-title`}>Titre</FieldLabel>
               <Input
                 id={`${formId}-title`}
                 value={form.formData.title}
                 onChange={(event) => form.setFormData({ ...form.formData, title: event.target.value })}
                 required
                 error={form.fieldErrors?.title}
                 data-testid="coord-project-dialog-title"
               />
             </Field>

             <Field>
               <FieldLabel htmlFor={`${formId}-description`}>Description</FieldLabel>
               <Textarea
                 id={`${formId}-description`}
                 value={form.formData.description}
                 onChange={(event) => form.setFormData({ ...form.formData, description: event.target.value })}
                 className="min-h-28"
                 data-testid="coord-project-dialog-description"
               />
             </Field>

             <Field>
               <FieldLabel htmlFor={`${formId}-defenseType`}>Type de soutenance</FieldLabel>
               <Select
                 value={form.formData.defenseType}
                 onValueChange={(val) => form.setFormData({ ...form.formData, defenseType: val as DefenseType })}
               >
                 <SelectTrigger id={`${formId}-defenseType`} fullWidth data-testid="coord-project-dialog-defense-type">
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

             <Field>
               <FieldLabel>Encadrant</FieldLabel>
               <Combobox
                 value={form.formData.supervisorId}
                 onValueChange={(val) => form.setFormData({ ...form.formData, supervisorId: val || "" })}
                 data-testid="coord-project-dialog-supervisor"
               >
                 <ComboboxInput placeholder="Rechercher un encadrant..." showTrigger>
                   <ComboboxValue />
                 </ComboboxInput>
                 <ComboboxContent>
                   <ComboboxList>
                     {teachers.map((teacher) => (
                       <ComboboxItem key={teacher.id} value={String(teacher.id)}>
                         {getFullName(teacher)}
                       </ComboboxItem>
                     ))}
                     <ComboboxEmpty>Aucun encadrant trouvé</ComboboxEmpty>
                   </ComboboxList>
                 </ComboboxContent>
               </Combobox>
               {form.fieldErrors?.supervisorId && (
                 <p className="text-sm font-medium text-destructive">{form.fieldErrors.supervisorId}</p>
               )}
             </Field>

             <Field>
               <FieldLabel>Étudiants</FieldLabel>
               <MultiSelect
                 options={studentOptions}
                 value={form.formData.studentIds}
                 onChange={(ids) => form.setFormData({ ...form.formData, studentIds: ids })}
                 placeholder="Sélectionner des étudiants..."
                 disabled={studentsQuery.isLoading}
                 data-testid="coord-project-dialog-students"
               />
               {form.fieldErrors?.studentIds && (
                 <p className="text-sm font-medium text-destructive">{form.fieldErrors.studentIds}</p>
               )}
             </Field>
           </FieldGroup>
         </form>
        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            data-testid="coord-project-dialog-cancel"
          >
            Annuler
          </Button>
          <Button
            type="submit"
            form={formId}
            disabled={teachersQuery.isLoading || (isEdit ? updateProjectMutation.isPending : createProjectMutation.isPending)}
            data-testid="coord-project-dialog-submit"
          >
            {(isEdit ? updateProjectMutation.isPending : createProjectMutation.isPending) && <Loader2 data-icon="inline-start" className="animate-spin" />}
            {(isEdit ? updateProjectMutation.isPending : createProjectMutation.isPending) ? (isEdit ? "Sauvegarder" : "Créer le projet") : (isEdit ? "Sauvegarder" : "Créer le projet")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

