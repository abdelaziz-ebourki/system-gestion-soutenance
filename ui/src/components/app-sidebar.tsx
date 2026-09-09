
import { siteConfig } from "@/config/site";
import * as React from "react";
import { NavMain } from "@/components/nav-main";
import { NavUser } from "@/components/nav-user";
import { Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarMenu, SidebarMenuButton, SidebarMenuItem } from "@/components/ui/sidebar";
import { LayoutDashboardIcon, UsersIcon, Settings2Icon, CalendarIcon, BookOpenIcon, GraduationCapIcon, FileTextIcon, BuildingIcon, HistoryIcon, University, AlertTriangle, FileCheck2 } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { ROUTES } from "@/config/routes";

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { user } = useAuth();
  const displayUser = user ?? {
    lastName: "Guest",
    firstName: "",
    email: "",
    role: "guest" as const,
    avatar: "",
  };

  const getNavItems = (role: string) => {
    switch (role) {
      case "admin":
        return [
          { title: "Tableau de bord", url: ROUTES.ADMIN.DASHBOARD, icon: <LayoutDashboardIcon /> },
          {
            title: "Départements",
            url: ROUTES.ADMIN.DEPARTMENTS,
            icon: <University />,
          },
          {
            title: "Salles",
            url: ROUTES.ADMIN.ROOMS,
            icon: <BuildingIcon />,
          },
          {
            title: "Étudiants",
            url: ROUTES.ADMIN.USERS.STUDENTS,
            icon: <GraduationCapIcon />,
          },
          {
            title: "Enseignants",
            url: ROUTES.ADMIN.USERS.TEACHERS,
            icon: <BookOpenIcon />,
          },
          {
            title: "Coordinateurs",
            url: ROUTES.ADMIN.USERS.COORDINATORS,
            icon: <UsersIcon />,
          },
          {
            title: "Filières & Niveaux",
            url: ROUTES.ADMIN.CONFIG,
            icon: <Settings2Icon />,
          },
          {
            title: "Journal d'audit",
            url: ROUTES.ADMIN.AUDIT_LOGS,
            icon: <HistoryIcon />,
          },
        ];
      case "coordinator":
        return [
          {
            title: "Dashboard",
            url: ROUTES.COORDINATOR.DASHBOARD,
            icon: <LayoutDashboardIcon />,
          },
          {
            title: "Projets & Groupes",
            url: ROUTES.COORDINATOR.PROJECTS,
            icon: <BookOpenIcon />,
          },
          {
            title: "Planification",
            url: ROUTES.COORDINATOR.SCHEDULE,
            icon: <CalendarIcon />,
          },
          { title: "Jurys", url: ROUTES.COORDINATOR.JURIES, icon: <UsersIcon /> },
          {
            title: "Sessions",
            url: ROUTES.COORDINATOR.DEFENSE_SESSIONS,
            icon: <GraduationCapIcon />,
          },
          {
            title: "Conflits",
            url: ROUTES.COORDINATOR.CONFLICTS,
            icon: <AlertTriangle />,
          },
          {
            title: "Notes",
            url: ROUTES.COORDINATOR.GRADES,
            icon: <FileCheck2 />,
          },
          {
            title: "Documents",
            url: ROUTES.COORDINATOR.DOCUMENTS,
            icon: <FileTextIcon />,
          },
        ];
      case "teacher":
        return [
          {
            title: "Mon Dashboard",
            url: ROUTES.TEACHER.DASHBOARD,
            icon: <LayoutDashboardIcon />,
          },
          {
            title: "Mon Planning",
            url: ROUTES.TEACHER.SCHEDULE,
            icon: <CalendarIcon />,
          },
          {
            title: "Évaluations",
            url: ROUTES.TEACHER.EVALUATIONS,
            icon: <GraduationCapIcon />,
          },
          {
            title: "Mes Indisponibilités",
            url: ROUTES.TEACHER.UNAVAILABILITY,
            icon: <HistoryIcon />,
          },
        ];
      case "student":
        return [
          {
            title: "Ma Soutenance",
            url: ROUTES.STUDENT.DASHBOARD,
            icon: <GraduationCapIcon />,
          },
          { title: "Mon Groupe", url: ROUTES.STUDENT.GROUP, icon: <UsersIcon /> },
          {
            title: "Documents",
            url: ROUTES.STUDENT.DOCUMENTS,
            icon: <FileTextIcon />,
          },
        ];
      default:
        return [];
    }
  };

  return (
    <Sidebar collapsible="offcanvas" data-testid="app-sidebar" {...props}>
      <SidebarHeader className="py-4">
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              className="data-[slot=sidebar-menu-button]:p-1.5!"
              asChild
            >
              <a href="/" className="flex items-center gap-3">
                <span className="truncate font-semibold text-lg tracking-tight">
                  {siteConfig.name}
                </span>
              </a>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <NavMain items={getNavItems(displayUser.role)} />
      </SidebarContent>
      <SidebarFooter>
        <NavUser user={displayUser} />
      </SidebarFooter>
    </Sidebar>
  );
}
