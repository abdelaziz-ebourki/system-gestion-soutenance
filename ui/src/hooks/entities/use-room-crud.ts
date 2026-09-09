import { useState } from "react";
import { useCreateRoom, useUpdateRoom, useDeleteRoom } from "@/hooks/queries";
import { roomSchema } from "@/lib/validations";
import { useEntityForm } from "@/hooks/use-entity-form";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import type { Room } from "@/types";

type RoomForm = {
  name: string;
  capacity: number;
  departmentId: string;
};

const defaultForm: RoomForm = {
  name: "", capacity: 0, departmentId: "",
};

export function useRoomCrud() {
  const form = useEntityForm(roomSchema, defaultForm);
  const create = useCreateRoom();
  const update = useUpdateRoom();
  const del = useDeleteRoom();

  const [selected, setSelected] = useState<Room | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const openCreate = () => {
    form.resetForm();
    setSelected(null);
    setIsDialogOpen(true);
  };

  const openEdit = (entity: Room) => {
    form.resetForm();
    form.setFormData({
      name: entity.name,
      capacity: entity.capacity,
      departmentId: String(entity.departmentId),
    });
    setSelected(entity);
    setIsDialogOpen(true);
  };

  const openDelete = (entity: Room) => {
    setSelected(entity);
    setIsDeleteDialogOpen(true);
  };

  const handleSubmit = async (e: { preventDefault(): void }) => {
    e.preventDefault();
    if (!form.validateForm()) return;
    try {
      const payload = {
        name: form.formData.name,
        capacity: form.formData.capacity,
        departmentId: Number(form.formData.departmentId),
      };
      if (selected) {
        await update.mutateAsync({ id: selected.id, data: payload });
        toast.success("Salle modifiée avec succès");
      } else {
        await create.mutateAsync(payload);
        toast.success("Salle ajoutée avec succès");
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
      toast.success("Salle supprimée");
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

