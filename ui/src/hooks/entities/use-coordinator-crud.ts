import { useCreateUser, useUpdateUser, useDeleteUser } from "@/hooks/queries";
import { coordinatorSchema } from "@/lib/validations";
import { makeEntityCrudHook } from "./make-entity-crud";
import type { Coordinator } from "@/types";

type CoordinatorFormData = {
  lastName: string;
  firstName: string;
  email: string;
};

type CoordinatorPayload = {
  lastName: string;
  firstName: string;
  email: string;
  role: "COORDINATOR";
};

export const useCoordinatorCrud = makeEntityCrudHook<Coordinator, CoordinatorFormData, CoordinatorPayload>({
  schema: coordinatorSchema,
  defaultForm: { lastName: "", firstName: "", email: "" },
  useCreate: useCreateUser,
  useUpdate: useUpdateUser,
  useDelete: useDeleteUser,
  toForm: (entity) => ({
    lastName: entity.lastName,
    firstName: entity.firstName,
    email: entity.email,
  }),
  toPayload: (form) => ({
    lastName: form.lastName,
    firstName: form.firstName,
    email: form.email,
    role: "COORDINATOR" as const,
  }),
  messages: {
    created: "Coordinateur ajouté avec succès",
    updated: "Coordinateur modifié avec succès",
    deleted: "Coordinateur supprimé",
  },
});
