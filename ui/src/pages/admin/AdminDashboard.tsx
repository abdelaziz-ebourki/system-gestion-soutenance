import * as React from "react";
import { type ColumnDef, type PaginationState } from "@tanstack/react-table";
import {
  Users,
  GraduationCap,
  Building2,
  DoorOpen,
  Info,
  AlertTriangle,
  CheckCircle,
  XCircle,
} from "lucide-react";
import { Bar, BarChart, CartesianGrid, XAxis } from "recharts";
import { format, parseISO } from "date-fns";
import { fr } from "date-fns/locale";
import { Link } from "react-router-dom";

import { useAdminStats, useUsers, useUpdateUser, useDeleteUser, useNotifications } from "@/hooks/queries";
import { ROUTES } from "@/config/routes";
import { DEFAULT_API_LIMIT, MAX_TEACHER_FETCH_LIMIT } from "@/lib/constants";
import type { User } from "@/types";
import { DataTable } from "@/components/ui/data-table";
import { PageHeader } from "@/components/ui/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { StatsCard } from "@/components/ui/stats-card";
import { BatchActionsBar } from "@/components/admin/BatchActionsBar";
import {
  type ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { cn } from "@/lib/utils";

const userColumns: ColumnDef<User>[] = [
  {
    accessorKey: "lastName",
    header: "Nom",
  },
  {
    accessorKey: "firstName",
    header: "Prénom",
  },
  {
    accessorKey: "email",
    header: "Email",
  },
  {
    accessorKey: "role",
    header: "Rôle",
    cell: ({ row }) => (
      <Badge variant="outline" className="capitalize">
        {row.getValue("role")}
      </Badge>
    ),
  },
];

const typeIcons: Record<string, typeof Info> = {
  info: Info,
  warning: AlertTriangle,
  success: CheckCircle,
  error: XCircle,
};

const typeColors: Record<string, string> = {
  info: "bg-primary/10 text-primary",
  warning: "bg-primary/10 text-primary",
  success: "bg-primary/10 text-primary",
  error: "bg-destructive/10 text-destructive",
};

const formatTimestamp = (ts: string) => {
  try {
    return format(parseISO(ts), "dd MMM 'à' HH:mm", { locale: fr });
  } catch {
    return ts;
  }
};

export default function AdminDashboard() {
  const [pagination, setPagination] = React.useState<PaginationState>({
    pageIndex: 0,
    pageSize: DEFAULT_API_LIMIT,
  });
  const [isFiltering, setIsFiltering] = React.useState(false);

  const { data: stats } = useAdminStats();
  const { data: notificationsData } = useNotifications();
  const notifications = notificationsData?.items ?? [];
  const { data: usersData, isLoading: isLoading } = useUsers({
    page: isFiltering ? 0 : pagination.pageIndex,
    limit: isFiltering ? MAX_TEACHER_FETCH_LIMIT : pagination.pageSize,
  });
  const updateUser = useUpdateUser();
  const deleteUser = useDeleteUser();
  const [selectedUsers, setSelectedUsers] = React.useState<User[]>([]);

  const users = usersData?.items ?? [];
  const pageCount = usersData?.pageCount ?? 0;


  const chartData = React.useMemo(() => [
    { name: "Étudiants", total: stats?.totalStudents || 0 },
    { name: "Enseignants", total: stats?.totalTeachers || 0 },
    { name: "Départements", total: stats?.totalDepartments || 0 },
    { name: "Salles", total: stats?.totalRooms || 0 },
  ], [stats]);

  const chartConfig = React.useMemo(() => ({
    total: { label: "Total", color: "var(--primary)" },
  } satisfies ChartConfig), []);

  return (
    <div className="flex flex-col gap-6 pb-20">
      <PageHeader title="Tableau de Bord" subtitle="Aperçu global de l'activité du système." />

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard label="Étudiants" value={stats?.totalStudents} icon={Users} />
        <StatsCard label="Enseignants" value={stats?.totalTeachers} icon={GraduationCap} />
        <StatsCard label="Départements" value={stats?.totalDepartments} icon={Building2} />
        <StatsCard label="Salles" value={stats?.totalRooms} icon={DoorOpen} />
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>Distribution des Ressources</CardTitle>
          </CardHeader>
          <CardContent className="pl-2">
            {stats ? (
              <ChartContainer config={chartConfig} className="min-h-75 w-full">
                <BarChart data={chartData}>
                  <CartesianGrid vertical={false} />
                  <XAxis
                    dataKey="name"
                    tickLine={false}
                    tickMargin={10}
                    axisLine={false}
                  />
                  <ChartTooltip content={<ChartTooltipContent />} />
                  <Bar dataKey="total" fill="var(--color-total)" radius={8} />
                </BarChart>
              </ChartContainer>
            ) : (
              <Skeleton className="h-75 w-full" />
            )}
          </CardContent>
        </Card>

        <Card className="col-span-3">
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Notifications</CardTitle>
            <Button variant="ghost" size="sm" asChild>
              <Link to={ROUTES.SHARED.NOTIFICATIONS}>Voir tout</Link>
            </Button>
          </CardHeader>
          <CardContent>
            {notifications.length === 0 ? (
              <EmptyState icon={Info} description="Aucune notification" />
            ) : (
              <div className="flex flex-col gap-3">
                {notifications.slice(0, 4).map((notification) => {
                  const Icon = typeIcons[notification.type] || Info;
                  return (
                    <div key={notification.id} className="flex items-start gap-3">
                      <div className={cn("mt-0.5 rounded-full p-1.5", typeColors[notification.type])}>
                        <Icon />
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-1.5">
                          <p className="truncate text-sm font-medium">{notification.title}</p>
                          {!notification.read && (
                            <Badge variant="default" className="size-1.5 rounded-full p-0" />
                          )}
                        </div>
                        <p className="truncate text-xs text-muted-foreground">{notification.message}</p>
                        <span className="text-xs text-muted-foreground">{formatTimestamp(notification.timestamp)}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Utilisateurs</CardTitle>
        </CardHeader>
        <CardContent>
             <DataTable
               columns={userColumns}
               data={users}
               loading={isLoading}
               getRowId={(row) => row.id}
               enableRowSelection
               onSelectedRowsChange={setSelectedUsers}
               manualPagination={!isFiltering}
               pageCount={!isFiltering ? pageCount : undefined}
               pagination={!isFiltering ? pagination : undefined}
               onPaginationChange={!isFiltering ? setPagination : undefined}
               onFiltering={setIsFiltering}
               filterColumns={["lastName", "firstName", "email"]}
               filterPlaceholder="Rechercher par nom, prénom ou email..."
               filters={[
                 { column: "role", label: "Rôle", options: [{ value: "admin", label: "Admin" }, { value: "coordinator", label: "Coordinateur" }, { value: "teacher", label: "Enseignant" }, { value: "student", label: "Étudiant" }] },
               ]}
             />
             
             <BatchActionsBar
               selectedCount={selectedUsers.length}
               entityLabel="utilisateur(s)"
               actions={[{ key: "role", label: "Changer le rôle" }, { key: "delete", label: "Supprimer" }]}
               fieldOptionsMap={{
                 role: [{ value: "coordinator", label: "Coordinateur" }, { value: "teacher", label: "Enseignant" }, { value: "student", label: "Étudiant" }],
               }}
                onUpdateField={async (field, value) => {
                  if (field === "role") {
                    await Promise.all(selectedUsers.map((u) => updateUser.mutateAsync({ id: u.id, data: { lastName: u.lastName, firstName: u.firstName, email: u.email, role: value } })));
                  }
                }}
               onDeleteSelected={async () => {
                 await Promise.all(selectedUsers.map((u) => deleteUser.mutateAsync(u.id)));
               }}
               isPending={updateUser.isPending || deleteUser.isPending}
               onClearSelection={() => setSelectedUsers([])}
             />
             
         </CardContent>
      </Card>
    </div>
  );
}
