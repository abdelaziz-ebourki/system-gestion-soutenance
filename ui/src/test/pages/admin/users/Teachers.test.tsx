import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Teachers from "@/pages/admin/users/Teachers";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderTeachers() {
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

describe("Teachers", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders teachers from API when departments exist", async () => {
    renderTeachers();
    expect(await screen.findByText("Benali")).toBeInTheDocument();
    expect(await screen.findByText("Ahmed")).toBeInTheDocument();
    expect(await screen.findByText("Amrani")).toBeInTheDocument();
    expect(await screen.findByText("Fatima")).toBeInTheDocument();
  });

  it("renders teacher emails", async () => {
    renderTeachers();
    expect(await screen.findByText("ahmed.benali@example.com")).toBeInTheDocument();
    expect(await screen.findByText("fatima.amrani@example.com")).toBeInTheDocument();
  });

  it("renders department names", async () => {
    renderTeachers();
    expect(await screen.findByText("Informatique")).toBeInTheDocument();
    expect(await screen.findByText("Mathématiques")).toBeInTheDocument();
  });

  it("renders active/inactive badges", async () => {
    renderTeachers();
    const actifs = await screen.findAllByText("Actif");
    expect(actifs.length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText("Inactif")).toBeInTheDocument();
  });

  it("opens create dialog when Nouvel Enseignant is clicked", async () => {
    const user = userEvent.setup();
    renderTeachers();
    await screen.findByText("Benali");
    await user.click(screen.getByRole("button", { name: /nouvel enseignant/i }));
    expect(await screen.findByText(/Ajouter Enseignant/i)).toBeInTheDocument();
  });

  it("shows batch actions bar when teachers are selected", async () => {
    const user = userEvent.setup();
    renderTeachers();
    const checkboxes = await screen.findAllByRole("checkbox");
    const firstRowCheckbox = checkboxes[1];
    await user.click(firstRowCheckbox);
    expect(screen.getByText(/1 enseignant\(s\) sélectionné\(s\)/i)).toBeInTheDocument();
  });
});
