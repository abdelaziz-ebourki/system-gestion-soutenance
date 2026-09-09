import { api, type DefenseSettings } from "./api-core";
import type { DashboardStats, JuryRoleTemplate, User, Department, Room, Major, Level, Faculty, TeacherRank, PaginatedResponse } from "@/types";
import { MAX_TEACHER_FETCH_LIMIT, AUDIT_LOG_PAGE_SIZE, DEFAULT_API_LIMIT } from "@/lib/constants";
import type { AuditLog } from "@/types/audit-log";

export const getAdminStats = () => api<DashboardStats>("/admin/stats");

export const getAuditLogs = (page = 0, limit = AUDIT_LOG_PAGE_SIZE) =>
  api<PaginatedResponse<AuditLog>>(`/admin/audit-logs?page=${page}&limit=${limit}`);

export const getUsers = (params: {
  role?: string;
  page?: number;
  limit?: number;
  search?: string;
}) => {
  const query = new URLSearchParams();
  if (params.role) query.append("role", params.role);
  if (params.page !== undefined) query.append("page", params.page.toString());
  if (params.limit !== undefined) query.append("limit", params.limit.toString());
  if (params.search) query.append("search", params.search);

  return api<PaginatedResponse<User>>(`/admin/users?${query.toString()}`);
};

export const getTeachersList = (limit = MAX_TEACHER_FETCH_LIMIT) =>
  api<PaginatedResponse<User>>(`/admin/users?role=TEACHER&limit=${limit}`);

export const createUser = (data: {
  lastName: string;
  firstName: string;
  email: string;
  role: string;
  cne?: string;
  majorId?: number;
  levelId?: number;
  teacherRankId?: number;
  departmentId?: number;
}) =>
  api<User>("/admin/users", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const bulkCreateUsers = (
  users: Array<{
    lastName: string;
    firstName: string;
    email: string;
    cne?: string;
    majorName?: string;
    levelName?: string;
    teacherRankName?: string;
    departmentName?: string;
  }>,
  role: string,
) =>
  api<User[]>("/admin/users/bulk", {
    method: "POST",
    body: JSON.stringify({ users, role }),
  });

export const updateUser = (id: number, data: {
  lastName: string;
  firstName: string;
  email: string;
  role: string;
  cne?: string;
  majorId?: number;
  levelId?: number;
  teacherRankId?: number;
  departmentId?: number;
}) =>
  api<User>(`/admin/users/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteUser = (id: number) =>
  api<void>(`/admin/users/${id}`, { method: "DELETE" });

export const getRooms = (page = 0, limit = DEFAULT_API_LIMIT) => {
  const params = new URLSearchParams({ page: String(page), limit: String(limit) });
  return api<PaginatedResponse<Room>>(`/admin/rooms?${params}`);
};

export const createRoom = (data: { name: string; capacity: number; departmentId: number }) =>
  api<Room>("/admin/rooms", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const bulkCreateRooms = (rooms: Array<{ name: string; capacity: number; departmentId: number }>) =>
  api<Room[]>("/admin/rooms/bulk", {
    method: "POST",
    body: JSON.stringify({ rooms }),
  });

export const updateRoom = (id: number, data: { name: string; capacity: number; departmentId: number }) =>
  api<Room>(`/admin/rooms/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteRoom = (id: number) =>
  api<void>(`/admin/rooms/${id}`, { method: "DELETE" });

export const getDepartments = () => api<PaginatedResponse<Department>>("/admin/departments");
export const createDepartment = (data: { name: string; code: string; headId?: number; facultyId?: number }) =>
  api<Department>("/admin/departments", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateDepartment = (id: number, data: { name: string; code: string; headId?: number; facultyId?: number }) =>
  api<Department>(`/admin/departments/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteDepartment = (id: number) =>
  api<void>(`/admin/departments/${id}`, { method: "DELETE" });

export const getFaculties = () => api<PaginatedResponse<Faculty>>("/admin/faculties");
export const createFaculty = (data: { name: string; code: string; deanId?: number; logoUrl?: string }) =>
  api<Faculty>("/admin/faculties", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateFaculty = (id: number, data: { name: string; code: string; deanId?: number; logoUrl?: string }) =>
  api<Faculty>(`/admin/faculties/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteFaculty = (id: number) =>
  api<void>(`/admin/faculties/${id}`, { method: "DELETE" });

export const getMajors = () => api<PaginatedResponse<Major>>("/admin/config/majors");
export const createMajor = (data: { name: string; departmentId?: number }) =>
  api<Major>("/admin/config/majors", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateMajor = (id: number, data: { name: string; departmentId?: number }) =>
  api<Major>(`/admin/config/majors/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteMajor = (id: number) =>
  api<void>(`/admin/config/majors/${id}`, { method: "DELETE" });

export const getLevels = () => api<PaginatedResponse<Level>>("/admin/config/levels");
export const createLevel = (data: { name: string }) =>
  api<Level>("/admin/config/levels", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateLevel = (id: number, data: { name: string }) =>
  api<Level>(`/admin/config/levels/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteLevel = (id: number) =>
  api<void>(`/admin/config/levels/${id}`, { method: "DELETE" });

export const getTeacherRanks = () => api<TeacherRank[]>("/admin/config/teacher-ranks");
export const createTeacherRank = (data: { name: string }) =>
  api<TeacherRank>("/admin/config/teacher-ranks", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateTeacherRank = (id: number, data: { name: string }) =>
  api<TeacherRank>(`/admin/config/teacher-ranks/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteTeacherRank = (id: number) =>
  api<void>(`/admin/config/teacher-ranks/${id}`, { method: "DELETE" });

export const getJuryRoleTemplates = () => api<PaginatedResponse<JuryRoleTemplate>>("/admin/config/jury-role-templates");
export const createJuryRoleTemplate = (data: { name: string; defenseType: string; roles: Array<{ name: string; count: number; coefficient: number }> }) =>
  api<JuryRoleTemplate>("/admin/config/jury-role-templates", {
    method: "POST",
    body: JSON.stringify(data),
  });
export const updateJuryRoleTemplate = (id: number, data: { name: string; defenseType: string; roles: Array<{ name: string; count: number; coefficient: number }> }) =>
  api<JuryRoleTemplate>(`/admin/config/jury-role-templates/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
export const deleteJuryRoleTemplate = (id: number) =>
  api<void>(`/admin/config/jury-role-templates/${id}`, { method: "DELETE" });

export const getDefenseSettings = () =>
  api<DefenseSettings>("/admin/config/settings");
export const updateDefenseSettings = (data: {
  startTime: string;
  endTime: string;
  defenseDuration: number;
  breakDuration: number;
  groupCreationStartDate: string;
  groupCreationEndDate: string;
}) =>
  api<DefenseSettings>("/admin/config/settings", {
    method: "PUT",
    body: JSON.stringify(data),
  });

