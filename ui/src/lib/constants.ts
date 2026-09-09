import type { DefenseType } from "@/types";

export const STORAGE_KEYS = {
  USER: "user",
} as const;

export const DEFENSE_TYPE_LABELS: Record<DefenseType, string> = {
  pfe: "Projet de Fin d'Études",
  memoire: "Mémoire",
  these: "Thèse",
};

export const DEFENSE_TYPE_SHORT_LABELS: Record<DefenseType, string> = {
  pfe: "PFE",
  memoire: "Mémoire",
  these: "Thèse",
};

export const DEFENSE_TYPE_OPTIONS = [
  { value: "pfe" as const, label: "PFE" },
  { value: "memoire" as const, label: "Mémoire" },
  { value: "these" as const, label: "Thèse" },
];

export const DEFENSE_ROLE_LABELS: Record<string, string> = {
  president: "Président",
  reporter: "Rapporteur",
  examiner: "Examinateur",
  supervisor: "Encadrant",
};

export const DEFENSE_SESSION_LIFECYCLE: Record<string, string[]> = {
  draft: ["active"],
  active: ["scheduled"],
  scheduled: ["completed"],
  completed: ["archived"],
};

export const DEFENSE_SESSION_STATUS_LABELS: Record<string, string> = {
  draft: "Brouillon",
  active: "Active",
  scheduled: "Planifiée",
  completed: "Terminée",
  archived: "Archivée",
};

export const DEFENSE_SESSION_STATUS_BADGE: Record<string, "default" | "secondary" | "outline"> = {
  draft: "secondary",
  active: "default",
  scheduled: "outline",
  completed: "secondary",
  archived: "secondary",
};

export const DEFAULT_API_LIMIT = 10;
export const MAX_TEACHER_FETCH_LIMIT = 5000;
export const AUDIT_LOG_PAGE_SIZE = 20;
export const DEFAULT_RETRY = 1;
export const DEFAULT_STALE_TIME = 30_000;
export const CONFIG_STALE_TIME = 5 * 60_000;
export const DOC_STALE_TIME = 60_000;
export const NOTIFICATION_POLL_INTERVAL = 30_000;
export const MS_PER_MINUTE = 60_000;
export const MS_PER_DAY = 86_400_000;
export const GRACE_PERIOD_DAYS = 2;
export const TOAST_DURATION_MS = 5000;
export const MAX_SCORE = 20;
export const MIN_PASSWORD_LENGTH = 8;
export const MAX_SUGGESTIONS = 3;
export const MAX_STUDENT_FETCH_LIMIT = 5000;
export const PAGE_SIZE_OPTIONS = [10, 20, 50];
