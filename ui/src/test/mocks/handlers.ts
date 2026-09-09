import { http, HttpResponse } from "msw";
import { mockJson, mockPaginated, mockCrud, mockEcho } from "./registry";

// ── Mock data ──────────────────────────────────────────────────────────

const NOTIFICATIONS = [
  { id: 1, type: "info", title: "Notification test", message: "Ceci est une notification de test", timestamp: new Date().toISOString(), read: false, actionLink: "", actor: "" },
];

const ROOMS = [
  { id: 1, name: "Salle 101", capacity: 30, departmentId: 1 },
  { id: 2, name: "Salle 102", capacity: 20, departmentId: 2 },
];

const DEPARTMENTS = [
  { id: 1, name: "Informatique", code: "INFO", headId: 1 },
  { id: 2, name: "Mathématiques", code: "MATH", headId: null },
];

const MAJORS = [
  { id: 1, name: "Génie Informatique", departmentId: 1, departmentName: "Informatique", studentCount: 12 },
  { id: 2, name: "Génie Civil", departmentId: 2, departmentName: "Mathématiques", studentCount: 8 },
];

const LEVELS = [
  { id: 1, name: "L3" },
  { id: 2, name: "M1" },
];

const STUDENTS = [
  { id: 1, cne: "CNE001", codeApogee: "APG001", lastName: "Dupont", firstName: "Jean", email: "jean.dupont@example.com", majorId: 1, majorName: "Génie Informatique", levelId: 1, levelName: "L3", departmentId: 1, departmentName: "Informatique", isActive: true, role: "student" },
  { id: 2, cne: "CNE002", codeApogee: "APG002", lastName: "Martin", firstName: "Sophie", email: "sophie.martin@example.com", majorId: 2, majorName: "Génie Civil", levelId: 2, levelName: "M1", departmentId: 2, departmentName: "Mathématiques", isActive: false, role: "student" },
];

const TEACHERS = [
  { id: 1, lastName: "Benali", firstName: "Ahmed", email: "ahmed.benali@example.com", departmentId: 1, teacherRankId: 1, isActive: true, role: "teacher" },
  { id: 2, lastName: "Amrani", firstName: "Fatima", email: "fatima.amrani@example.com", departmentId: 2, teacherRankId: 2, isActive: false, role: "teacher" },
];

const COORDINATORS = [
  { id: 1, lastName: "Idrissi", firstName: "Hassan", email: "hassan.idrissi@example.com", isActive: true, role: "coordinator" },
  { id: 2, lastName: "El Fassi", firstName: "Nadia", email: "nadia.elfassi@example.com", isActive: false, role: "coordinator" },
];

const ALL_USERS = [...STUDENTS, ...TEACHERS, ...COORDINATORS];

const AUDIT_LOGS = [
  { id: 1, action: "LOGIN", entity: "user", entityId: 1, performedByEmail: "admin@univh2c.ma", details: "Connexion admin", timestamp: new Date().toISOString() },
  { id: 2, action: "CREATE", entity: "room", entityId: 2, performedByEmail: "admin@univh2c.ma", details: "Création salle S101", timestamp: new Date().toISOString() },
];

const PROJECTS = [
  { id: 1, title: "Application CI/CD", description: "Pipeline d'intégration continue", groupId: 1, supervisorId: 1, supervisorName: "Ahmed Benali", studentNames: ["Jean Dupont", "Sophie Martin"], defenseType: "pfe" },
  { id: 2, title: "Analyse des données", description: "Traitement et visualisation", groupId: 2, supervisorId: 2, supervisorName: "Fatima Amrani", studentNames: ["Amine El Idrissi"], defenseType: "memoire" },
];

const JURIES = [
  { id: 1, projectId: 1, projectTitle: "Application CI/CD", defenseType: "pfe", members: [{ roleName: "President", teacherId: 1, teacherName: "Ahmed Benali" }, { roleName: "Rapporteur", teacherId: 2, teacherName: "Fatima Amrani" }] },
];

const DEFENSE_SESSIONS = [
  { id: 1, name: "Session PFE 2026", defenseType: "pfe", status: "active", maxGroupSize: 3, defenseDuration: 60, breakDuration: 15, submissionDeadline: "2026-06-01", evaluationCoefficients: { President: 2, Rapporteur: 1.5, Examinateur: 1 }, juryRoleTemplateId: 1, startDate: "2026-06-15", endDate: "2026-06-30" },
  { id: 2, name: "Session Mémoire 2026", defenseType: "memoire", status: "draft", maxGroupSize: 2, defenseDuration: 45, breakDuration: 10, submissionDeadline: "2026-07-01", evaluationCoefficients: { President: 2, Rapporteur: 1.5 }, juryRoleTemplateId: 1, startDate: "2026-07-10", endDate: "2026-07-25" },
];

