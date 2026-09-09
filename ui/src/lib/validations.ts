import { z } from "zod";
import { MIN_PASSWORD_LENGTH, MAX_SCORE } from "@/lib/constants";

type Errors<T> = Partial<Record<keyof T, string>>;

export function validate<T>(schema: z.ZodType<T>, data: T): Errors<T> | null {
  const result = schema.safeParse(data);
  if (result.success) return null;
  const errors: Errors<T> = {};
  for (const issue of result.error.issues) {
    const path = issue.path.join(".") as keyof T;
    if (path && !errors[path]) {
      errors[path] = issue.message;
    }
  }
  return Object.keys(errors).length > 0 ? errors : null;
}

// --- Auth ---

export const loginSchema = z.object({
  email: z.string().min(1, "L'email est requis").email("Email invalide"),
  password: z.string().min(1, "Le mot de passe est requis"),
});

export const verifyAccountSchema = z
  .object({
    password: z
      .string()
      .min(MIN_PASSWORD_LENGTH, "Le mot de passe doit contenir au moins 8 caractères"),
    confirmPassword: z.string().min(1, "Confirmez le mot de passe"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Les mots de passe ne correspondent pas",
    path: ["confirmPassword"],
  });

export const forgotPasswordSchema = z.object({
  email: z.string().min(1, "L'email est requis").email("Email invalide"),
});

export const resetPasswordSchema = z
  .object({
    password: z
      .string()
      .min(MIN_PASSWORD_LENGTH, "Le mot de passe doit contenir au moins 8 caractères"),
    confirmPassword: z.string().min(1, "Confirmez le mot de passe"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Les mots de passe ne correspondent pas",
    path: ["confirmPassword"],
  });

// --- Profile ---

export const profileEditSchema = z.object({
  firstName: z.string().min(1, "Le prénom est requis"),
  lastName: z.string().min(1, "Le nom est requis"),
});

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "Le mot de passe actuel est requis"),
    newPassword: z
      .string()
      .min(MIN_PASSWORD_LENGTH, "Le mot de passe doit contenir au moins 8 caractères"),
    confirmPassword: z.string().min(1, "Confirmez le mot de passe"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Les mots de passe ne correspondent pas",
    path: ["confirmPassword"],
  });

// --- Coordinator: Defense Sessions ---

export const defenseSessionSchema = z
  .object({
    name: z.string().min(1, "Le nom est requis"),
    defenseType: z.enum(["pfe", "memoire", "these"]).default("pfe"),
    status: z.enum(["draft", "active", "scheduled", "completed", "archived"]),
    maxGroupSize: z.coerce.number().min(1, "Doit être au moins 1").max(10, "Ne peut pas dépasser 10"),
    defenseDuration: z.coerce.number().min(5, "Doit être au moins 5 min").max(180, "Ne peut pas dépasser 180 min"),
    breakDuration: z.coerce.number().min(0, "Ne peut pas être négative"),
    submissionDeadline: z.string().min(1, "La date limite est requise"),
    juryRoleTemplateId: z.string().min(1, "Le template de jury est requis"),
    startDate: z.string().min(1, "La date de début est requise"),
    endDate: z.string().min(1, "La date de fin est requise"),
    evaluationCoefficients: z.record(z.string(), z.number()).default({}),
  })
  .refine((data) => !data.startDate || !data.endDate || data.startDate <= data.endDate, {
    message: "La date de fin doit être postérieure à la date de début",
    path: ["endDate"],
  });

export const defenseSessionTransitionSchema = z.object({
  fromStatus: z.enum(["draft", "active", "scheduled", "completed", "archived"]),
  toStatus: z.enum(["draft", "active", "scheduled", "completed", "archived"]),
});

// --- Admin: Rooms ---

export const roomSchema = z.object({
  name: z.string().min(1, "Le nom est requis"),
  departmentId: z.string().min(1, "Le département est requis"),
  capacity: z.coerce.number().min(1, "La capacité doit être supérieure à 0"),
});

// --- Admin: Departments ---

export const departmentSchema = z.object({
  name: z.string().min(1, "Le nom est requis"),
  code: z.string().min(1, "Le code est requis"),
  headId: z.string().optional(),
});

// --- Admin: Users ---

