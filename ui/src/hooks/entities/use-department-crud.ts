import { useCreateDepartment, useUpdateDepartment, useDeleteDepartment } from "@/hooks/queries";
import { departmentSchema } from "@/lib/validations";
import { makeEntityCrudHook } from "./make-entity-crud";
import type { Department } from "@/types";

type DepartmentForm = {
  name: string;
  code: string;
  headId?: string;
};

export const useDepartmentCrud = makeEntityCrudHook<
  Department,
  DepartmentForm,
  { name: string; code: string; headId?: number }
>({
  schema: departmentSchema,
  defaultForm: { name: "", code: "", headId: "" },
  useCreate: useCreateDepartment,
  useUpdate: useUpdateDepartment,
  useDelete: useDeleteDepartment,
  toForm: (entity) => ({
    name: entity.name,
    code: entity.code,
    headId: entity.headId != null ? String(entity.headId) : "",
  }),
  toPayload: (form) => ({
    name: form.name,
    code: form.code,
    headId: form.headId ? Number(form.headId) : undefined,
  }),
  messages: {
    created: "Département ajouté avec succès",
    updated: "Département mis à jour avec succès",
    deleted: "Département supprimé avec succès",
  },
});
