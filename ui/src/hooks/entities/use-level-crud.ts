import { useCreateLevel, useUpdateLevel, useDeleteLevel } from "@/hooks/queries";
import { configNameSchema } from "@/lib/validations";
import { makeEntityCrudHook } from "./make-entity-crud";
import type { Level } from "@/types";

type LevelForm = {
  name: string;
};

export const useLevelCrud = makeEntityCrudHook<Level, LevelForm, { name: string }>({
  schema: configNameSchema,
  defaultForm: { name: "" },
  useCreate: useCreateLevel,
  useUpdate: useUpdateLevel,
  useDelete: useDeleteLevel,
  toForm: (entity) => ({ name: entity.name }),
  toPayload: (form) => ({ name: form.name }),
  messages: {
    created: "Niveau ajouté avec succès",
    updated: "Niveau mis à jour avec succès",
    deleted: "Niveau supprimé avec succès",
  },
});
