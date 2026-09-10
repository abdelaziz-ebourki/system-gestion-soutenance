import { useCreateUser, useUpdateUser, useDeleteUser } from "@/hooks/queries";
import { teacherSchema } from "@/lib/validations";
import { makeEntityCrudHook } from "./make-entity-crud";
import type { Teacher } from "@/types";

type TeacherFormData = {
  lastName: string;
  firstName: string;
  email: string;
  departmentId: string;
};

type TeacherPayload = {
  lastName: string;
  firstName: string;
  email: string;
  departmentId?: number;
  role: "TEACHER";
};

export const useTeacherCrud = makeEntityCrudHook<Teacher, TeacherFormData, TeacherPayload>({
  schema: teacherSchema,
  defaultForm: { lastName: "", firstName: "", email: "", departmentId: "" },
  useCreate: useCreateUser,
  useUpdate: useUpdateUser,
  useDelete: useDeleteUser,
  toForm: (entity) => ({
    lastName: entity.lastName,
    firstName: entity.firstName,
    email: entity.email,
    departmentId: entity.departmentId != null ? String(entity.departmentId) : "",
  }),
  toPayload: (form) => ({
    lastName: form.lastName,
    firstName: form.firstName,
    email: form.email,
    departmentId: form.departmentId ? Number(form.departmentId) : undefined,
    role: "TEACHER" as const,
  }),
  messages: {
    created: "Enseignant créé avec succès",
    updated: "Enseignant modifié avec succès",
    deleted: "Enseignant supprimé",
  },
});
