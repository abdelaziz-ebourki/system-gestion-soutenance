import { useCreateMajor, useUpdateMajor, useDeleteMajor } from "@/hooks/queries";
import { majorSchema } from "@/lib/validations";
import { makeEntityCrudHook } from "./make-entity-crud";
import type { Major } from "@/types";

type MajorForm = {
  name: string;
  departmentId?: string;
};

export const useMajorCrud = makeEntityCrudHook<
  Major,
  MajorForm,
  { name: string; departmentId?: number }
>({
  schema: majorSchema,
  defaultForm: { name: "", departmentId: "" },
  useCreate: useCreateMajor,
  useUpdate: useUpdateMajor,
  useDelete: useDeleteMajor,
  toForm: (entity) => ({
    name: entity.name,
    departmentId: entity.departmentId != null ? String(entity.departmentId) : "",
  }),
  toPayload: (form) => ({
    name: form.name,
    departmentId: form.departmentId ? Number(form.departmentId) : undefined,
  }),
  messages: {
    created: "Filière ajoutée avec succès",
    updated: "Filière mise à jour avec succès",
    deleted: "Filière supprimée avec succès",
  },
});
