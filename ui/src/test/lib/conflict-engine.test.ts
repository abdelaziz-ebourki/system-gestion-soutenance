import { describe, it, expect } from "vitest";
import { validateSlotAssignment, getAllConflicts, getSmartSuggestions, type ConflictContext } from "@/lib/conflict-engine";

describe("Conflict Engine", () => {
  const mockContext: ConflictContext = {
    schedule: {},
    rooms: {
      "1": { id: 1, name: "Salle A" },
      "2": { id: 2, name: "Salle B" },
    },
    groups: {
      "1": { id: 1, studentIds: [1, 2] },
    },
    projects: {
      "1": { id: 1, studentIds: [1, 2], supervisorId: 10 },
      "2": { id: 2, studentIds: [3, 4, 5], supervisorId: 10 },
      "3": { id: 3, studentIds: [6, 7, 8, 9, 10, 11], supervisorId: 20 },
    },
    teachers: {
      "1": { id: 1, name: "Teacher 1" },
      "2": { id: 2, name: "Teacher 2" },
      "3": { id: 3, name: "Teacher 3" },
    },
    juries: {
      "1": { id: 1, projectId: 1, teacherIds: [1, 2] },
      "2": { id: 2, projectId: 2, teacherIds: [2, 3] },
      "3": { id: 3, projectId: 3, teacherIds: [1, 3] },
    },
    juriesByProjectId: {
      "1": { id: 1, projectId: 1, teacherIds: [1, 2] },
      "2": { id: 2, projectId: 2, teacherIds: [2, 3] },
      "3": { id: 3, projectId: 3, teacherIds: [1, 3] },
    },
    unavailability: {
      "1": [{ date: "2026-06-01", slots: ["09:00", "10:00"], teacherId: 1 }],
    },
    unavailabilitySet: new Set(["1|2026-06-01|09:00", "1|2026-06-01|10:00"]),
    defenseSession: {
      startDate: "2026-06-01",
      endDate: "2026-06-14",
      breakDuration: 15,
    },
  };

  describe("getSmartSuggestions", () => {
    it("should suggest other rooms when slot is occupied", () => {
      const context: ConflictContext = {
        ...mockContext,
        schedule: { "2026-06-01|1|11:00": { id: 2, title: "P2", date: "2026-06-01", time: "11:00", roomId: 1 } },
        allTimeSlots: ["09:00", "11:00"],
      };
      const suggestion = getSmartSuggestions(1, "2026-06-01", 1, "11:00", context, "slot_occupied");
      expect(suggestion).toContain("Salle B");
    });

    it("should suggest other times if no rooms are available", () => {
        const context: ConflictContext = {
            ...mockContext,
            unavailability: {},
            unavailabilitySet: new Set(),
            schedule: { 
                "2026-06-01|1|11:00": { id: 2, title: "P2", date: "2026-06-01", time: "11:00", roomId: 1 },
                "2026-06-01|2|11:00": { id: 3, title: "P3", date: "2026-06-01", time: "11:00", roomId: 2 } 
            },
            allTimeSlots: ["09:00", "11:00"],
        };
        const suggestion = getSmartSuggestions(1, "2026-06-01", 1, "11:00", context, "slot_occupied");
        expect(suggestion).toBeDefined();
        expect(suggestion).toContain("09:00");
    });
  });

  describe("validateSlotAssignment", () => {
    it("should return error for invalid slot format", () => {
      const result = validateSlotAssignment(1, "invalid-slot", mockContext);
      expect(result.isValid).toBe(false);
      expect(result.issues[0].type).toBe("slot_occupied");
      expect(result.issues[0].message).toContain("Format de créneau invalide");
    });

    it("should return error if project is already scheduled", () => {
      const context = {
        ...mockContext,
        schedule: { "2026-06-01|1|09:00": { id: 1, title: "P1", date: "2026-06-01", time: "09:00", roomId: 1 } },
      };
      const result = validateSlotAssignment(1, "2026-06-02|1|09:00", context);
      expect(result.isValid).toBe(false);
      expect(result.issues.some(i => i.type === "project_already_scheduled")).toBe(true);
    });

    it("should return error if slot is already occupied", () => {
      const context = {
        ...mockContext,
        schedule: { "2026-06-01|1|09:00": { id: 2, title: "P2", date: "2026-06-01", time: "09:00", roomId: 1 } },
      };
      const result = validateSlotAssignment(1, "2026-06-01|1|09:00", context);
      expect(result.isValid).toBe(false);
      expect(result.issues.some(i => i.type === "slot_occupied")).toBe(true);
    });

    it("should return error if date is out of session boundaries", () => {
      const resultBefore = validateSlotAssignment(1, "2026-05-31|1|09:00", mockContext);
      const resultAfter = validateSlotAssignment(1, "2026-06-15|1|09:00", mockContext);
      
      expect(resultBefore.isValid).toBe(false);
      expect(resultBefore.issues.some(i => i.type === "out_of_bounds")).toBe(true);
      expect(resultAfter.isValid).toBe(false);
      expect(resultAfter.issues.some(i => i.type === "out_of_bounds")).toBe(true);
    });

    it("should return error if teacher is double-booked on the same date", () => {
      const context = {
        ...mockContext,
        schedule: { "2026-06-01|1|11:00": { id: 2, title: "Proj 2", date: "2026-06-01", time: "11:00", roomId: 1 } },
      };
      const result = validateSlotAssignment(1, "2026-06-01|2|09:00", context);
      expect(result.isValid).toBe(false);
      expect(result.issues.some(i => i.type === "teacher_double_booked")).toBe(true);
      expect(result.issues.find(i => i.type === "teacher_double_booked")?.message).toContain("Teacher 2");
    });

    it("should return error if supervisor has another project on the same date", () => {
      const context = {
        ...mockContext,
        schedule: { "2026-06-01|1|11:00": { id: 2, title: "Proj 2", date: "2026-06-01", time: "11:00", roomId: 1 } },
      };
      const result = validateSlotAssignment(1, "2026-06-01|2|09:00", context);
      expect(result.issues.some(i => i.type === "supervisor_conflict")).toBe(true);
      expect(result.issues.find(i => i.type === "supervisor_conflict")?.severity).toBe("warning");
    });

    it("should return error if teacher is unavailable for the slot", () => {
      const result = validateSlotAssignment(1, "2026-06-01|1|09:00", mockContext);
      expect(result.isValid).toBe(false);
      expect(result.issues.some(i => i.type === "teacher_unavailable")).toBe(true);
      expect(result.issues.find(i => i.type === "teacher_unavailable")?.message).toContain("Teacher 1");
    });

    it("should be valid when no conflicts exist", () => {
      const result = validateSlotAssignment(1, "2026-06-02|1|09:00", mockContext);
      expect(result.isValid).toBe(true);
      expect(result.issues).toHaveLength(0);
    });

    it("should handle missing optional context data gracefully", () => {
      const minimalContext: ConflictContext = {
        schedule: {},
        rooms: {},
        groups: {},
        projects: { "1": { id: 1, studentIds: [], supervisorId: 100 } },
        teachers: {},
        juries: {},
        juriesByProjectId: {},
        unavailability: {},
        unavailabilitySet: new Set(),
      };
      const result = validateSlotAssignment(1, "2026-06-01|100|09:00", minimalContext);
      expect(result.isValid).toBe(true);
    });

    it("should handle missing juries context", () => {
      const context = { ...mockContext, juries: {}, juriesByProjectId: {} };
      const result = validateSlotAssignment(1, "2026-06-01|1|09:00", context);
      expect(result.isValid).toBe(true);
    });

    it("should handle missing teacher names in unavailability message", () => {
      const context = {
        ...mockContext,
        teachers: {},
      };
      const result = validateSlotAssignment(1, "2026-06-01|1|09:00", context);
      expect(result.issues.find(i => i.type === "teacher_unavailable")?.message).toContain("Un enseignant");
    });

    it("should handle missing teacher names in double booking message", () => {
      const context = {
        ...mockContext,
        teachers: {}, 
        schedule: { "2026-06-01|1|11:00": { id: 2, title: "P2", date: "2026-06-01", time: "11:00", roomId: 1 } },
      };
      const result = validateSlotAssignment(1, "2026-06-01|2|09:00", context);
      expect(result.issues.find(i => i.type === "teacher_double_booked")?.message).toContain("Un enseignant");
    });

    it("should handle existing projects without supervisors in supervisor conflict check", () => {
      const context = {
        ...mockContext,
        projects: {
          ...mockContext.projects,
          "98": { id: 98, studentIds: [99], supervisorId: 0 },
        },
        schedule: { "2026-06-01|1|09:00": { id: 98, title: "No Sup", date: "2026-06-01", time: "09:00", roomId: 1 } },
      };
      const result = validateSlotAssignment(1, "2026-06-01|2|11:00", context);
      expect(result.issues.some(i => i.type === "supervisor_conflict")).toBe(false);
    });

    it("should handle existing projects with no jury in double-booking check", () => {
       const context = {
        ...mockContext,
        juries: {
          "1": { id: 1, projectId: 1, teacherIds: [1] },
        },
        juriesByProjectId: {
          "1": { id: 1, projectId: 1, teacherIds: [1] },
        },
        schedule: { "2026-06-01|1|09:00": { id: 2, title: "P2", date: "2026-06-01", time: "09:00", roomId: 1 } },
      };
      const result = validateSlotAssignment(1, "2026-06-01|2|11:00", context);
      expect(result.issues.some(i => i.type === "teacher_double_booked")).toBe(false);
    });

    it("should handle missing unavailability context", () => {
      const context = { ...mockContext, unavailability: {}, unavailabilitySet: new Set<string>() };
      const result = validateSlotAssignment(1, "2026-06-01|1|09:00", context);
      expect(result.isValid).toBe(true);
    });

    it("should provide smart suggestions for occupied slots", () => {
      const context: ConflictContext = {
        ...mockContext,
        schedule: { "2026-06-01|1|11:00": { id: 2, title: "P2", date: "2026-06-01", time: "11:00", roomId: 1 } },
        allTimeSlots: ["09:00", "10:00", "11:00"],
      };
      const result = validateSlotAssignment(1, "2026-06-01|1|11:00", context);
      expect(result.issues.some(i => i.type === "slot_occupied")).toBe(true);
      const issue = result.issues.find(i => i.type === "slot_occupied");
      expect(issue?.suggestedResolution).toContain("Essayez les salles libres : Salle B");
    });

    it("should provide smart suggestions for teacher double booking", () => {
        const context: ConflictContext = {
            ...mockContext,
            schedule: { "2026-06-01|1|09:00": { id: 2, title: "P2", date: "2026-06-01", time: "09:00", roomId: 1 } },
            allTimeSlots: ["09:00", "11:00"],
        };
        const result = validateSlotAssignment(1, "2026-06-01|2|09:00", context);
        const issue = result.issues.find(i => i.type === "teacher_double_booked");
        expect(issue?.suggestedResolution).toContain("Le jury est disponible à : 11:00");
    });

    it("should provide smart suggestions for teacher unavailability", () => {
        const context: ConflictContext = {
            ...mockContext,
            allTimeSlots: ["09:00", "11:00"],
        };
        const result = validateSlotAssignment(1, "2026-06-01|1|09:00", context);
        const issue = result.issues.find(i => i.type === "teacher_unavailable");
        expect(issue?.suggestedResolution).toContain("Le jury est disponible à : 11:00");
    });

  });

  describe("getAllConflicts", () => {
    it("should aggregate all conflicts from the schedule", () => {
      const realSchedule = {
        "2026-06-01|1|09:00": { id: 1, title: "P1", date: "2026-06-01", time: "09:00", roomId: 1 },
        "2026-06-01|1|09:10": { id: 2, title: "P2", date: "2026-06-01", time: "09:10", roomId: 1 },
      };
      
      const contextWithSchedule = {
        ...mockContext,
        schedule: realSchedule,
      };

      const conflicts = getAllConflicts(realSchedule, contextWithSchedule);
      expect(conflicts.length).toBeGreaterThan(0);
    });
  });
});
