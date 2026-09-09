import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { ErrorBoundary } from "./components/ErrorBoundary";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import DashboardLayout from "./components/layout/DashboardLayout";
import { ROUTES } from "./config/routes";

const Login = lazy(() => import("./pages/Login"));
const VerifyAccount = lazy(() => import("./pages/auth/VerifyAccount"));
const ForgotPassword = lazy(() => import("./pages/auth/ForgotPassword"));
const ResetPassword = lazy(() => import("./pages/auth/ResetPassword"));
const AdminDashboard = lazy(() => import("./pages/admin/AdminDashboard"));
const Departments = lazy(() => import("./pages/admin/Departments"));
const Rooms = lazy(() => import("./pages/admin/Rooms"));
const Students = lazy(() => import("./pages/admin/users/Students"));
const Teachers = lazy(() => import("./pages/admin/users/Teachers"));
const Coordinators = lazy(() => import("./pages/admin/users/Coordinators"));
const Configuration = lazy(() => import("./pages/admin/Configuration"));
const CoordinatorDashboard = lazy(() => import("./pages/coordinator/CoordinatorDashboard"));
const CoordinatorProjects = lazy(() => import("./pages/coordinator/ProjectsGroups"));
const Jurys = lazy(() => import("./pages/coordinator/Jurys"));
const CoordinatorDefenseSessions = lazy(() => import("./pages/coordinator/DefenseSessions"));
const DefenseDesigner = lazy(() => import("./pages/coordinator/DefenseDesigner"));
const CoordinatorConflicts = lazy(() => import("./pages/coordinator/ConflictDashboard"));
const CoordinatorGrades = lazy(() => import("./pages/coordinator/Grades"));
const TeacherDashboard = lazy(() => import("./pages/teacher/TeacherDashboard"));
const TeacherSchedule = lazy(() => import("./pages/teacher/TeacherSchedule"));
const TeacherEvaluations = lazy(() => import("./pages/teacher/TeacherEvaluations"));
const TeacherUnavailability = lazy(() => import("./pages/teacher/TeacherUnavailability"));
const StudentDashboard = lazy(() => import("./pages/student/StudentDashboard"));
const StudentGroup = lazy(() => import("./pages/student/StudentGroup"));
const StudentDocuments = lazy(() => import("./pages/student/StudentDocuments"));
const NotFound = lazy(() => import("./pages/NotFound"));
const Profile = lazy(() => import("./pages/Profile"));
const Notifications = lazy(() => import("./pages/Notifications"));
const AuditLogs = lazy(() => import("./pages/admin/AuditLogs"));
const CoordinatorDocuments = lazy(() => import("./pages/coordinator/Documents"));

function PageLoader() {
  return (
    <div className="flex h-[60vh] items-center justify-center">
      <div className="size-10 animate-pulse rounded-full bg-muted" />
    </div>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
    <Suspense fallback={<PageLoader />}>
      <Routes>
        <Route path={ROUTES.AUTH.LOGIN} element={<Login />} />
        <Route path={ROUTES.AUTH.VERIFY_ACCOUNT} element={<VerifyAccount />} />
        <Route path={ROUTES.AUTH.FORGOT_PASSWORD} element={<ForgotPassword />} />
        <Route path={ROUTES.AUTH.RESET_PASSWORD} element={<ResetPassword />} />

        <Route element={<DashboardLayout />}>
          <Route element={<ProtectedRoute allowedRoles={["admin"]} />}>
            <Route path={ROUTES.ADMIN.CONFIG} element={<Configuration />} />
            <Route path={ROUTES.ADMIN.DASHBOARD} element={<AdminDashboard />} />
            <Route path={ROUTES.ADMIN.DEPARTMENTS} element={<Departments />} />
            <Route path={ROUTES.ADMIN.ROOMS} element={<Rooms />} />
            <Route path={ROUTES.ADMIN.USERS.STUDENTS} element={<Students />} />
            <Route path={ROUTES.ADMIN.USERS.TEACHERS} element={<Teachers />} />
            <Route path={ROUTES.ADMIN.USERS.COORDINATORS} element={<Coordinators />} />
            <Route path={ROUTES.ADMIN.AUDIT_LOGS} element={<AuditLogs />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={["coordinator"]} />}>
            <Route path={ROUTES.COORDINATOR.DASHBOARD} element={<CoordinatorDashboard />} />
            <Route
              path={ROUTES.COORDINATOR.SCHEDULE}
              element={<DefenseDesigner />}
            />
            <Route
              path={ROUTES.COORDINATOR.PROJECTS}
              element={<CoordinatorProjects />}
            />
            <Route path={ROUTES.COORDINATOR.JURIES} element={<Jurys />} />
            <Route path={ROUTES.COORDINATOR.DEFENSE_SESSIONS} element={<CoordinatorDefenseSessions />} />
            <Route path={ROUTES.COORDINATOR.CONFLICTS} element={<CoordinatorConflicts />} />
            <Route path={ROUTES.COORDINATOR.GRADES} element={<CoordinatorGrades />} />
            <Route path={ROUTES.COORDINATOR.DOCUMENTS} element={<CoordinatorDocuments />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={["teacher"]} />}>
            <Route path={ROUTES.TEACHER.DASHBOARD} element={<TeacherDashboard />} />
            <Route path={ROUTES.TEACHER.SCHEDULE} element={<TeacherSchedule />} />
            <Route path={ROUTES.TEACHER.EVALUATIONS} element={<TeacherEvaluations />} />
            <Route
              path={ROUTES.TEACHER.UNAVAILABILITY}
              element={<TeacherUnavailability />}
            />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={["student"]} />}>
            <Route path={ROUTES.STUDENT.DASHBOARD} element={<StudentDashboard />} />
            <Route path={ROUTES.STUDENT.GROUP} element={<StudentGroup />} />
            <Route path={ROUTES.STUDENT.DOCUMENTS} element={<StudentDocuments />} />
          </Route>

          <Route element={<ProtectedRoute allowedRoles={["admin", "coordinator", "teacher", "student"]} />}>
            <Route path={ROUTES.SHARED.PROFILE} element={<Profile />} />
            <Route path={ROUTES.SHARED.NOTIFICATIONS} element={<Notifications />} />
          </Route>
        </Route>

        <Route path="/" element={<Navigate to={ROUTES.AUTH.LOGIN} replace />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
    </ErrorBoundary>
  );
}
