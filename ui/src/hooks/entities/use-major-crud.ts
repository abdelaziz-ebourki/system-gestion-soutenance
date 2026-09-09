import { useState } from "react";
import { useCreateMajor, useUpdateMajor, useDeleteMajor } from "@/hooks/queries";
import { majorSchema } from "@/lib/validations";
import { useEntityForm } from "@/hooks/use-entity-form";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { Major } from "@/types";

type MajorForm = {
  name: string;
  departmentId: string;
};

const defaultForm: MajorForm = {
  name: "", departmentId: "",
};

export function useMajorCrud() {
  const form = useEntityForm(majorSchema, defaultForm);
  const create = useCreateMajor();
  const update = useUpdateMajor();
  const del = useDeleteMajor();

  const [selected, setSelected] = useState<Major | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const openCreate = () => {
    form.resetForm();
    setSelected(null);
    setIsDialogOpen(true);
  };

  const openEdit = (entity: Major) => {
    form.resetForm();
    form.setFormData({
      name: entity.name,
      departmentId: entity.departmentId != null ? String(entity.departmentId) : "",
    });
    setSelected(entity);
    setIsDialogOpen(true);
  };

  const openDelete = (entity: Major) => {
    setSelected(entity);
    setIsDeleteDialogOpen(true);
  };

  const handleSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    if (!form.validateForm()) return;
    try {
      const payload = {
        name: form.formData.name,
        departmentId: form.formData.departmentId ? Number(form.formData.departmentId) : undefined,
      };
      if (selected) {
        await update.mutateAsync({ id: selected.id, data: payload });
        toast.success("Filière mise à jour avec succès");
      } else {
        await create.mutateAsync(payload);
        toast.success("Filière ajoutée avec succès");
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
      toast.success("Filière supprimée avec succès");
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