export const userBaseSchema = z.object({
  lastName: z.string().min(1, "Le nom est requis"),
  firstName: z.string().min(1, "Le prénom est requis"),
  email: z.string().min(1, "L'email est requis").email("Email invalide"),
});

export const studentSchema = userBaseSchema.extend({
  cne: z.string().min(1, "Le CNE est requis"),
  majorId: z.string().min(1, "La major est requise"),
  levelId: z.string().min(1, "Le niveau est requis"),
});

export const teacherSchema = userBaseSchema.extend({
  departmentId: z.string().min(1, "Le département est requis"),
});

export const coordinatorSchema = userBaseSchema;

// --- Admin: Configuration ---

export const configNameSchema = z.object({
  name: z.string().min(1, "Le nom est requis"),
});

export const majorSchema = z.object({
  name: z.string().min(1, "Le nom est requis"),
  departmentId: z.string().optional(),
});

export const defenseSettingsSchema = z
  .object({
    startTime: z.string().min(1, "L'heure de début est requise"),
    endTime: z.string().min(1, "L'heure de fin est requise"),
    defenseDuration: z.coerce
      .number()
      .min(1, "Doit être supérieure à 0")
      .max(180, "Ne peut pas dépasser 180 minutes"),
    breakDuration: z.coerce.number().min(0, "Ne peut pas être négative"),
    groupCreationStartDate: z
      .string()
      .min(1, "La date de début est requise"),
    groupCreationEndDate: z.string().min(1, "La date de fin est requise"),
  })
  .refine(
    (data) =>
      !data.groupCreationStartDate ||
      !data.groupCreationEndDate ||
      data.groupCreationStartDate <= data.groupCreationEndDate,
    {
      message:
        "La date de fin doit être postérieure à la date de début",
      path: ["groupCreationEndDate"],
    },
  )
  .refine(
    (data) => data.startTime < data.endTime,
    {
      message: "L'heure de début doit être antérieure à l'heure de fin",
      path: ["endTime"],
    },
  );

// --- Admin: General Settings ---

export const generalSettingsSchema = z.object({
  institutionName: z.string().min(1, "Le nom de l'établissement est requis"),
  institutionLogoUrl: z.string().optional(),
  timezone: z.string().min(1, "Le fuseau horaire est requis"),
  dateFormat: z.string().min(1, "Le format de date est requis"),
  setupCompleted: z.boolean().optional(),
});

// --- Admin: Document Config ---

export const documentConfigSchema = z.object({
  maxFileSizeMb: z.coerce.number().min(1, "Doit être au moins 1 Mo").max(500, "Ne peut pas dépasser 500 Mo"),
  allowedExtensions: z.string().min(1, "Les extensions autorisées sont requises"),
  versionLimit: z.coerce.number().min(1, "Doit être au moins 1").max(50, "Ne peut pas dépasser 50"),
});

// --- Teacher: Evaluations ---

export const evaluationSchema = z.object({
  score: z.coerce
    .number()
    .min(0, `La note doit être entre 0 et ${MAX_SCORE}`)
    .max(MAX_SCORE, `La note doit être entre 0 et ${MAX_SCORE}`),
  comment: z.string().optional(),
});

// --- Coordinator: Projects ---

export const projectSchema = z.object({
  title: z.string().min(1, "Le titre est requis"),
  description: z.string().optional(),
  supervisorId: z.string().min(1, "L'encadrant est requis"),
  studentIds: z.array(z.string()).min(1, "Au moins un étudiant est requis"),
  defenseType: z.enum(["pfe", "memoire", "these"]).default("pfe"),
});

// --- Coordinator: Jury ---

export const juryMemberSchema = z.object({
  roleName: z.string().min(1),
  teacherId: z.string().min(1, "Veuillez sélectionner un enseignant"),
});

export const jurySchema = z.object({
  projectId: z.string().min(1, "Le projet est requis"),
  templateId: z.string().min(1, "Le modèle de jury est requis"),
  members: z
    .array(juryMemberSchema)
    .min(1, "Ajoutez au moins un membre")
    .refine(
      (members) => {
        const ids = members.map((m) => m.teacherId);
        return new Set(ids).size === ids.length;
      },
      { message: "Les membres du jury doivent être des personnes différentes" },
    ),
});


// --- Teacher: Unavailability ---

export const unavailabilitySchema = z.object({
  slotsByDate: z.record(z.string(), z.array(z.string())),
});
