import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { toast } from "sonner";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";
import AdminDashboard from "@/pages/admin/AdminDashboard";

vi.mock("recharts", async (importOriginal) => {
  const actual = await importOriginal<typeof import("recharts")>();
  return {
    ...actual,
    ResponsiveContainer: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    BarChart: ({ children }: { children: React.ReactNode }) => <div data-testid="mock-barchart">{children}</div>,
    Bar: () => <div data-testid="mock-bar" />,
    XAxis: () => <div data-testid="mock-xaxis" />,
    CartesianGrid: () => <div data-testid="mock-grid" />,
    Tooltip: () => <div data-testid="mock-tooltip" />,
    ChartTooltip: () => <div data-testid="mock-tooltip" />,
    Legend: () => <div data-testid="mock-legend" />,
  };
});


vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderDashboard() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("AdminDashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title", () => {
    renderDashboard();
    expect(screen.getByText("Tableau de Bord")).toBeInTheDocument();
  });

  it("renders stat cards with values from API", async () => {
    renderDashboard();
    expect(await screen.findByText("100")).toBeInTheDocument();
    expect(await screen.findByText("20")).toBeInTheDocument();
    expect(await screen.findByText("5")).toBeInTheDocument();
    expect(await screen.findByText("15")).toBeInTheDocument();
  });

  it("renders the users table with data from API", async () => {
    renderDashboard();
    expect(await screen.findByText("jean.dupont@example.com")).toBeInTheDocument();
    expect(await screen.findByText("ahmed.benali@example.com")).toBeInTheDocument();
  });

  it("renders the notifications card", async () => {
    renderDashboard();
    expect(await screen.findByText("Notifications")).toBeInTheDocument();
  });

  it("shows batch bar when users are selected", async () => {
    const user = userEvent.setup();
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    expect(await screen.findByText(/utilisateur\(s\) sélectionné\(s\)/i)).toBeInTheDocument();
  });

  it("opens batch role change dialog", async () => {
    const user = userEvent.setup();
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const roleButtons = await screen.findAllByRole("button", { name: /changer le rôle/i });
    await user.click(roleButtons[0]);
    await waitFor(() => {
      expect(screen.getByText("Choisir...")).toBeInTheDocument();
    });
  });

  it("deletes selected users via batch delete button", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const deleteButtons = await screen.findAllByRole("button", { name: /supprimer/i });
    await user.click(deleteButtons[0]);
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(toast.success).toHaveBeenCalled();
    });
  });

  it("shows error toast when batch delete fails", async () => {
    server.use(
      http.delete("*/api/admin/users/:id", () =>
        HttpResponse.json({ message: "Error" }, { status: 500 }),
      ),
    );
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const deleteButtons = await screen.findAllByRole("button", { name: /supprimer/i });
    await user.click(deleteButtons[0]);
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Erreur serveur. Veuillez réessayer plus tard.");
    });
  });

  it("cancels delete alert without deleting", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const deleteButtons = await screen.findAllByRole("button", { name: /supprimer/i });
    await user.click(deleteButtons[0]);
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /annuler/i }));
    await waitFor(() => {
      expect(screen.queryByTestId("delete-alert")).not.toBeInTheDocument();
    });
  });

  it("closes role dialog via cancel button", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const roleButtons = await screen.findAllByRole("button", { name: /changer le rôle/i });
    await user.click(roleButtons[0]);
    expect(screen.getByText("Choisir...")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /annuler/i }));
    await waitFor(() => {
      expect(screen.queryByText("Choisir...")).not.toBeInTheDocument();
    });
  });

  it("does not save role when no value selected", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const roleButtons = await screen.findAllByRole("button", { name: /changer le rôle/i });
    await user.click(roleButtons[0]);
    expect(screen.getByText("Choisir...")).toBeInTheDocument();
    await user.click(screen.getAllByRole("button", { name: /enregistrer/i }).at(-1)!);
    expect(toast.success).not.toHaveBeenCalled();
  });

  it("saves batch role change successfully", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const roleButtons = await screen.findAllByRole("button", { name: /changer le rôle/i });
    await user.click(roleButtons[0]);
    await user.click(screen.getByRole("combobox"));
    const option = await screen.findByRole("option", { name: /coordinateur/i });
    await user.click(option);
    await user.click(screen.getAllByRole("button", { name: /enregistrer/i }).at(-1)!);
    await waitFor(() => {
      expect(toast.success).toHaveBeenCalled();
    });
  });

  it("shows error toast when batch role change fails", async () => {
    server.use(
      http.put("*/api/admin/users/:id", () =>
        new HttpResponse(null, { status: 500 }),
      ),
    );
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderDashboard();
    const rowCheckbox = (await screen.findAllByRole("checkbox", { name: /select row/i }))[0];
    await user.click(rowCheckbox);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const roleButtons = await screen.findAllByRole("button", { name: /changer le rôle/i });
    await user.click(roleButtons[0]);
    await user.click(screen.getByRole("combobox"));
    const option = await screen.findByRole("option", { name: /coordinateur/i });
    await user.click(option);
    await user.click(screen.getAllByRole("button", { name: /enregistrer/i }).at(-1)!);
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Erreur serveur. Veuillez réessayer plus tard.");
    });
  });
});
