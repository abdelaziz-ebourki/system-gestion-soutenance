import { useAuditLogs } from "@/hooks/queries";
import type { AuditLog } from "@/types/audit-log";
import type { ColumnDef, PaginationState } from "@tanstack/react-table";
import { DataTable } from "@/components/ui/data-table";
import { PageHeader } from "@/components/ui/page-header";
import { useState } from "react";
import { AUDIT_LOG_PAGE_SIZE } from "@/lib/constants";

export default function AuditLogs() {
  const [pagination, setPagination] = useState<PaginationState>({ pageIndex: 0, pageSize: AUDIT_LOG_PAGE_SIZE });
  const { data, isLoading } = useAuditLogs(pagination.pageIndex, pagination.pageSize);

  const columns: ColumnDef<AuditLog>[] = [
    { accessorKey: "action", header: "Action" },
    { accessorKey: "entity", header: "Entité" },
    { accessorKey: "performedByEmail", header: "Utilisateur" },
    { accessorKey: "timestamp", header: "Date" },
  ];

  return (
    <div className="flex flex-col gap-6" data-testid="admin-audit-logs-page">
      <PageHeader title="Journal d'audit" subtitle="Consultez l'historique des actions administratives." />

      <DataTable
        columns={columns}
        data={data?.items ?? []}
        loading={isLoading}
        getRowId={(row) => String(row.id)}
        filterColumns="action"
        filterPlaceholder="Rechercher une action..."
        manualPagination
        pageCount={data?.pageCount ?? 0}
        pagination={pagination}
        onPaginationChange={setPagination}
      />
    </div>
  );
}

