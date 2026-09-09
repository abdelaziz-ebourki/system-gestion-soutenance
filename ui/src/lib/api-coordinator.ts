import { api } from "./api-core";
import type { Project, Group, Jury, DefenseSession, User, PaginatedResponse, JuryRoleTemplate } from "@/types";

export interface CoordinatorStats {
  totalProjects: number;
  totalGroups: number;
  totalJuries: number;
  scheduledDefenses: number;
}

export interface CreateProjectPayload {
  title: string;
  description: string;
  supervisorId: number;
  defenseType: string;
  studentIds?: number[];
}

export interface UpdateProjectPayload {
  title: string;
  description: string;
  defenseType: string;
}

export interface MemberEntry {
  teacherId: number;
  roleName: string;
}

export interface CreateJuryPayload {
  projectId: number;
  members: MemberEntry[];
}

export interface UpdateJuryPayload {
  projectId: number;
  members: MemberEntry[];
}

export interface ScheduleSlot {
  title: string;
  date: string;
  time: string;
  projectId: number;
  roomId: number;
}

export interface ScheduleResponse {
  id: number;
  title: string;
  date: string;
  time: string;
  projectId: number;
  roomId: number;
  roomName: string;
  projectTitle: string;
  studentNames: string[];
  role: string;
  status: string;
}

export interface ConflictDetail {
  type: string;
  severity: string;
  message: string;
  slot: string;
  suggestedResolution: string;
}

export interface GradeWeightedAverageResponse {
  projectId: number;
  projectTitle: string;
  defenseDate: string;
  status: string;
  finalScore: number;
  evaluationCoefficients: Record<string, number>;
  individualScores: Array<{ roleName: string; teacherName: string; score: number }>;
}

export interface UnavailabilityEntry {
  id: number;
  teacherId: number;
  date: string;
  slots: string[];
}

export const getCoordinatorStats = () =>
  api<CoordinatorStats>("/coordinator/stats");

