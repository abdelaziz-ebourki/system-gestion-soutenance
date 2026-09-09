import { useState } from "react";
import { useCreateUser, useUpdateUser, useDeleteUser } from "@/hooks/queries";
import { studentSchema } from "@/lib/validations";
import { useEntityForm } from "@/hooks/use-entity-form";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { Student } from "@/types";

export type StudentFormData = {
  lastName: string;
  firstName: string;
  email: string;
  cne: string;
  majorId: string;
  levelId: string;
};

const defaultForm: StudentFormData = {
  lastName: "", firstName: "", email: "", cne: "", majorId: "", levelId: "",
};

export function useStudentCrud() {
  const form = useEntityForm(studentSchema, defaultForm);
  const create = useCreateUser();
  const update = useUpdateUser();
  const del = useDeleteUser();

  const [selected, setSelected] = useState<Student | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const openCreate = () => {
    form.resetForm();
    setSelected(null);
    setIsDialogOpen(true);
  };

  const openEdit = (entity: Student) => {
    form.resetForm();
    form.setFormData({
      lastName: entity.lastName,
      firstName: entity.firstName,
      email: entity.email,
      cne: entity.cne ?? "",
      majorId: entity.majorId != null ? String(entity.majorId) : "",
      levelId: entity.levelId != null ? String(entity.levelId) : "",
    });
    setSelected(entity);
    setIsDialogOpen(true);
  };

  const openDelete = (entity: Student) => {
    setSelected(entity);
    setIsDeleteDialogOpen(true);
  };

  const handleSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    if (!form.validateForm()) return;
    try {
      const payload = {
        lastName: form.formData.lastName,
        firstName: form.formData.firstName,
        email: form.formData.email,
        cne: form.formData.cne,
        majorId: form.formData.majorId ? Number(form.formData.majorId) : undefined,
        levelId: form.formData.levelId ? Number(form.formData.levelId) : undefined,
        role: "STUDENT" as const,
      };
      if (selected) {
        await update.mutateAsync({ id: selected.id, data: payload });
        toast.success("Étudiant modifié avec succès");
      } else {
        await create.mutateAsync(payload);
        toast.success("Étudiant créé avec succès");
      }
      setIsDialogOpen(false);
      form.resetForm();
      setSelected(null);
    } catch (error) {
      toast.error(getErrorMessage(error, selected ? "Erreur lors de la modification" : "Erreur lors de la création"));
    }
  };

  const handleDelete = async () => {
    if (!selected) return;
    try {
      await del.mutateAsync(selected.id);
      toast.success("Étudiant supprimé");
      setIsDeleteDialogOpen(false);
      setSelected(null);
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la suppression"));
    }
  };

  const updateMutation = (id: number, data: Parameters<typeof update.mutateAsync>[0]["data"]) =>
    update.mutateAsync({ id, data });

  const deleteMutation = (id: number) => del.mutateAsync(id);

  const handleClose = () => setIsDialogOpen(false);
  const handleCloseDelete = () => setIsDeleteDialogOpen(false);

  return {
    ...form,
    selected,
    isDialogOpen,
    setIsDialogOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    openCreate,
    openEdit,
    openDelete,
    handleSubmit,
    handleDelete,
    handleClose,
    handleCloseDelete,
    updateMutation,
    deleteMutation,
    isCreatePending: create.isPending,
    isUpdatePending: update.isPending,
    isDeletePending: del.isPending,
    isPending: create.isPending || update.isPending || del.isPending,
  };
}

