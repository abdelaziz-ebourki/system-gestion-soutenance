import { useState } from "react";
import { useCreateUser, useUpdateUser, useDeleteUser } from "@/hooks/queries";
import { coordinatorSchema } from "@/lib/validations";
import { useEntityForm } from "@/hooks/use-entity-form";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { Coordinator } from "@/types";

type CoordinatorFormData = {
  lastName: string;
  firstName: string;
  email: string;
};

const defaultForm: CoordinatorFormData = {
  lastName: "", firstName: "", email: "",
};

export function useCoordinatorCrud() {
  const form = useEntityForm(coordinatorSchema, defaultForm);
  const create = useCreateUser();
  const update = useUpdateUser();
  const del = useDeleteUser();

  const [selected, setSelected] = useState<Coordinator | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const openCreate = () => {
    form.resetForm();
    setSelected(null);
    setIsDialogOpen(true);
  };

  const openEdit = (entity: Coordinator) => {
    form.resetForm();
    form.setFormData({
      lastName: entity.lastName,
      firstName: entity.firstName,
      email: entity.email,
    });
    setSelected(entity);
    setIsDialogOpen(true);
  };

  const openDelete = (entity: Coordinator) => {
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
        role: "COORDINATOR" as const,
      };
      if (selected) {
        await update.mutateAsync({ id: selected.id, data: payload });
        toast.success("Coordinateur modifié avec succès");
      } else {
        await create.mutateAsync(payload);
        toast.success("Coordinateur ajouté avec succès");
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
      toast.success("Coordinateur supprimé");
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

