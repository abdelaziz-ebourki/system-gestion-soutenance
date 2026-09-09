import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Students from "@/pages/admin/users/Students";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderStudents() {
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

describe("Students", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders students from API when majors and levels exist", async () => {
    renderStudents();
    expect(await screen.findByText("CNE001")).toBeInTheDocument();
    expect(await screen.findByText("Dupont")).toBeInTheDocument();
    expect(await screen.findByText("Jean")).toBeInTheDocument();
    expect(await screen.findByText("CNE002")).toBeInTheDocument();
  });

  it("renders student emails", async () => {
    renderStudents();
    expect(await screen.findByText("jean.dupont@example.com")).toBeInTheDocument();
    expect(await screen.findByText("sophie.martin@example.com")).toBeInTheDocument();
  });

  it("renders major names from config", async () => {
    renderStudents();
    expect(await screen.findByText("Génie Informatique")).toBeInTheDocument();
  });

  it("renders level badges", async () => {
    renderStudents();
    expect(await screen.findByText("L3")).toBeInTheDocument();
  });

  it("opens create dialog when Nouvel Étudiant is clicked", async () => {
    const user = userEvent.setup();
    renderStudents();
    await screen.findByText("CNE001");
    await user.click(screen.getByRole("button", { name: /nouvel étudiant/i }));
    expect(await screen.findByText(/Ajouter Étudiant/i)).toBeInTheDocument();
  });

  it("shows batch actions bar when students are selected", async () => {
    const user = userEvent.setup();
    renderStudents();
    const checkboxes = await screen.findAllByRole("checkbox");
    const firstRowCheckbox = checkboxes[1];
    await user.click(firstRowCheckbox);
    expect(screen.getByText(/1 étudiant\(s\) sélectionné\(s\)/i)).toBeInTheDocument();
  });
});