const TEACHER_SCHEDULE = [
  { date: "2026-06-15", time: "08:00", roomName: "Salle 101", projectTitle: "Application CI/CD", studentNames: ["Jean Dupont", "Sophie Martin"] },
  { date: "2026-06-16", time: "10:00", roomName: "Salle 102", projectTitle: "Analyse des données", studentNames: ["Amine El Idrissi"] },
];

const TEACHER_EVALUATIONS = [
  { id: 1, projectId: 1, projectTitle: "Application CI/CD", finalGrade: 0, comment: "", status: "pending" },
  { id: 2, projectId: 2, projectTitle: "Analyse des données", finalGrade: 15, comment: "Bon travail", status: "submitted" },
];

const STUDENT_DOCUMENTS = [
  { id: 1, studentId: 1, name: "Rapport PFE", type: "report", deadline: "2027-06-01", status: "submitted", submittedAt: "2027-05-28T10:00:00Z", filePath: "/uploads/rapport.pdf" },
  { id: 2, studentId: 1, name: "Fiche de présentation", type: "presentation", deadline: "2027-06-05", status: "missing", submittedAt: "", filePath: "" },
  { id: 3, studentId: 1, name: "Déclaration sur l'honneur", type: "declaration", deadline: "2027-06-01", status: "validated", submittedAt: "2027-05-25T14:00:00Z", filePath: "/uploads/decl.pdf" },
  { id: 4, studentId: 1, name: "Attestation de stage", type: "internship", deadline: "2027-06-01", status: "rejected", submittedAt: "2027-05-20T09:00:00Z", filePath: "/uploads/att.pdf" },
];

const GROUP_DOCUMENTS = [
  { id: 1, groupId: 1, type: "REPORT", name: "Rapport PFE", deadline: "2027-06-01", status: "submitted", submittedAt: "2027-05-28T10:00:00Z", filePath: "/uploads/rapport.pdf" },
  { id: 2, groupId: 1, type: "PRESENTATION", name: "Fiche de présentation", deadline: "2027-06-05", status: "missing", submittedAt: null, filePath: "" },
  { id: 3, groupId: 1, type: "DIVERSE", name: "Divers", deadline: "2027-06-10", status: "missing", submittedAt: null, filePath: "" },
];

const GROUPS = [
  { id: 1, groupName: "Groupe Alpha", projectId: 1, memberCount: 2, studentNames: ["Jean Dupont", "Sophie Martin"] },
  { id: 2, groupName: "Groupe Beta", projectId: 0, memberCount: 1, studentNames: ["Sarah Benali"] },
];

const SCHEDULES = [
  { id: 1, projectId: 1, projectTitle: "Application CI/CD", date: "2026-06-15", time: "08:00", roomId: 1, roomName: "Salle 101", studentNames: ["Jean Dupont", "Sophie Martin"], role: "President", status: "scheduled" },
  { id: 2, projectId: 2, projectTitle: "Analyse des données", date: "2026-06-15", time: "09:00", roomId: 2, roomName: "Salle 102", studentNames: ["Amine El Idrissi"], role: "Rapporteur", status: "scheduled" },
];

const UNAVAILABILITY = [
  { id: 1, teacherId: 1, date: "2026-06-10", slots: ["08:00-09:00", "09:00-10:00"] },
];

const GRADES = [
  { projectId: 1, projectTitle: "Application CI/CD", defenseDate: "2026-06-15", status: "completed", finalScore: 16.5, evaluationCoefficients: { President: 2, Rapporteur: 1.5 }, individualScores: [{ roleName: "President", teacherName: "Ahmed Benali", score: 17 }, { roleName: "Rapporteur", teacherName: "Fatima Amrani", score: 16 }] },
  { projectId: 2, projectTitle: "Analyse des données", defenseDate: null, status: "pending", finalScore: null, evaluationCoefficients: { President: 2, Rapporteur: 1.5 }, individualScores: [] },
];

const JURY_ROLE_TEMPLATES = [{ id: 1, name: "Standard PFE", defenseType: "pfe", roles: [{ name: "President", count: 1, coefficient: 2 }, { name: "Rapporteur", count: 1, coefficient: 1.5 }, { name: "Examinateur", count: 1, coefficient: 1 }] }];

