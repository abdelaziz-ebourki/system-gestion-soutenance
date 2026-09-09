import { api } from "./api-core";
import type {
  TeacherStats, TeacherDefense, TeacherEvaluation, TeacherUnavailability, PaginatedResponse,
} from "@/types";

export const getTeacherStats = () => api<TeacherStats>("/teacher/stats");

export const getTeacherSchedule = () =>
  api<{ slots: TeacherDefense[] }>("/teacher/schedules");

export const getTeacherEvaluations = () =>
  api<PaginatedResponse<TeacherEvaluation>>("/teacher/evaluations");

export const submitTeacherEvaluation = (
  id: number,
  data: { score: number; comment: string },
) =>
  api<TeacherEvaluation>(`/teacher/evaluations/${id}`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getTeacherUnavailability = () =>
  api<TeacherUnavailability>("/teacher/unavailabilities");

export const saveTeacherUnavailability = (data: { slots: Array<{ date: string; slots: string[] }> }) =>
  api<TeacherUnavailability>("/teacher/unavailabilities", {
    method: "POST",
    body: JSON.stringify(data),
  });
