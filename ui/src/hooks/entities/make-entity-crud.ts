import { useState } from "react";
import type { z } from "zod";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/utils";
import { useEntityForm } from "@/hooks/use-entity-form";

export interface EntityCrudMessages {
  created: string;
  updated: string;
  deleted: string;
}

interface CreateMutation<P> {
  mutateAsync: (payload: P) => Promise<unknown>;
  isPending: boolean;
}

interface UpdateMutation<P> {
  mutateAsync: (vars: { id: number; data: P }) => Promise<unknown>;
  isPending: boolean;
}

interface DeleteMutation {
  mutateAsync: (id: number) => Promise<unknown>;
  isPending: boolean;
}

export interface EntityCrudConfig<E extends { id: number }, F, P> {
  schema: z.ZodType<F>;
  defaultForm: F;
  useCreate: () => CreateMutation<P>;
  useUpdate: () => UpdateMutation<P>;
  useDelete: () => DeleteMutation;
  toForm: (entity: E) => F;
  toPayload: (form: F) => P;
  messages: EntityCrudMessages;
}

export function makeEntityCrudHook<E extends { id: number }, F, P>(config: EntityCrudConfig<E, F, P>) {
  return function useEntityCrud() {
    const form = useEntityForm(config.schema, config.defaultForm);
    const create = config.useCreate();
    const update = config.useUpdate();
    const del = config.useDelete();

    const [selected, setSelected] = useState<E | null>(null);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

    const openCreate = () => {
      form.resetForm();
      setSelected(null);
      setIsDialogOpen(true);
    };

    const openEdit = (entity: E) => {
      form.resetForm();
      form.setFormData(config.toForm(entity));
      setSelected(entity);
      setIsDialogOpen(true);
    };

    const openDelete = (entity: E) => {
      setSelected(entity);
      setIsDeleteDialogOpen(true);
    };

    const handleSubmit = async (e: { preventDefault(): void }) => {
      e.preventDefault();
      if (!form.validateForm()) return;
      try {
        const payload = config.toPayload(form.formData);
        if (selected) {
          await update.mutateAsync({ id: selected.id, data: payload });
          toast.success(config.messages.updated);
        } else {
          await create.mutateAsync(payload);
          toast.success(config.messages.created);
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
        toast.success(config.messages.deleted);
        setIsDeleteDialogOpen(false);
        setSelected(null);
      } catch (error) {
        toast.error(getErrorMessage(error, "Erreur lors de la suppression"));
      }
    };

    const updateMutation = (id: number, data: P) => update.mutateAsync({ id, data });

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
  };
}
