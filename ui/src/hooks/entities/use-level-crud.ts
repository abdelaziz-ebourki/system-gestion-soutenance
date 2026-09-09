import { useState } from "react";
import { useCreateLevel, useUpdateLevel, useDeleteLevel } from "@/hooks/queries";
import { configNameSchema } from "@/lib/validations";
import { useEntityForm } from "@/hooks/use-entity-form";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { Level } from "@/types";

type LevelForm = {
  name: string;
};

const defaultForm: LevelForm = {
  name: "",
};

export function useLevelCrud() {
  const form = useEntityForm(configNameSchema, defaultForm);
  const create = useCreateLevel();
  const update = useUpdateLevel();
  const del = useDeleteLevel();

  const [selected, setSelected] = useState<Level | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const openCreate = () => {
    form.resetForm();
    setSelected(null);
    setIsDialogOpen(true);
  };

  const openEdit = (entity: Level) => {
    form.resetForm();
    form.setFormData({ name: entity.name });
    setSelected(entity);
    setIsDialogOpen(true);
  };

  const openDelete = (entity: Level) => {
    setSelected(entity);
    setIsDeleteDialogOpen(true);
  };

  const handleSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    if (!form.validateForm()) return;
    try {
      const payload = { name: form.formData.name };
      if (selected) {
        await update.mutateAsync({ id: selected.id, data: payload });
        toast.success("Niveau mis à jour avec succès");
      } else {
        await create.mutateAsync(payload);
        toast.success("Niveau ajouté avec succès");
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
      toast.success("Niveau supprimé avec succès");
      setIsDeleteDialogOpen(false);
      setSelected(null);
    } catch (error) {
      toast.error(getErrorMessage(error, "Erreur lors de la suppression"));
    }
  };

  const updateMutation = (id: number, data: Parameters<typeof update.mutateAsync>[0]["data"]) =>
    update.mutateAsync({ id, data });

  const deleteMutation = (id: number) => del.mutateAsync(id);

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
    updateMutation,
    deleteMutation,
    isCreatePending: create.isPending,
    isUpdatePending: update.isPending,
    isDeletePending: del.isPending,
    isPending: create.isPending || update.isPending || del.isPending,
  };
}
