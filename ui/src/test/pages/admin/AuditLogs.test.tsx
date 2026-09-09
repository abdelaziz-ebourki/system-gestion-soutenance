import { screen } from "@testing-library/react";
import { describe, it, expect, beforeEach } from "vitest";
import AuditLogs from "@/pages/admin/AuditLogs";
import { renderWithProviders } from "@/test/utils";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";

const adminUser = {
  id: 1,
  email: "admin@univh2c.ma",
  firstName: "Admin",
  lastName: "User",
  role: "admin" as const,
  isActive: true,
};

describe("AuditLogs", () => {
  beforeEach(() => {
    server.resetHandlers();
  });

  it("renders the audit logs page with title", async () => {
    renderWithProviders(<AuditLogs />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByTestId("admin-audit-logs-page")).toBeInTheDocument();
    expect(screen.getByText("Journal d'audit")).toBeInTheDocument();
  });

  it("renders audit log entries in the table", async () => {
    renderWithProviders(<AuditLogs />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByText("LOGIN")).toBeInTheDocument();
    expect(await screen.findByText("CREATE")).toBeInTheDocument();
    expect(screen.getAllByText("admin@univh2c.ma")).toHaveLength(2);
  });

  it("renders table columns", async () => {
    renderWithProviders(<AuditLogs />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByText("Action")).toBeInTheDocument();
    expect(screen.getByText("Entité")).toBeInTheDocument();
    expect(screen.getByText("Utilisateur")).toBeInTheDocument();
    expect(screen.getByText("Date")).toBeInTheDocument();
  });

  it("shows empty state when no logs", async () => {
    server.use(
      http.get("*/api/admin/audit-logs", () =>
        HttpResponse.json({ items: [], total: 0, pageCount: 0 }),
      ),
    );
    renderWithProviders(<AuditLogs />, {
      initialAuthState: { user: adminUser },
    });
    expect(await screen.findByTestId("admin-audit-logs-page")).toBeInTheDocument();
  });
});
