import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Grades from "@/pages/coordinator/Grades";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderGrades() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Grades />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Grades (Coordinator)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title", () => {
    renderGrades();
    expect(screen.getByText("Notes")).toBeInTheDocument();
  });

  it("renders stat cards", async () => {
    renderGrades();
    expect(await screen.findByText("Projets notés")).toBeInTheDocument();
    expect(await screen.findByText("En attente")).toBeInTheDocument();
    expect(await screen.findByText("Non évalués")).toBeInTheDocument();
  });

  it("displays completed grade with final score", async () => {
    renderGrades();
    expect(await screen.findByText("Application CI/CD")).toBeInTheDocument();
    expect(await screen.findByText("16.5/20")).toBeInTheDocument();
  });

  it("displays pending grade without final score", async () => {
    renderGrades();
    expect(await screen.findByText("Analyse des données")).toBeInTheDocument();
    expect(await screen.findByText("Pas encore soutenu")).toBeInTheDocument();
  });

  it("shows status badges", async () => {
    renderGrades();
    const completed = await screen.findAllByText("Complété");
    expect(completed.length).toBeGreaterThanOrEqual(1);
    const pending = await screen.findAllByText("En attente");
    expect(pending.length).toBeGreaterThanOrEqual(1);
  });

  it("shows individual scores", async () => {
    renderGrades();
    expect(await screen.findByText("Ahmed Benali")).toBeInTheDocument();
    expect(await screen.findByText("Fatima Amrani")).toBeInTheDocument();
  });

  it("shows empty state when no grades", async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    queryClient.setQueryData(["coordinator", "grades"], []);
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <Grades />
        </MemoryRouter>
      </QueryClientProvider>,
    );
    expect(await screen.findByText("Aucune évaluation disponible pour le moment.")).toBeInTheDocument();
  });
});
