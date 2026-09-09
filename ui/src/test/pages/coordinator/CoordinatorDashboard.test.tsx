import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import CoordinatorDashboard from "@/pages/coordinator/CoordinatorDashboard";

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
        <CoordinatorDashboard />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("CoordinatorDashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page after data loads", async () => {
    renderDashboard();
    expect(await screen.findByTestId("coord-dashboard-page")).toBeInTheDocument();
  });

  it("renders navigation links", async () => {
    renderDashboard();
    expect(await screen.findByText("Ouvrir le planificateur")).toBeInTheDocument();
    expect(await screen.findByText("Verifier les jurys")).toBeInTheDocument();
  });

  it("renders stat cards from API", async () => {
    renderDashboard();
    expect(await screen.findByText("Projets")).toBeInTheDocument();
    expect(await screen.findByText("Groupes")).toBeInTheDocument();
  });

  it("renders stat values from coordinator/stats", async () => {
    renderDashboard();
    expect(await screen.findByText("12")).toBeInTheDocument();
    expect(await screen.findByText("8")).toBeInTheDocument();
  });

  it("renders the preparation status section", async () => {
    renderDashboard();
    expect(await screen.findByText(/État de preparation/)).toBeInTheDocument();
    expect(await screen.findByText("Couverture des jurys")).toBeInTheDocument();
  });

  it("renders quick access section", async () => {
    renderDashboard();
    expect(await screen.findByTestId("coord-dashboard-quick-access")).toBeInTheDocument();
  });

  it("renders attention points section", async () => {
    renderDashboard();
    expect(await screen.findByTestId("coord-dashboard-attention-points")).toBeInTheDocument();
  });

  it("shows jury coverage bar", async () => {
    renderDashboard();
    expect(await screen.findByTestId("coord-dashboard-jury-coverage")).toBeInTheDocument();
  });

  it("shows ready projects and projects without jury", async () => {
    renderDashboard();
    expect(await screen.findByText("Prêts")).toBeInTheDocument();
    expect(await screen.findByText(/À completer/)).toBeInTheDocument();
  });

  it("shows projects without jury in attention points", async () => {
    renderDashboard();
    expect(await screen.findByText("Analyse des données")).toBeInTheDocument();
  });
});
