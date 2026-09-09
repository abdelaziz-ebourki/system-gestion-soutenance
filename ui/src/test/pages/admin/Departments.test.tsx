import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Departments from "@/pages/admin/Departments";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderDepartments() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Departments />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Departments", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title", () => {
    renderDepartments();
    expect(screen.getByText("Départements")).toBeInTheDocument();
  });

  it("renders departments from API", async () => {
    renderDepartments();
    expect(await screen.findByText("Informatique")).toBeInTheDocument();
    expect(await screen.findByText("Mathématiques")).toBeInTheDocument();
  });

  it("renders department codes", async () => {
    renderDepartments();
    expect(await screen.findByText("INFO")).toBeInTheDocument();
    expect(await screen.findByText("MATH")).toBeInTheDocument();
  });

  it("opens create dialog when Nouveau Département is clicked", async () => {
    const user = userEvent.setup();
    renderDepartments();
    await user.click(screen.getByRole("button", { name: /nouveau département/i }));
    expect(await screen.findByText(/Ajouter Département/i)).toBeInTheDocument();
  });

  it("shows delete alert for batch deletion", async () => {
    const user = userEvent.setup();
    renderDepartments();
    const rowCheckboxes = await screen.findAllByRole("checkbox", { name: /select row/i });
    await user.click(rowCheckboxes[0]);
    await waitFor(() => expect(screen.getByTestId("batch-actions-bar")).toBeInTheDocument());
    const deleteButton = await screen.findByRole("button", { name: /supprimer/i });
    await user.click(deleteButton);
    expect(screen.getByText(/Confirmation/)).toBeInTheDocument();
  });

    it("opens create dialog and submits", async () => {
      const user = userEvent.setup();
      renderDepartments();
      await user.click(screen.getByRole("button", { name: /nouveau département/i }));
      expect(await screen.findByText(/Ajouter Département/i)).toBeInTheDocument();
      await user.type(screen.getByPlaceholderText(/ex: Informatique/i), "Physique");
      await user.type(screen.getByPlaceholderText("ex: INFO"), "PHY");
      await user.click(screen.getByRole("button", { name: /enregistrer/i }));
      await waitFor(() => {
        expect(screen.queryByText(/Ajouter Département/i)).not.toBeInTheDocument();
      });
    });

  it("opens single delete dialog via CrudActions", async () => {
    const user = userEvent.setup();
    renderDepartments();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /supprimer/i }));
    expect(await screen.findByText(/Cette action est irréversible/i)).toBeInTheDocument();
  });
});
