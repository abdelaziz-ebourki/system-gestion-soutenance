import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as api from "@/lib/api";
import { CONFIG_STALE_TIME } from "@/lib/constants";
import type { CreateProjectPayload, UpdateProjectPayload, CreateJuryPayload, UpdateJuryPayload, ScheduleSlot } from "@/lib/api-coordinator";
import { MAX_STUDENT_FETCH_LIMIT } from "@/lib/constants";

export function useCoordinatorStats() {
  return useQuery({
    queryKey: ["coordinator", "stats"],
    queryFn: api.getCoordinatorStats,
  });
}

export function useProjects() {
  return useQuery({ queryKey: ["projects"], queryFn: api.getProjects });
}

export function useCreateProject() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateProjectPayload) => api.createProject(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["projects"], refetchType: "active" }),
  });
}

export function useUpdateProject() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateProjectPayload }) =>
      api.updateProject(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["projects"], refetchType: "active" }),
  });
}

export function useDeleteProject() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteProject(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["projects"], refetchType: "active" }),
  });
}

export function useJuries() {
  return useQuery({
    queryKey: ["juries"],
    queryFn: api.getJuries,
    staleTime: 30_000,
  });
}

export function useCreateJury() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateJuryPayload) => api.createJury(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["juries"], refetchType: "active" }),
  });
}

export function useUpdateJury() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateJuryPayload }) =>
      api.updateJury(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["juries"], refetchType: "active" }),
  });
}

export function useDeleteJury() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteJury(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["juries"], refetchType: "active" }),
  });
}

export function useGroups() {
  return useQuery({ queryKey: ["groups"], queryFn: api.getGroups });
}

export function useCreateGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { groupName: string; projectId: number; studentIds: number[]; sessionId?: number; leaderId?: number }) =>
      api.createGroup(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["groups"], refetchType: "active" }),
  });
}

export function useDeleteGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteGroup(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["groups"], refetchType: "active" }),
  });
}

export function useAssignProjectToGroup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { projectId: number; groupId: number }) => api.assignProjectToGroup(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["groups"], refetchType: "active" });
      qc.invalidateQueries({ queryKey: ["projects"], refetchType: "active" });
    },
  });
}

export function useCoordinatorDefenseSessions() {
  return useQuery({
    queryKey: ["coordinator", "defense-sessions"],
    queryFn: api.getCoordinatorDefenseSessions,
  });
}

export function useCreateDefenseSession() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: {
      name: string;
      defenseType: string;
      status: string;
      maxGroupSize: number;
      defenseDuration: number;
      breakDuration: number;
      submissionDeadline: string;
      juryRoleTemplateId: number;
      startDate: string;
      endDate: string;
      evaluationCoefficients: Record<string, number>;
    }) => api.createCoordinatorDefenseSession(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["coordinator", "defense-sessions"], refetchType: "active" }),
  });
}

export function useUpdateDefenseSession() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: {
      id: number;
      data: {
        name: string;
        defenseType: string;
        status: string;
        maxGroupSize: number;
        defenseDuration: number;
        breakDuration: number;
        submissionDeadline: string;
        juryRoleTemplateId: number;
        startDate: string;
        endDate: string;
        evaluationCoefficients: Record<string, number>;
      };
    }) => api.updateCoordinatorDefenseSession(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["coordinator", "defense-sessions"], refetchType: "active" }),
  });
}

export function useDeleteDefenseSession() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteCoordinatorDefenseSession(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["coordinator", "defense-sessions"], refetchType: "active" }),
  });
}

export function useTransitionDefenseSession() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, toStatus }: { id: number; toStatus: string }) =>
      api.transitionDefenseSession(id, toStatus),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["coordinator", "defense-sessions"], refetchType: "active" });
    },
  });
}

export function useSchedules() {
  return useQuery({
    queryKey: ["coordinator", "schedules"],
    queryFn: api.getSchedules,
  });
}

export function useSaveSchedules() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ defenseSessionId, slots }: { defenseSessionId: number; slots: ScheduleSlot[] }) =>
      api.saveSchedules(defenseSessionId, slots),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["coordinator", "stats"], refetchType: "active" });
      qc.invalidateQueries({ queryKey: ["coordinator", "schedules"], refetchType: "active" });
    },
  });
}

export function useAutoGenerateSchedules() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (defenseSessionId: number) => api.autoGenerateSchedules(defenseSessionId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["coordinator", "stats"], refetchType: "active" });
      qc.invalidateQueries({ queryKey: ["coordinator", "schedules"], refetchType: "active" });
    },
  });
}

export function usePublishSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (defenseSessionId: number) => api.publishSchedule(defenseSessionId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["coordinator", "defense-sessions"], refetchType: "active" });
      qc.invalidateQueries({ queryKey: ["coordinator", "schedules"], refetchType: "active" });
    },
  });
}

export function useCancelDefense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.cancelDefense(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["coordinator", "schedules"], refetchType: "active" });
    },
  });
}

export function useValidateConflicts() {
  return useMutation({
    mutationFn: ({ defenseSessionId, schedule }: { defenseSessionId: number; schedule: ScheduleSlot[] }) =>
      api.validateConflicts(defenseSessionId, schedule),
  });
}

export function useCoordinatorUnavailability() {
  return useQuery({
    queryKey: ["coordinator", "unavailability"],
    queryFn: api.getCoordinatorUnavailability,
  });
}

export function useProjectGrades() {
  return useQuery({
    queryKey: ["coordinator", "grades"],
    queryFn: () => api.getCoordinatorGrades(),
  });
}

export function useCoordinatorTeachersList() {
  return useQuery({
    queryKey: ["coordinator", "teachers"],
    queryFn: () => api.getCoordinatorUsers("TEACHER"),
    staleTime: CONFIG_STALE_TIME,
  });
}

export function useCoordinatorStudents(limit = MAX_STUDENT_FETCH_LIMIT) {
  return useQuery({
    queryKey: ["coordinator", "students", limit],
    queryFn: () => api.getCoordinatorUsers("STUDENT", 0, limit),
    staleTime: CONFIG_STALE_TIME,
  });
}
