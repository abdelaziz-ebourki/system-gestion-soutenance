export type UserRole = "admin" | "coordinator" | "teacher" | "student";

export interface User {
  id: number;
  lastName: string;
  firstName: string;
  email: string;
  role: UserRole;
  isActive: boolean;
  password?: string;
  cne?: string;
  codeApogee?: string;
  majorId?: number;
  majorName?: string;
  levelId?: number;
  levelName?: string;
  teacherRankId?: number;
  teacherRankName?: string;
  departmentId?: number;
  departmentName?: string;
}

export type Student = User;
export type Teacher = User;
export type Coordinator = User;

export interface Major {
  id: number;
  name: string;
  departmentId?: number;
  departmentName?: string;
  studentCount?: number;
}

export interface Level {
  id: number;
  name: string;
}

export interface TeacherRank {
  id: number;
  name: string;
}

export interface Faculty {
  id: number;
  name: string;
  code: string;
  deanId?: number;
  logoUrl?: string;
}

export interface Department {
  id: number;
  name: string;
  code: string;
  headId?: number;
  facultyId?: number;
  facultyName?: string;
}

export interface Room {
  id: number;
  name: string;
  capacity: number;
  departmentId: number;
}
