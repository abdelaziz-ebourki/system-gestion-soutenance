export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  pageCount: number;
  currentPage: number;
  size: number;
}

export * from "./users-types";

export interface DashboardStats {
  totalStudents: number;
  totalTeachers: number;
  totalDepartments: number;
  totalRooms: number;
  totalDefenseSessions: number;
}

export interface Project {
  id: number;
  title: string;
  description: string;
  defenseType: string;
  groupId: number;
  supervisorName: string;
  studentNames: string[];
}

export interface Group {
  id: number;
  groupName: string;
  projectId: number;
  memberCount: number;
  studentNames: string[];
}

export interface Jury {
  id: number;
  projectId: number;
  projectTitle: string;
  defenseType: string;
  members: JuryMember[];
}

export interface JuryMember {
  roleName: string;
  teacherId: number;
  teacherName: string;
}

export interface DefenseSession {
  id: number;
  name: string;
  defenseType: string;
  status: string;
  maxGroupSize: number;
  defenseDuration: number;
  breakDuration: number;
  submissionDeadline: string;
  evaluationCoefficients: Record<string, number>;
  juryRoleTemplateId: number;
  startDate: string;
  endDate: string;
}

export interface TeacherStats {
  upcomingDefenses: number;
  pendingEvaluations: number;
  declaredUnavailabilitySlots: number;
  juryAssignments: number;
}

export interface TeacherDefense {
  date: string;
  time: string;
  roomName: string;
  projectTitle: string;
  studentNames: string[];
}

export interface TeacherEvaluation {
  id: number;
  projectId: number;
  projectTitle: string;
  finalGrade: number;
  comment: string;
  status: string;
}

export interface TeacherUnavailability {
  slotsByDate: Record<string, string[]>;
}

export interface StudentStats {
  documentCount: number;
  missingDocuments: number;
  groupMembers: number;
  defenseStatus: string;
}

export interface StudentDefenseDetails {
  projectTitle: string;
  projectDescription: string;
  supervisorName: string;
  juryMembers: Array<{
    roleName: string;
    teacherName: string;
  }>;
  date: string;
  startTime: string;
  endTime: string;
  roomName: string;
  status: string;
  convocationUrl: string;
  result: string;
}

export interface StudentGroupDetails {
  id: number;
  groupName: string;
  projectTitle: string;
  supervisorName: string;
  members: Array<{
    id: number;
    fullName: string;
    email: string;
    role: string;
  }>;
}

export interface StudentDocument {
  id: number;
  studentId: number;
  name: string;
  type: string;
  deadline: string;
  status: string;
  submittedAt: string;
  filePath: string;
}

export type GroupDocumentType = "REPORT" | "PRESENTATION" | "DIVERSE";

export interface GroupDocument {
  id: number;
  groupId: number;
  type: GroupDocumentType;
  name: string;
  deadline: string;
  status: "missing" | "submitted" | "validated" | "rejected";
  submittedAt: string | null;
  filePath: string;
}

export type DefenseType = "pfe" | "memoire" | "these";

export type DefenseSessionStatus = "draft" | "active" | "scheduled" | "completed" | "archived";

export type SlotKey = string & { __brand: 'SlotKey' };

export interface AppNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
  actionLink: string;
  actor: string;
}

export interface JuryRole {
  name: string;
  count: number;
  coefficient: number;
}

export interface JuryRoleTemplate {
  id: number;
  name: string;
  defenseType: string;
  roles: JuryRole[];
}

export interface StudentGroupWorkspace {
  currentGroup: StudentGroupDetails | null;
  availableGroups: Array<{
    id: number;
    groupName: string;
    memberCount: number;
  }>;
  groupCreationStartDate: string;
  groupCreationEndDate: string;
  isGroupCreationOpen: boolean;
}

export interface DefenseSlot {
  id: number;
  projectId: number;
  slot: string;
  date: string;
  roomId: number;
}
