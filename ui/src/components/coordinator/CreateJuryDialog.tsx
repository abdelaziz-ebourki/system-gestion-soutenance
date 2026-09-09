import * as React from "react";
import { Loader2 } from "lucide-react";

import {
  useCoordinatorTeachersList, useProjects, useCreateJury, useUpdateJury, useJuryRoleTemplates,
} from "@/hooks/queries";
import { useEntityForm } from "@/hooks/use-entity-form";
import { validate, jurySchema } from "@/lib/validations";
import { toast } from "sonner";
import { getFullName, getErrorMessage } from "@/lib/utils";
import type { Jury } from "@/types";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

interface CreateJuryDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
  jury?: Jury | null;
}

interface SlotEntry {
  roleName: string;
  teacherId: string;
}

const defaultForm = { projectId: "", templateId: "", members: [] as SlotEntry[] };

export function CreateJuryDialog({
  open,
  onOpenChange,
  onSuccess,
  jury,
}: CreateJuryDialogProps) {
  const teachersQuery = useCoordinatorTeachersList();
  const projectsQuery = useProjects();
  const templatesQuery = useJuryRoleTemplates();
  const createJuryMutation = useCreateJury();
  const updateJuryMutation = useUpdateJury();

  const teachers = React.useMemo(() => teachersQuery.data?.items ?? [], [teachersQuery.data]);
  const projects = React.useMemo(() => projectsQuery.data?.items ?? [], [projectsQuery.data]);
  const templates = React.useMemo(() => templatesQuery.data?.items ?? [], [templatesQuery.data]);
  const isLoadingOptions = teachersQuery.isLoading || projectsQuery.isLoading || templatesQuery.isLoading;
  const isEdit = !!jury;

  const form = useEntityForm(jurySchema, defaultForm);

  const selectedProject = projects.find((p) => String(p.id) === form.formData.projectId);
  const selectedTemplate = templates.find((t) => String(t.id) === form.formData.templateId);

  const slotEntries: { index: number; roleName: string; label: string }[] = React.useMemo(() => {
    if (!selectedTemplate) return [];
    const entries: { index: number; roleName: string; label: string }[] = [];
    for (const role of selectedTemplate.roles) {
      for (let i = 0; i < role.count; i++) {
        entries.push({
          index: entries.length,
          roleName: role.name,
          label: role.count > 1 ? `${role.name} ${i + 1}` : role.name,
        });
      }
    }
    return entries;
  }, [selectedTemplate]);

  const prevTemplateKey = React.useRef<string | null>(null);
  React.useEffect(() => {
    const key = selectedTemplate ? `${selectedTemplate.id}-${JSON.stringify(selectedTemplate.roles)}` : null;
    if (key !== null && key !== prevTemplateKey.current) {
      prevTemplateKey.current = key;
      form.setFormData({
        ...form.formData,
        members: slotEntries.map((s) => ({ roleName: s.roleName, teacherId: "" })),
      });
    }
  }, [selectedTemplate, slotEntries, form]);

  React.useEffect(() => {
    if (open) {
      if (jury) {
        form.resetForm();
        form.setFormData({
          projectId: String(jury.projectId),
          templateId: "",
          members: jury.members.map((m) => ({ roleName: m.roleName, teacherId: String(m.teacherId) })),
        });
      } else {
        form.resetForm();
      }
    }
  }, [open, form, jury]);

  const availableTemplates = React.useMemo(() => {
    if (!selectedProject) return [];
    return templates.filter((t) => t.defenseType === selectedProject.defenseType);
  }, [templates, selectedProject]);

  const handleSubmit = async (event: React.SyntheticEvent) => {
    event.preventDefault();

    const errors = validate(jurySchema, form.formData);
    if (errors) {
      form.setFieldErrors(errors);
      return;
    }

    try {
      const payload = {
        projectId: Number(form.formData.projectId),
        members: form.formData.members.map((m) => ({ roleName: m.roleName, teacherId: Number(m.teacherId) })),
      };

      if (isEdit && jury) {
        await updateJuryMutation.mutateAsync({ id: jury.id, data: payload });
        toast.success("Jury modifié avec succès");
      } else {
        await createJuryMutation.mutateAsync(payload);
        toast.success("Jury créé avec succès");
      }
      form.setFieldErrors({});
      onSuccess();
      onOpenChange(false);
    } catch (error) {
      toast.error(getErrorMessage(error, `Erreur lors de la ${isEdit ? "modification" : "création"} du jury`));
    }
  };

  const filteredProjects = React.useMemo(
    () => projects,
    [projects],
  );

  const filteredTeachersBySlot = React.useMemo(
    () => {
      const assignedIds = new Set(
        form.formData.members.map((m) => m.teacherId).filter(Boolean),
      );
      return form.formData.members.map((m) =>
        teachers.filter((t) => String(t.id) === m.teacherId || !assignedIds.has(String(t.id))),
      );
    },
    [teachers, form.formData.members],
  );

  const updateMember = (slotIndex: number, teacherId: string) => {
    const updated = [...form.formData.members];
    for (let i = 0; i < updated.length; i++) {
      if (i !== slotIndex && updated[i].teacherId === teacherId) {
        updated[i] = { ...updated[i], teacherId: "" };
      }
    }
    updated[slotIndex] = { ...updated[slotIndex], teacherId };
    form.setFormData({ ...form.formData, members: updated });
  };

  const isPending = isEdit ? updateJuryMutation.isPending : createJuryMutation.isPending;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-155" data-testid="coord-jury-create-dialog">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Modifier le jury" : "Nouveau jury"}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? "Mettez à jour la composition du jury."
              : "Sélectionnez un projet, un modèle de jury, puis assignez les enseignants aux rôles."}
          </DialogDescription>
        </DialogHeader>
        <form id="create-jury-form" onSubmit={handleSubmit}>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="jury-project">Projet</FieldLabel>
              <Select
                value={form.formData.projectId}
                onValueChange={(val) => form.setFormData({
                  ...form.formData,
                  projectId: val || "",
                  templateId: "",
                  members: [],
                })}
                disabled={isLoadingOptions || isEdit}
              >
                <SelectTrigger id="jury-project" fullWidth data-testid="coord-jury-create-project">
                  <SelectValue placeholder="Sélectionner un projet" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {filteredProjects.map((project) => (
                      <SelectItem key={project.id} value={String(project.id)}>
                        {project.title}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
              {form.fieldErrors?.projectId && (
                <p className="text-sm font-medium text-destructive">{form.fieldErrors.projectId}</p>
              )}
            </Field>

            {selectedProject && (
              <Field>
                <FieldLabel htmlFor="jury-template">Modèle de jury</FieldLabel>
                <Select
                  value={form.formData.templateId}
                  onValueChange={(val) => form.setFormData({
                    ...form.formData,
                    templateId: val || "",
                    members: [],
                  })}
                  disabled={isLoadingOptions || isEdit}
                >
                  <SelectTrigger id="jury-template" fullWidth data-testid="coord-jury-create-template">
                    <SelectValue placeholder="Sélectionner un modèle" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {availableTemplates.map((t) => (
                      <SelectItem key={t.id} value={String(t.id)}>
                        {t.name}
                      </SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
                {form.fieldErrors?.templateId && (
                  <p className="text-sm font-medium text-destructive">{form.fieldErrors.templateId}</p>
                )}
              </Field>
            )}

            {slotEntries.map((slot, idx) => {
              const value = form.formData.members[idx]?.teacherId ?? "";
              const filtered = filteredTeachersBySlot[idx] ?? [];
              return (
                <Field key={`slot-${idx}`} data-testid={`coord-jury-create-slot-${idx}`}>
                  <FieldLabel>{slot.label}</FieldLabel>
                  <Select
                    value={value}
                    onValueChange={(val) => updateMember(idx, val || "")}
                    disabled={isLoadingOptions}
                  >
                    <SelectTrigger fullWidth>
                      <SelectValue placeholder={`Sélectionner ${slot.label.toLowerCase()}`} />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectGroup>
                        {filtered.map((teacher) => (
                          <SelectItem key={teacher.id} value={String(teacher.id)}>
                            {getFullName(teacher)}
                          </SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                </Field>
              );
            })}
          </FieldGroup>
        </form>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} data-testid="coord-jury-create-cancel">
            Annuler
          </Button>
          <Button
            type="submit"
            form="create-jury-form"
            disabled={isLoadingOptions || isPending}
            data-testid="coord-jury-create-submit"
          >
            {isPending && <Loader2 data-icon="inline-start" className="animate-spin" />}
            {isPending ? (isEdit ? "Enregistrer" : "Créer le jury") : (isEdit ? "Enregistrer" : "Créer le jury")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