// ── Handlers ───────────────────────────────────────────────────────────

function filterByRole(url: URL, data: typeof ALL_USERS) {
  const role = url.searchParams.get("role");
  const page = Number(url.searchParams.get("page") || 0);
  const limit = Number(url.searchParams.get("limit") || 10);
  let filtered = role ? data.filter((u) => u.role.toLowerCase() === role.toLowerCase()) : data;
  const total = filtered.length;
  const pageCount = Math.ceil(total / limit) || 1;
  filtered = filtered.slice(page * limit, (page + 1) * limit);
  return HttpResponse.json({ items: filtered, total, pageCount, currentPage: page, size: limit });
}

export const handlers = [
  // Auth
  http.post("*/api/auth/login", async ({ request }) => {
    const body = (await request.json()) as { email?: string; password?: string };
    if (body.email === "admin@univh2c.ma" && body.password === "1234") {
      return HttpResponse.json({ user: { id: 1, email: "admin@univh2c.ma", role: "admin", lastName: "Admin", firstName: "User", isActive: true }, expiresAt: Date.now() + 7200000 });
    }
    if (body.email === "teacher@univh2c.ma" && body.password === "1234") {
      return HttpResponse.json({ user: { id: 3, email: "teacher@univh2c.ma", role: "teacher", lastName: "Teacher", firstName: "User", isActive: true }, expiresAt: Date.now() + 7200000 });
    }
    return HttpResponse.json({ message: "Identifiants invalides" }, { status: 401 });
  }),

  http.post("*/api/auth/logout", () => {
    return HttpResponse.json(undefined, { status: 204 });
  }),

  http.post("*/api/auth/forgot-password", () =>
    HttpResponse.json({ message: "Si cet email existe, un lien..." }),
  ),

  http.post("*/api/auth/reset-password", async ({ request }) => {
    const body = (await request.json()) as { token?: string };
    if (body.token === "expired-token") {
      return HttpResponse.json({ message: "Token expiré" }, { status: 400 });
    }
    return HttpResponse.json({ message: "Mot de passe réinitialisé" });
  }),

  http.post("*/api/auth/verify-account", async ({ request }) => {
    const body = (await request.json()) as { token?: string };
    if (body.token === "invalid-token") {
      return HttpResponse.json({ message: "Token invalide" }, { status: 404 });
    }
    return HttpResponse.json({ message: "Account verified successfully" });
  }),

  http.get("*/api/me", () => {
    return HttpResponse.json({ id: 1, email: "admin@univh2c.ma", role: "admin", firstName: "Admin", lastName: "System", isActive: true });
  }),

  http.patch("*/api/me", async ({ request }) => {
    const body = (await request.json()) as { firstName?: string; lastName?: string };
    return HttpResponse.json({ id: 1, email: "admin@univh2c.ma", role: "admin", firstName: body.firstName, lastName: body.lastName, isActive: true });
  }),

  http.put("*/api/me/password", async ({ request }) => {
    const body = (await request.json()) as { currentPassword?: string; newPassword?: string };
    if (body.currentPassword === "wrong") {
      return HttpResponse.json({ message: "Mot de passe actuel incorrect" }, { status: 400 });
    }
    return HttpResponse.json({ message: "Mot de passe modifié" });
  }),

  // Notifications
  ...mockPaginated("*/api/notifications", NOTIFICATIONS),
  mockJson("patch", "*/api/notifications/:id/read", { message: "Marked as read" }),
  mockJson("patch", "*/api/notifications/read-all", { message: "All marked as read" }),

  // Admin — simple GETs
  mockJson("get", "*/api/admin/config/settings", { id: 1, startTime: "08:00", endTime: "18:00", defenseDuration: 60, breakDuration: 15, groupCreationStartDate: "2026-01-01", groupCreationEndDate: "2026-06-01" }),
  mockJson("get", "*/api/admin/stats", { totalStudents: 100, totalTeachers: 20, totalDepartments: 5, totalRooms: 15, totalDefenseSessions: 3 }),
  ...mockPaginated("*/api/admin/departments", DEPARTMENTS),
  ...mockPaginated("*/api/admin/faculties", []),
  ...mockPaginated("*/api/admin/rooms", ROOMS, 2),
  ...mockPaginated("*/api/admin/config/majors", MAJORS),
  ...mockPaginated("*/api/admin/config/levels", LEVELS),
  mockJson("get", "*/api/admin/config/grades", []),
  ...mockPaginated("*/api/admin/config/jury-role-templates", JURY_ROLE_TEMPLATES),

  // Admin — paginated GETs (role-filtered /admin/users)
  http.get("*/api/admin/users", ({ request }) => filterByRole(new URL(request.url), ALL_USERS)),
  ...mockPaginated("*/api/admin/audit-logs", AUDIT_LOGS, 2),

  // Admin — CRUD
  ...mockCrud("*/api/admin/users"),
  ...mockCrud("*/api/admin/rooms"),
  ...mockCrud("*/api/admin/departments"),
  ...mockCrud("*/api/admin/config/majors"),
  ...mockCrud("*/api/admin/config/levels"),

  // Admin — special endpoints
  http.post("*/api/admin/users/bulk", async ({ request }) => {
    const body = (await request.json()) as { users: Record<string, unknown>[]; role: string };
    return HttpResponse.json(body.users.map((u, i) => ({ id: 100 + i, ...u, role: body.role, isActive: false })), { status: 201 });
  }),

  http.post("*/api/admin/rooms/bulk", async ({ request }) => {
    const body = (await request.json()) as { rooms: Record<string, unknown>[] };
    return HttpResponse.json(body.rooms.map((r, i) => ({ id: 100 + i, ...r })), { status: 201 });
  }),

  // Coordinator — simple GETs
  mockJson("get", "*/api/coordinator/stats", { totalProjects: 12, totalGroups: 8, totalJuries: 6, scheduledDefenses: 4 }),
  ...mockPaginated("*/api/coordinator/projects", PROJECTS),
  ...mockPaginated("*/api/coordinator/juries", JURIES),
  ...mockPaginated("*/api/coordinator/defense-sessions", DEFENSE_SESSIONS),
  mockJson("get", "*/api/coordinator/grades", GRADES),
  ...mockPaginated("*/api/coordinator/groups", GROUPS),
  mockJson("get", "*/api/coordinator/schedules", SCHEDULES),
  mockJson("get", "*/api/coordinator/unavailability", UNAVAILABILITY),

  // Coordinator — CRUD
  ...mockCrud("*/api/coordinator/projects"),
  ...mockCrud("*/api/coordinator/juries"),
  ...mockCrud("*/api/coordinator/defense-sessions"),
  ...mockCrud("*/api/coordinator/groups"),

  // Coordinator — special endpoints
  http.post("*/api/coordinator/defense-sessions/:id/transition", async ({ request }) => {
    const body = (await request.json()) as { toStatus?: string };
    return HttpResponse.json({ id: 1, status: body.toStatus ?? "active" });
  }),

  http.post("*/api/coordinator/schedules", async () => {
    return HttpResponse.json(SCHEDULES);
  }),

  http.post("*/api/coordinator/schedules/generation", async () => {
    return HttpResponse.json(SCHEDULES);
  }),

  http.patch("*/api/coordinator/schedules/publication", async () => {
    return HttpResponse.json(undefined, { status: 204 });
  }),

  http.post("*/api/coordinator/groups/assign", async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json({ id: 1, ...(body as Record<string, unknown>) });
  }),

  http.post("*/api/coordinator/projects/bulk", async ({ request }) => {
    const body = (await request.json()) as { projects: Array<{ title: string; description: string; supervisorEmail: string; defenseType: string }> };
    const created = body.projects.map((p, i) => ({ id: 1000 + i, title: p.title }));
    return HttpResponse.json(
      { success: true, message: "Projets importés", data: { total: body.projects.length, imported: created.length, created, errors: [] }, timestamp: new Date().toISOString(), errors: [] },
      { status: 201 },
    );
  }),

  http.post("*/api/coordinator/defenses/:id/cancel", async () => {
    return HttpResponse.json(undefined, { status: 204 });
  }),

  http.post("*/api/coordinator/conflicts/validate", async () => {
    return HttpResponse.json([]);
  }),

  // Coordinator — documents (PDF blob responses)
  http.post("*/api/coordinator/documents/pdf/evaluation-sheets", () =>
    new HttpResponse("mock-evaluation-sheet-pdf", { status: 200, headers: { "Content-Type": "application/pdf" } }),
  ),

  http.post("*/api/coordinator/documents/pdf/attendance-lists", () =>
    new HttpResponse("mock-attendance-list-pdf", { status: 200, headers: { "Content-Type": "application/pdf" } }),
  ),

  http.post("*/api/coordinator/documents/pdf/jury-convocations", () =>
    new HttpResponse("mock-jury-convocation-pdf", { status: 200, headers: { "Content-Type": "application/pdf" } }),
  ),

  http.post("*/api/coordinator/documents/pdf/schedule", () =>
    new HttpResponse("mock-schedule-pdf", { status: 200, headers: { "Content-Type": "application/pdf" } }),
  ),

  http.post("*/api/coordinator/documents/pdf/proces-verbal", () =>
    new HttpResponse("mock-proces-verbal-pdf", { status: 200, headers: { "Content-Type": "application/pdf" } }),
  ),

  // Teacher
  mockJson("get", "*/api/teacher/stats", { upcomingDefenses: 5, pendingEvaluations: 3, declaredUnavailabilitySlots: 2, juryAssignments: 4 }),
  mockJson("get", "*/api/teacher/schedules", { slots: TEACHER_SCHEDULE }),
  ...mockPaginated("*/api/teacher/evaluations", TEACHER_EVALUATIONS),
  mockJson("get", "*/api/teacher/unavailability", { slotsByDate: { "2026-06-10": ["08:00-09:00", "09:00-10:00"], "2026-06-11": ["14:00-15:00"] } }),
  mockEcho("post", "*/api/teacher/unavailabilities"),

  http.post("*/api/teacher/evaluations/:id", async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json({ id: 1, ...(body as Record<string, unknown>), status: "submitted", submittedAt: new Date().toISOString() });
  }),

  // Student
  mockJson("get", "*/api/student/stats", { documentCount: 4, missingDocuments: 1, groupMembers: 3, defenseStatus: "scheduled" }),
  mockJson("get", "*/api/student/defenses", { projectTitle: "Application CI/CD", projectDescription: "Pipeline d'intégration continue", supervisorName: "Ahmed Benali", juryMembers: [{ teacherName: "Ahmed Benali", roleName: "President" }, { teacherName: "Fatima Amrani", roleName: "Rapporteur" }], date: "2026-06-15", startTime: "08:00", endTime: "09:00", roomName: "Salle 101", status: "scheduled", convocationUrl: "", result: null }),
  ...mockPaginated("*/api/student/documents", STUDENT_DOCUMENTS),

  http.get("*/api/student/convocations", () =>
    HttpResponse.json(new Blob(["mock-pdf-content"], { type: "application/pdf" })),
  ),

  http.get("*/api/student/groups", () =>
    HttpResponse.json({
      currentGroup: {
        id: 1,
        groupName: "Groupe Alpha",
        projectTitle: "Application CI/CD",
        supervisorName: "Ahmed Benali",
        members: [{ id: 1, fullName: "Jean Dupont", email: "jean.dupont@example.com", role: "leader" }, { id: 2, fullName: "Sophie Martin", email: "sophie.martin@example.com", role: "member" }],
      },
      availableGroups: [{ id: 2, groupName: "Groupe Beta", memberCount: 2 }],
      groupCreationStartDate: "2026-01-01",
      groupCreationEndDate: "2026-06-01",
      isGroupCreationOpen: true,
    }),
  ),

  http.post("*/api/student/groups", () =>
    HttpResponse.json({ id: 1, groupName: "Nouveau Groupe", members: [] }, { status: 201 }),
  ),

  http.post("*/api/student/groups/:id/members", () =>
    HttpResponse.json({ id: 1, groupName: "Groupe Rejoint", members: [] }, { status: 201 }),
  ),

  http.post("*/api/student/documents/:documentId/attachments", async ({ request }) => {
    const body = await request.formData();
    const file = body.get("file") as File | null;
    return HttpResponse.json({ id: 1, name: file?.name ?? "uploaded-file", type: "upload", deadline: "2027-06-01", status: "submitted", submittedAt: new Date().toISOString(), studentId: 1, filePath: "" }, { status: 201 });
  }),

  // Group documents
  http.get("*/api/groups/:groupId/documents", () =>
    HttpResponse.json(GROUP_DOCUMENTS),
  ),

  http.post("*/api/groups/:groupId/documents/:type/attachments", async ({ params }) => {
    const { groupId, type } = params;
    return HttpResponse.json({
      id: Date.now(), groupId: Number(groupId), type,
      name: type === "REPORT" ? "Rapport PFE" : type === "PRESENTATION" ? "Fiche de présentation" : "Divers",
      deadline: "2027-06-01", status: "submitted",
      submittedAt: new Date().toISOString(), filePath: "",
    }, { status: 201 });
  }),
];
