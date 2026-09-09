import { useState } from "react";
import { useCreateUser, useUpdateUser, useDeleteUser } from "@/hooks/queries";
import { teacherSchema } from "@/lib/validations";
import { useEntityForm } from "@/hooks/use-entity-form";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { Teacher } from "@/types";

type TeacherFormData = {
  lastName: string;
  firstName: string;
  email: string;
  departmentId: string;
};

const defaultForm: TeacherFormData = {
  lastName: "", firstName: "", email: "", departmentId: "",
};

export function useTeacherCrud() {
  const form = useEntityForm(teacherSchema, defaultForm);
  const create = useCreateUser();
  const update = useUpdateUser();
  const del = useDeleteUser();

  const [selected, setSelected] = useState<Teacher | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const openCreate = () => {
    form.resetForm();
    setSelected(null);
    setIsDialogOpen(true);
  };

  const openEdit = (entity: Teacher) => {
    form.resetForm();
    form.setFormData({
      lastName: entity.lastName,
      firstName: entity.firstName,
      email: entity.email,
      departmentId: entity.departmentId != null ? String(entity.departmentId) : "",
    });
    setSelected(entity);
    setIsDialogOpen(true);
  };

  const openDelete = (entity: Teacher) => {
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
        departmentId: form.formData.departmentId ? Number(form.formData.departmentId) : undefined,
        role: "TEACHER" as const,
      };
      if (selected) {
        await update.mutateAsync({ id: selected.id, data: payload });
        toast.success("Enseignant modifié avec succès");
      } else {
        await create.mutateAsync(payload);
        toast.success("Enseignant créé avec succès");
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
      toast.success("Enseignant supprimé");
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

