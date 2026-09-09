import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";
import Teachers from "@/pages/admin/users/Teachers";

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
        <Teachers />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Teachers (Admin)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title and header", async () => {
    renderPage();
    expect(await screen.findByText("Enseignants")).toBeInTheDocument();
    expect(screen.getByTestId("admin-teachers-page")).toBeInTheDocument();
  });

  it("renders data in the table", async () => {
    renderPage();
    expect(await screen.findByText("Benali")).toBeInTheDocument();
    expect(screen.getByText("Amrani")).toBeInTheDocument();
  });

  it("renders status badges", async () => {
    renderPage();
    expect(await screen.findByText("Actif")).toBeInTheDocument();
    expect(screen.getByText("Inactif")).toBeInTheDocument();
  });

  it("renders department name from lookup", async () => {
    renderPage();
    expect(await screen.findByText("Informatique")).toBeInTheDocument();
    expect(screen.getByText("Mathématiques")).toBeInTheDocument();
  });

  it("shows empty state when departments are missing", async () => {
    server.use(
      http.get("*/api/admin/departments", () => HttpResponse.json([])),
    );
    renderPage();
    expect(await screen.findByText("Configuration requise")).toBeInTheDocument();
    expect(screen.getByText(/Vous devez d'abord configurer les départements/)).toBeInTheDocument();
  });

  it("opens dialog on add button click", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    renderPage();
    expect(await screen.findByText("Benali")).toBeInTheDocument();
    await user.click(screen.getByTestId("admin-teachers-add-button"));
    expect(screen.getByText("Ajouter Enseignant")).toBeInTheDocument();
  });

  it("submits create form successfully", { timeout: 15000 }, async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    const toast = await import("sonner");
    renderPage();
    expect(await screen.findByText("Benali")).toBeInTheDocument();
    await user.click(screen.getByTestId("admin-teachers-add-button"));
    fireEvent.change(screen.getByLabelText("Nom"), { target: { value: "Test" } });
    fireEvent.change(screen.getByLabelText("Prénom"), { target: { value: "User" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "test@test.com" } });
    await user.click(screen.getByRole("combobox", { name: /département/i }));
    await user.click(await screen.findByRole("option", { name: "Informatique" }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => {
      expect(toast.toast.success).toHaveBeenCalledWith("Enseignant créé avec succès");
    });
  });

  it("shows error toast when create fails", { timeout: 15000 }, async () => {
    server.use(
      http.post("*/api/admin/users", () => HttpResponse.json({ message: "fail" }, { status: 500 })),
    );
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    const toast = await import("sonner");
    renderPage();
    expect(await screen.findByText("Benali")).toBeInTheDocument();
    await user.click(screen.getByTestId("admin-teachers-add-button"));
    fireEvent.change(screen.getByLabelText("Nom"), { target: { value: "Test" } });
    fireEvent.change(screen.getByLabelText("Prénom"), { target: { value: "User" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "test@test.com" } });
    await user.click(screen.getByRole("combobox", { name: /département/i }));
    await user.click(await screen.findByRole("option", { name: "Informatique" }));
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => {
      expect(toast.toast.error).toHaveBeenCalledWith("Erreur serveur. Veuillez réessayer plus tard.");
    });
  });

  it("deletes a teacher via CrudActions", async () => {
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

  it("shows description text on page", async () => {
    renderPage();
    expect(await screen.findByText("Gestion du corps enseignant.")).toBeInTheDocument();
  });
});
