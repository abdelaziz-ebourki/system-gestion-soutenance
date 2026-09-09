export { api, getNotifications, markNotificationRead, markAllNotificationsRead, sendNotificationEmail, type ApiResponse, type DefenseSettings } from "./api-core";
export * from "./api-admin";
export {
  getCoordinatorStats, getProjects, createProject, updateProject, deleteProject,
  getGroups, createGroup, deleteGroup,
  getJuries, createJury, updateJury, deleteJury,
  getCoordinatorDefenseSessions, createCoordinatorDefenseSession, updateCoordinatorDefenseSession, deleteCoordinatorDefenseSession,
  transitionDefenseSession, getSchedules, saveSchedules, autoGenerateSchedules, publishSchedule,
  cancelDefense, validateConflicts, getCoordinatorUnavailability, getCoordinatorUsers, assignProjectToGroup,
  getEvaluationSheetPdf, getAttendanceListPdf, getJuryConvocationsPdf, getDefenseScheduleDocPdf, getProcesVerbalPdf,
  type CreateProjectPayload, type UpdateProjectPayload, type MemberEntry, type CreateJuryPayload, type UpdateJuryPayload,
  type ScheduleSlot, type ScheduleResponse, type ConflictDetail, type GradeWeightedAverageResponse,
  type CoordinatorStats, type UnavailabilityEntry,
} from "./api-coordinator";
export { getGrades as getCoordinatorGrades } from "./api-coordinator";
export * from "./api-teacher";
export * from "./api-student";
