import { describe, it, expect } from "vitest";
import { 
  validate, 
  loginSchema, 
  verifyAccountSchema, 
  defenseSessionSchema, 
  roomSchema, 
  jurySchema,
  departmentSchema,
  teacherSchema,
  configNameSchema,
  defenseSettingsSchema,
  generalSettingsSchema,
  documentConfigSchema,
  evaluationSchema, 
  projectSchema, 
  unavailabilitySchema,
  userBaseSchema,
  coordinatorSchema,
  juryMemberSchema
  } from "@/lib/validations";

describe("Validation utilities", () => {
  it("validate() returns null for valid data", () => {
    const data = { email: "test@test.com", password: "password123" };
    expect(validate(loginSchema, data)).toBeNull();
  });

  it("validate() returns errors for invalid data", () => {
    const data = { email: "invalid-email", password: "" };
    const errors = validate(loginSchema, data);
    expect(errors).toEqual({
      email: "Email invalide",
      password: "Le mot de passe est requis",
    });
  });

  describe("Auth schemas", () => {
    it("verifyAccountSchema validates matching passwords", () => {
      expect(validate(verifyAccountSchema, { password: "password123", confirmPassword: "password123" })).toBeNull();
      expect(validate(verifyAccountSchema, { password: "password123", confirmPassword: "different" })).toEqual({
        confirmPassword: "Les mots de passe ne correspondent pas",
      });
    });

    it("verifyAccountSchema enforces min length", () => {
      expect(validate(verifyAccountSchema, { password: "short", confirmPassword: "short" })).toEqual({
        password: "Le mot de passe doit contenir au moins 8 caractères",
      });
    });
  });

  describe("Coordinator schemas", () => {
    it("defenseSessionSchema validates boundaries", () => {
      const valid = {
        name: "Session PFE",
        defenseType: "pfe" as const,
        status: "draft" as const,
        maxGroupSize: 5,
        defenseDuration: 60,
        breakDuration: 15,
        submissionDeadline: "2026-06-01",
        juryRoleTemplateId: "t1",
        startDate: "2026-06-10",
        endDate: "2026-06-20",
        evaluationCoefficients: {},
      };
      
      // Boundaries
      expect(validate(defenseSessionSchema, { ...valid, maxGroupSize: 0 })).toBeDefined();
      expect(validate(defenseSessionSchema, { ...valid, maxGroupSize: 11 })).toBeDefined();
      expect(validate(defenseSessionSchema, { ...valid, defenseDuration: 4 })).toBeDefined();
      expect(validate(defenseSessionSchema, { ...valid, defenseDuration: 181 })).toBeDefined();
      expect(validate(defenseSessionSchema, { ...valid, breakDuration: -1 })).toBeDefined();

      // Refinement
      const invalidDates = { ...valid, startDate: "2026-06-20", endDate: "2026-06-10" };
      expect(validate(defenseSessionSchema, invalidDates)).toEqual({
        endDate: "La date de fin doit être postérieure à la date de début",
      });
    });

    it("jurySchema validates unique members", () => {
      const valid = {
        projectId: "1",
        templateId: "1",
        members: [{ roleName: "President", teacherId: "1" }, { roleName: "Member", teacherId: "2" }],
      };
      expect(validate(jurySchema, valid)).toBeNull();

      const duplicateMembers = {
        projectId: "1",
        templateId: "1",
        members: [{ roleName: "President", teacherId: "1" }, { roleName: "Member", teacherId: "1" }],
      };
      expect(validate(jurySchema, duplicateMembers)).toEqual({
        members: "Les membres du jury doivent être des personnes différentes",
      });
    });

    it("userBaseSchema validates fields", () => {
        expect(validate(userBaseSchema, { lastName: "Doe", firstName: "John", email: "john@doe.com" })).toBeNull();
        expect(validate(userBaseSchema, { lastName: "", firstName: "", email: "bad" })).toBeDefined();
    });

    it("coordinatorSchema validates as userBase", () => {
        expect(validate(coordinatorSchema, { lastName: "Doe", firstName: "John", email: "john@doe.com" })).toBeNull();
    });

    it("juryMemberSchema validates", () => {
        expect(validate(juryMemberSchema, { roleName: "President", teacherId: "t1" })).toBeNull();
        expect(validate(juryMemberSchema, { roleName: "", teacherId: "" })).toBeDefined();
    });

    it("projectSchema validates required fields", () => {
        const invalid = { title: "", supervisorId: "", studentIds: [], defenseType: "pfe" as const };
        const errors = validate(projectSchema, invalid);
        expect(errors?.title).toBeDefined();
        expect(errors?.supervisorId).toBeDefined();
    });
  });

  describe("Admin schemas", () => {
    it("roomSchema validates fields", () => {
      expect(validate(roomSchema, { name: "", departmentId: "", capacity: 0 })).toEqual({
        name: "Le nom est requis",
        departmentId: "Le département est requis",
        capacity: "La capacité doit être supérieure à 0",
      });
    });

    it("departmentSchema validates required fields", () => {
        expect(validate(departmentSchema, { name: "", code: "" })).toEqual({
            name: "Le nom est requis",
            code: "Le code est requis",
        });
    });

    it("teacherSchema validates required fields", () => {
        expect(validate(teacherSchema, { lastName: "", firstName: "", email: "bad", departmentId: "" })).toEqual({
            lastName: "Le nom est requis",
            firstName: "Le prénom est requis",
            email: "Email invalide",
            departmentId: "Le département est requis",
        });
    });

    it("configNameSchema validates", () => {
        expect(validate(configNameSchema, { name: "" })).toBeDefined();
    });

    it("generalSettingsSchema validates", () => {
        expect(validate(generalSettingsSchema, { institutionName: "", timezone: "", dateFormat: "" })).toBeDefined();
    });

    it("documentConfigSchema validates boundaries", () => {
        const valid = { maxFileSizeMb: 10, allowedExtensions: ".pdf", versionLimit: 5 };
        expect(validate(documentConfigSchema, { ...valid, maxFileSizeMb: 0 })).toBeDefined();
        expect(validate(documentConfigSchema, { ...valid, maxFileSizeMb: 501 })).toBeDefined();
        expect(validate(documentConfigSchema, { ...valid, versionLimit: 0 })).toBeDefined();
        expect(validate(documentConfigSchema, { ...valid, versionLimit: 51 })).toBeDefined();
    });
  });

  describe("Other schemas", () => {
      it("evaluationSchema validates score range", () => {
          expect(validate(evaluationSchema, { score: -1 })).toBeDefined();
          expect(validate(evaluationSchema, { score: 21 })).toBeDefined();
          expect(validate(evaluationSchema, { score: 15 })).toBeNull();
      });

      it("unavailabilitySchema validates", () => {
          expect(validate(unavailabilitySchema, { slotsByDate: { "2026-06-01": ["09:00"] } })).toBeNull();
      });

      it("defenseSettingsSchema validates date order", () => {
          const valid = {
              startTime: "08:00",
              endTime: "18:00",
              defenseDuration: 30,
              breakDuration: 10,
              groupCreationStartDate: "2026-01-01",
              groupCreationEndDate: "2026-02-01",
          };
          expect(validate(defenseSettingsSchema, valid)).toBeNull();
          expect(validate(defenseSettingsSchema, { ...valid, groupCreationStartDate: "2026-02-01", groupCreationEndDate: "2026-01-01" })).toBeDefined();
      });
  });
});
