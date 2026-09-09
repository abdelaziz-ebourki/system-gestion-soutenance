import { describe, it, expect } from "vitest";
import { screen, render } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "@/contexts/auth-context";
import { TooltipProvider } from "@/components/ui/tooltip";
import DashboardLayout from "@/components/layout/DashboardLayout";
import ProtectedRoute from "@/components/auth/ProtectedRoute";
import AdminDashboard from "@/pages/admin/AdminDashboard";
import Login from "@/pages/Login";
import { ROUTES } from "@/config/routes";

function renderRoute(initialEntries: string[], authUser?: Record<string, unknown>) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false, gcTime: 0 }, mutations: { retry: false } },
  });

  if (authUser) {
    localStorage.setItem("user", JSON.stringify(authUser));
    localStorage.setItem("token", "mock-test-token");
  } else {
    localStorage.clear();
  }

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={initialEntries}>
        <AuthProvider>
          <TooltipProvider>
            <Routes>
              <Route path={ROUTES.AUTH.LOGIN} element={<Login />} />
              <Route element={<DashboardLayout />}>
                <Route element={<ProtectedRoute allowedRoles={["admin"]} />}>
                  <Route path={ROUTES.ADMIN.DASHBOARD} element={<AdminDashboard />} />
                </Route>
              </Route>
            </Routes>
          </TooltipProvider>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Infrastructure Validation", () => {
  it("should redirect unauthenticated user to login page", async () => {
    renderRoute([ROUTES.ADMIN.DASHBOARD]);

    expect(await screen.findByText(/connexion/i)).toBeInTheDocument();
  });

  it("should allow admin user to access admin dashboard", async () => {
    renderRoute([ROUTES.ADMIN.DASHBOARD], {
      id: 1, firstName: "Admin", lastName: "User", email: "admin@univh2c.ma", role: "admin", isActive: true,
    });

    expect(
      await screen.findByRole("heading", { name: /Tableau de Bord/i }),
    ).toBeInTheDocument();
  });

  it("should redirect student user away from admin dashboard", async () => {
    renderRoute([ROUTES.ADMIN.DASHBOARD], {
      id: 2, firstName: "Student", lastName: "User", email: "student@univh2c.ma", role: "student", isActive: true,
    });

    const dashboardHeading = screen.queryByText(/Tableau de bord Administrateur/i);
    expect(dashboardHeading).not.toBeInTheDocument();
  });
});
