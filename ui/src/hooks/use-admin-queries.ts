import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as api from "@/lib/api";
import { CONFIG_STALE_TIME, AUDIT_LOG_PAGE_SIZE, DEFAULT_API_LIMIT } from "@/lib/constants";

export function useAdminStats() {
  return useQuery({ queryKey: ["admin", "stats"], queryFn: api.getAdminStats });
}

export function useAuditLogs(page = 0, limit = AUDIT_LOG_PAGE_SIZE) {
  return useQuery({
    queryKey: ["admin", "audit-logs", page, limit],
    queryFn: () => api.getAuditLogs(page, limit),
  });
}

export function useRooms(page = 0, limit = DEFAULT_API_LIMIT) {
  return useQuery({
    queryKey: ["rooms", page, limit],
    queryFn: () => api.getRooms(page, limit),
    staleTime: CONFIG_STALE_TIME,
  });
}

export function useCreateRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string; capacity: number; departmentId: number }) => api.createRoom(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["rooms"], refetchType: "active" }),
  });
}

export function useUpdateRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name: string; capacity: number; departmentId: number } }) =>
      api.updateRoom(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["rooms"], refetchType: "active" }),
  });
}

export function useDeleteRoom() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteRoom(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["rooms"], refetchType: "active" }),
  });
}

export function useDepartments() {
  return useQuery({ queryKey: ["departments"], queryFn: api.getDepartments, staleTime: CONFIG_STALE_TIME });
}

export function useCreateDepartment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string; code: string; headId?: number; facultyId?: number }) => api.createDepartment(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["departments"], refetchType: "active" }),
  });
}

export function useUpdateDepartment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name: string; code: string; headId?: number; facultyId?: number } }) =>
      api.updateDepartment(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["departments"], refetchType: "active" }),
  });
}

export function useDeleteDepartment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteDepartment(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["departments"], refetchType: "active" }),
  });
}

export function useFaculties() {
  return useQuery({ queryKey: ["faculties"], queryFn: api.getFaculties, staleTime: CONFIG_STALE_TIME });
}

export function useCreateFaculty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string; code: string; deanId?: number; logoUrl?: string }) => api.createFaculty(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["faculties"], refetchType: "active" }),
  });
}

export function useUpdateFaculty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name: string; code: string; deanId?: number; logoUrl?: string } }) =>
      api.updateFaculty(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["faculties"], refetchType: "active" }),
  });
}

export function useDeleteFaculty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteFaculty(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["faculties"], refetchType: "active" }),
  });
}

export function useMajors() {
  return useQuery({ queryKey: ["majors"], queryFn: api.getMajors, staleTime: CONFIG_STALE_TIME });
}

export function useCreateMajor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string; departmentId?: number }) => api.createMajor(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["majors"], refetchType: "active" }),
  });
}

export function useUpdateMajor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name: string; departmentId?: number } }) =>
      api.updateMajor(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["majors"], refetchType: "active" }),
  });
}

export function useDeleteMajor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteMajor(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["majors"], refetchType: "active" }),
  });
}

export function useLevels() {
  return useQuery({ queryKey: ["levels"], queryFn: api.getLevels, staleTime: CONFIG_STALE_TIME });
}

export function useCreateLevel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string }) => api.createLevel(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["levels"], refetchType: "active" }),
  });
}

export function useUpdateLevel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name: string } }) =>
      api.updateLevel(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["levels"], refetchType: "active" }),
  });
}

export function useDeleteLevel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteLevel(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["levels"], refetchType: "active" }),
  });
}

export function useTeacherRanks() {
  return useQuery({ queryKey: ["teacher-ranks"], queryFn: api.getTeacherRanks, staleTime: CONFIG_STALE_TIME });
}

export function useCreateTeacherRank() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: { name: string }) => api.createTeacherRank(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["teacher-ranks"], refetchType: "active" }),
  });
}

export function useUpdateTeacherRank() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name: string } }) =>
      api.updateTeacherRank(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["teacher-ranks"], refetchType: "active" }),
  });
}

export function useDeleteTeacherRank() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteTeacherRank(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["teacher-ranks"], refetchType: "active" }),
  });
}

export function useJuryRoleTemplates() {
  return useQuery({ queryKey: ["jury-role-templates"], queryFn: api.getJuryRoleTemplates, staleTime: CONFIG_STALE_TIME });
}

export function useUsers(params: { role?: string; page?: number; limit?: number; search?: string }) {
  return useQuery({
    queryKey: ["users", params],
    queryFn: () => api.getUsers(params),
  });
}

export function useStudents(params?: { page?: number; limit?: number; search?: string }) {
  const { page = 0, limit = DEFAULT_API_LIMIT, search } = params ?? {};
  return useQuery({
    queryKey: ["users", "students", page, limit, search],
    queryFn: () => api.getUsers({ role: "STUDENT", page, limit, search }),
  });
}

export function useTeachers(params?: { page?: number; limit?: number; search?: string }) {
  const { page = 0, limit = DEFAULT_API_LIMIT, search } = params ?? {};
  return useQuery({
    queryKey: ["users", "teachers", page, limit, search],
    queryFn: () => api.getUsers({ role: "TEACHER", page, limit, search }),
  });
}

export function useCoordinators(params?: { page?: number; limit?: number; search?: string }) {
  const { page = 0, limit = DEFAULT_API_LIMIT, search } = params ?? {};
  return useQuery({
    queryKey: ["users", "coordinators", page, limit, search],
    queryFn: () => api.getUsers({ role: "COORDINATOR", page, limit, search }),
  });
}

export function useTeachersList() {
  return useQuery({
    queryKey: ["users", "teachers", "list"],
    queryFn: () => api.getTeachersList(),
    staleTime: CONFIG_STALE_TIME,
  });
}

export function useCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: {
      lastName: string;
      firstName: string;
      email: string;
      role: string;
      cne?: string;
      majorId?: number;
      levelId?: number;
      teacherRankId?: number;
      departmentId?: number;
    }) => api.createUser(data),
    onSuccess: (user) => {
      const roleKey = user.role === "student" ? "students" : user.role === "teacher" ? "teachers" : "coordinators";
      qc.invalidateQueries({ queryKey: ["users", roleKey], refetchType: "active" });
      qc.invalidateQueries({ queryKey: ["users"], refetchType: "active" });
    },
  });
}

export function useUpdateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: {
      id: number;
      data: {
        lastName: string;
        firstName: string;
        email: string;
        role: string;
        cne?: string;
        majorId?: number;
        levelId?: number;
        teacherRankId?: number;
        departmentId?: number;
      };
    }) => api.updateUser(id, data),
    onSuccess: (user) => {
      const roleKey = user.role === "student" ? "students" : user.role === "teacher" ? "teachers" : "coordinators";
      qc.invalidateQueries({ queryKey: ["users", roleKey], refetchType: "active" });
      qc.invalidateQueries({ queryKey: ["users"], refetchType: "active" });
    },
  });
}

export function useDeleteUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.deleteUser(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["users"], refetchType: "active" }),
  });
}

export function useDefenseSettings() {
  return useQuery({
    queryKey: ["admin", "config", "settings"],
    queryFn: api.getDefenseSettings,
    staleTime: CONFIG_STALE_TIME,
  });
}

export function useUpdateDefenseSettings() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: {
      startTime: string;
      endTime: string;
      defenseDuration: number;
      breakDuration: number;
      groupCreationStartDate: string;
      groupCreationEndDate: string;
    }) => api.updateDefenseSettings(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin", "config", "settings"], refetchType: "active" }),
  });
}