export const getProjects = () => api<PaginatedResponse<Project>>("/coordinator/projects");
export const createProject = (data: CreateProjectPayload) =>
  api<Project>("/coordinator/projects", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateProject = (id: number, data: UpdateProjectPayload) =>
  api<Project>(`/coordinator/projects/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteProject = (id: number) =>
  api<void>(`/coordinator/projects/${id}`, { method: "DELETE" });

export interface BulkProjectPayload {
  title: string;
  description: string;
  supervisorEmail: string;
  defenseType: string;
}

export interface BulkImportResult {
  total: number;
  imported: number;
  created: Array<{ id: number; title: string }>;
  errors: Array<{ line: number; message: string }>;
}

export const bulkCreateProjects = (projects: BulkProjectPayload[]) =>
  api<BulkImportResult>("/coordinator/projects/bulk", {
    method: "POST",
    body: JSON.stringify({ projects }),
  });

export const getGroups = () => api<PaginatedResponse<Group>>("/coordinator/groups");
export const createGroup = (data: { groupName: string; projectId: number; studentIds: number[]; sessionId?: number; leaderId?: number }) =>
  api<Group>("/coordinator/groups", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const deleteGroup = (id: number) =>
  api<void>(`/coordinator/groups/${id}`, { method: "DELETE" });
// TODO: backend does not have POST /coordinator/groups/assign yet.
// Track: https://github.com/abdelaziz-ebourki/system-gestion-soutenance-api/issues
// Once implemented, the endpoint should accept { projectId, groupId } to assign a project to an existing group.
export const assignProjectToGroup = (data: { projectId: number; groupId: number }) =>
  api<Group>("/coordinator/groups/assign", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getJuries = () => api<PaginatedResponse<Jury>>("/coordinator/juries");
export const createJury = (data: CreateJuryPayload) =>
  api<Jury>("/coordinator/juries", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateJury = (id: number, data: UpdateJuryPayload) =>
  api<Jury>(`/coordinator/juries/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteJury = (id: number) =>
  api<void>(`/coordinator/juries/${id}`, { method: "DELETE" });

export const getCoordinatorDefenseSessions = () =>
  api<PaginatedResponse<DefenseSession>>("/coordinator/defense-sessions");

export const createCoordinatorDefenseSession = (data: {
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
}) =>
  api<DefenseSession>("/coordinator/defense-sessions", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateCoordinatorDefenseSession = (id: number, data: {
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
}) =>
  api<DefenseSession>(`/coordinator/defense-sessions/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteCoordinatorDefenseSession = (id: number) =>
  api<void>(`/coordinator/defense-sessions/${id}`, { method: "DELETE" });

export const transitionDefenseSession = (id: number, toStatus: string) =>
  api<DefenseSession>(`/coordinator/defense-sessions/${id}/transition`, {
    method: "POST",
    body: JSON.stringify({ toStatus }),
  });

export const getSchedules = () =>
  api<ScheduleResponse[]>("/coordinator/schedules");

export const saveSchedules = (defenseSessionId: number, slots: ScheduleSlot[]) =>
  api<ScheduleResponse[]>("/coordinator/schedules", {
    method: "POST",
    body: JSON.stringify({ defenseSessionId, slots }),
  });

export const autoGenerateSchedules = (defenseSessionId: number) =>
  api<ScheduleResponse[]>("/coordinator/schedules/generation", {
    method: "POST",
    body: JSON.stringify({ defenseSessionId }),
  });

export const publishSchedule = (defenseSessionId: number) =>
  api<void>("/coordinator/schedules/publication", {
    method: "PATCH",
    body: JSON.stringify({ defenseSessionId }),
  });

export const cancelDefense = (id: number) =>
  api<void>(`/coordinator/defenses/${id}/cancel`, { method: "POST" });

export const validateConflicts = (defenseSessionId: number, schedule: ScheduleSlot[]) =>
  api<ConflictDetail[]>("/coordinator/conflicts/validate", {
    method: "POST",
    body: JSON.stringify({ defenseSessionId, schedule }),
  });

export const getCoordinatorJuryRoleTemplates = () =>
  api<PaginatedResponse<JuryRoleTemplate>>("/coordinator/config/jury-role-templates");

export const getCoordinatorUnavailability = () =>
  api<UnavailabilityEntry[]>("/coordinator/unavailability");

export const getCoordinatorUsers = (role: string, page = 0, limit = 5000, search?: string) => {
  const params = new URLSearchParams({ role, page: String(page), limit: String(limit) });
  if (search) params.append("search", search);
  return api<{ items: User[]; total: number; pageCount: number; currentPage: number; size: number }>(`/coordinator/users?${params}`);
};

export const getGrades = () =>
  api<GradeWeightedAverageResponse[]>("/coordinator/grades");

export const getEvaluationSheetPdf = (projectId: number) =>
  api<Blob>("/coordinator/documents/pdf/evaluation-sheets", {
    method: "POST",
    body: JSON.stringify({ projectId }),
    responseType: "blob",
  });

export const getAttendanceListPdf = (defenseSessionId: number) =>
  api<Blob>("/coordinator/documents/pdf/attendance-lists", {
    method: "POST",
    body: JSON.stringify({ defenseSessionId }),
    responseType: "blob",
  });

export const getJuryConvocationsPdf = (projectId: number) =>
  api<Blob>("/coordinator/documents/pdf/jury-convocations", {
    method: "POST",
    body: JSON.stringify({ projectId }),
    responseType: "blob",
  });

export const getDefenseScheduleDocPdf = (defenseSessionId: number) =>
  api<Blob>("/coordinator/documents/pdf/schedule", {
    method: "POST",
    body: JSON.stringify({ defenseSessionId }),
    responseType: "blob",
  });

export const getProcesVerbalPdf = (projectId: number) =>
  api<Blob>("/coordinator/documents/pdf/proces-verbal", {
    method: "POST",
    body: JSON.stringify({ projectId }),
    responseType: "blob",
  });
