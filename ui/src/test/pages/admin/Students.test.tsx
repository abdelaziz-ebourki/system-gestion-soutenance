import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";
import Students from "@/pages/admin/users/Students";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Students />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Students (Admin)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title and header", async () => {
    renderPage();
    expect(await screen.findByText("Étudiants")).toBeInTheDocument();
    expect(screen.getByTestId("admin-students-page")).toBeInTheDocument();
  });

  it("renders data in the table", async () => {
    renderPage();
    expect(await screen.findByText("Dupont")).toBeInTheDocument();
    expect(screen.getByText("Martin")).toBeInTheDocument();
  });

  it("renders status badges", async () => {
    renderPage();
    expect(await screen.findByText("Actif")).toBeInTheDocument();
    expect(screen.getByText("Inactif")).toBeInTheDocument();
  });

  it("renders level badge from level lookup", async () => {
    renderPage();
    expect(await screen.findByText("L3")).toBeInTheDocument();
    expect(screen.getByText("M1")).toBeInTheDocument();
  });

  it("shows empty state when majors are missing", async () => {
    server.use(
      http.get("*/api/admin/config/majors", () => HttpResponse.json([])),
    );
    renderPage();
    expect(await screen.findByText("Configuration requise")).toBeInTheDocument();
    expect(screen.getByText(/Vous devez d'abord configurer/)).toBeInTheDocument();
  });

  it("opens dialog on add button click", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderPage();
    expect(await screen.findByText("Dupont")).toBeInTheDocument();
    await user.click(screen.getByTestId("admin-students-add-button"));
    expect(screen.getByText("Ajouter Étudiant")).toBeInTheDocument();
  });

  it("submits create form successfully", { timeout: 15000 }, async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    const toast = await import("sonner");
    renderPage();
    expect(await screen.findByText("Dupont")).toBeInTheDocument();
    await user.click(screen.getByTestId("admin-students-add-button"));
    fireEvent.change(screen.getByLabelText("Nom"), { target: { value: "Test" } });
    fireEvent.change(screen.getByLabelText("Prénom"), { target: { value: "User" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "test@test.com" } });
    fireEvent.change(screen.getByLabelText("CNE"), { target: { value: "T123" } });
    await user.click(screen.getByRole("combobox", { name: /niveau/i }));
    await user.click(await screen.findByRole("option", { name: "L3" }));
    await user.click(screen.getByRole("combobox", { name: /filière/i }));
    await user.click(await screen.findByRole("option", { name: "Génie Informatique" }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => {
      expect(toast.toast.success).toHaveBeenCalledWith("Étudiant créé avec succès");
    });
  });

  it("shows error toast when create fails", { timeout: 15000 }, async () => {
    server.use(
      http.post("*/api/admin/users", () => HttpResponse.json({ message: "fail" }, { status: 500 })),
    );
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    const toast = await import("sonner");
    renderPage();
    expect(await screen.findByText("Dupont")).toBeInTheDocument();
    await user.click(screen.getByTestId("admin-students-add-button"));
    fireEvent.change(screen.getByLabelText("Nom"), { target: { value: "Test" } });
    fireEvent.change(screen.getByLabelText("Prénom"), { target: { value: "User" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "test@test.com" } });
    fireEvent.change(screen.getByLabelText("CNE"), { target: { value: "T123" } });
    await user.click(screen.getByRole("combobox", { name: /niveau/i }));
    await user.click(await screen.findByRole("option", { name: "L3" }));
    await user.click(screen.getByRole("combobox", { name: /filière/i }));
    await user.click(await screen.findByRole("option", { name: "Génie Informatique" }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => {
      expect(toast.toast.error).toHaveBeenCalledWith("Erreur serveur. Veuillez réessayer plus tard.");
    });
  });

  it("deletes a student via CrudActions", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderPage();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByTestId("crud-actions-delete"));
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(screen.queryByTestId("delete-alert")).not.toBeInTheDocument();
    });
  });

  it("shows error toast when delete fails", async () => {
    server.use(
      http.delete("*/api/admin/users/:id", () =>
        HttpResponse.json({ message: "Error" }, { status: 500 }),
      ),
    );
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    const toast = await import("sonner");
    renderPage();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByTestId("crud-actions-delete"));
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(toast.toast.error).toHaveBeenCalledWith("Erreur serveur. Veuillez réessayer plus tard.");
    });
  });

  it("cancels delete dialog", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderPage();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByTestId("crud-actions-delete"));
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /annuler/i }));
    await waitFor(() => {
      expect(screen.queryByTestId("delete-alert")).not.toBeInTheDocument();
    });
  });

  it("shows empty state when levels are missing", async () => {
    server.use(
      http.get("*/api/admin/config/levels", () => HttpResponse.json([])),
    );
    renderPage();
    expect(await screen.findByText("Configuration requise")).toBeInTheDocument();
    expect(screen.getByText(/niveaux/)).toBeInTheDocument();
  });
});
