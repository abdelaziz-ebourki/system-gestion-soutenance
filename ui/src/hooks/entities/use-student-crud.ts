import { useCreateUser, useUpdateUser, useDeleteUser } from "@/hooks/queries";
import { studentSchema } from "@/lib/validations";
import { makeEntityCrudHook } from "./make-entity-crud";
import type { Student } from "@/types";

export type StudentFormData = {
  lastName: string;
  firstName: string;
  email: string;
  cne: string;
  majorId: string;
  levelId: string;
};

type StudentPayload = {
  lastName: string;
  firstName: string;
  email: string;
  cne?: string;
  majorId?: number;
  levelId?: number;
  role: "STUDENT";
};

export const useStudentCrud = makeEntityCrudHook<Student, StudentFormData, StudentPayload>({
  schema: studentSchema,
  defaultForm: { lastName: "", firstName: "", email: "", cne: "", majorId: "", levelId: "" },
  useCreate: useCreateUser,
  useUpdate: useUpdateUser,
  useDelete: useDeleteUser,
  toForm: (entity) => ({
    lastName: entity.lastName,
    firstName: entity.firstName,
    email: entity.email,
    cne: entity.cne ?? "",
    majorId: entity.majorId != null ? String(entity.majorId) : "",
    levelId: entity.levelId != null ? String(entity.levelId) : "",
  }),
  toPayload: (form) => ({
    lastName: form.lastName,
    firstName: form.firstName,
    email: form.email,
    cne: form.cne,
    majorId: form.majorId ? Number(form.majorId) : undefined,
    levelId: form.levelId ? Number(form.levelId) : undefined,
    role: "STUDENT" as const,
  }),
  messages: {
    created: "Étudiant créé avec succès",
    updated: "Étudiant modifié avec succès",
    deleted: "Étudiant supprimé",
  },
});
