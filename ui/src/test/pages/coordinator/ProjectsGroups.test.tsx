import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import CoordinatorProjects from "@/pages/coordinator/ProjectsGroups";
import { server } from "@/test/mocks/server";
import { http, HttpResponse } from "msw";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderProjects() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <CoordinatorProjects />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("ProjectsGroups (Coordinator)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title and header", async () => {
    renderProjects();
    expect(await screen.findByText("Projets & Groupes")).toBeInTheDocument();
    expect(screen.getByTestId("coord-projects-page")).toBeInTheDocument();
  });

  it("renders stats cards", async () => {
    renderProjects();
    expect(await screen.findByText("2")).toBeInTheDocument();
    expect(screen.getByText("Portefeuille")).toBeInTheDocument();
    expect(screen.getByText("Groupes complets")).toBeInTheDocument();
  });

  it("renders the add project button", async () => {
    renderProjects();
    expect(await screen.findByTestId("coord-projects-add-button")).toBeInTheDocument();
  });

  it("renders project table with data", async () => {
    renderProjects();
    const projectTitles = await screen.findAllByText(/Application CI\/CD|Analyse des données/);
    expect(projectTitles.length).toBeGreaterThanOrEqual(1);
  });

  it("renders student groups section with assigned and unassigned groups", async () => {
    renderProjects();
    expect(await screen.findByText("Groupes étudiants")).toBeInTheDocument();
    expect(await screen.findByText("Groupe Alpha")).toBeInTheDocument();
    expect(screen.getByText("Groupe Beta")).toBeInTheDocument();
    expect(screen.getByTestId("coord-projects-groups-section")).toBeInTheDocument();
  });

  it("renders assign button for unassigned groups", async () => {
    renderProjects();
    expect(await screen.findByText("Groupe Beta")).toBeInTheDocument();
    expect(screen.getByTestId("coord-projects-assign-2")).toBeInTheDocument();
  });

  it("shows loading skeleton when projects are loading", async () => {
    server.use(
      http.get("*/api/coordinator/projects", async () => {
        await new Promise(() => {});
        return HttpResponse.json([]);
      }),
    );
    renderProjects();
    expect(screen.getByTestId("coord-projects-page")).toBeInTheDocument();
    const skeleton = document.querySelector('[data-slot="skeleton"]');
    expect(skeleton).toBeInTheDocument();
  });

  it("shows empty project portfolio", async () => {
    server.use(
      http.get("*/api/coordinator/projects", () => HttpResponse.json([])),
    );
    renderProjects();
    expect(await screen.findByTestId("coord-projects-page")).toBeInTheDocument();
    const zeros = screen.getAllByText("0");
    expect(zeros.length).toBeGreaterThanOrEqual(1);
  });

  it("calls delete mutation when CrudActions delete is confirmed", async () => {
    const user = userEvent.setup();
    renderProjects();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /supprimer/i }));
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(screen.queryByTestId("delete-alert")).not.toBeInTheDocument();
    });
  });

  it("shows error toast when delete fails", async () => {
    server.use(
      http.delete("*/api/coordinator/projects/:id", () => HttpResponse.json({ message: "Erreur serveur" }, { status: 500 })),
    );
    const user = userEvent.setup();
    const toast = await import("sonner");
    renderProjects();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /supprimer/i }));
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(toast.toast.error).toHaveBeenCalledWith("Erreur serveur. Veuillez réessayer plus tard.");
    });
  });

  it("opens ProjectDialog on add button click", async () => {
    const user = userEvent.setup();
    renderProjects();
    const addBtn = await screen.findByTestId("coord-projects-add-button");
    await user.click(addBtn);
    expect(await screen.findByTestId("coord-project-dialog")).toBeInTheDocument();
    expect(screen.getByTestId("coord-project-dialog-form")).toBeInTheDocument();
    expect(screen.getByText("Nouveau projet")).toBeInTheDocument();
  });

  it("closes ProjectDialog via cancel", async () => {
    const user = userEvent.setup();
    renderProjects();
    const addBtn = await screen.findByTestId("coord-projects-add-button");
    await user.click(addBtn);
    expect(await screen.findByTestId("coord-project-dialog")).toBeInTheDocument();
    await user.click(screen.getByTestId("coord-project-dialog-cancel"));
    await waitFor(() => {
      expect(screen.queryByTestId("coord-project-dialog")).not.toBeInTheDocument();
    });
  });

  it("opens AssignProjectDialog on assign button click", async () => {
    const user = userEvent.setup();
    renderProjects();
    const assignBtn = await screen.findByTestId("coord-projects-assign-2");
    await user.click(assignBtn);
    expect(await screen.findByTestId("coord-assign-project-dialog")).toBeInTheDocument();
    expect(screen.getByTestId("coord-assign-project-dialog")).toHaveTextContent("Assigner un projet");
  });

  it("closes AssignProjectDialog via cancel", async () => {
    const user = userEvent.setup();
    renderProjects();
    const assignBtn = await screen.findByTestId("coord-projects-assign-2");
    await user.click(assignBtn);
    expect(await screen.findByTestId("coord-assign-project-dialog")).toBeInTheDocument();
    await user.click(screen.getByTestId("coord-assign-project-cancel"));
    await waitFor(() => {
      expect(screen.queryByTestId("coord-assign-project-dialog")).not.toBeInTheDocument();
    });
  });
});
