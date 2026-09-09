import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Coordinators from "@/pages/admin/users/Coordinators";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

function renderCoordinators() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Coordinators />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Coordinators", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title", () => {
    renderCoordinators();
    expect(screen.getByText("Coordinateurs")).toBeInTheDocument();
  });

  it("renders coordinators from API", async () => {
    renderCoordinators();
    expect(await screen.findByText("Idrissi")).toBeInTheDocument();
    expect(await screen.findByText("Hassan")).toBeInTheDocument();
    expect(await screen.findByText("El Fassi")).toBeInTheDocument();
    expect(await screen.findByText("Nadia")).toBeInTheDocument();
  });

  it("renders coordinator emails", async () => {
    renderCoordinators();
    expect(await screen.findByText("hassan.idrissi@example.com")).toBeInTheDocument();
    expect(await screen.findByText("nadia.elfassi@example.com")).toBeInTheDocument();
  });

  it("renders active/inactive badges", async () => {
    renderCoordinators();
    expect(await screen.findByText("Actif")).toBeInTheDocument();
    expect(await screen.findByText("Inactif")).toBeInTheDocument();
  });

  it("opens create dialog when Nouveau Coordinateur is clicked", async () => {
    const user = userEvent.setup();
    renderCoordinators();
    await user.click(screen.getByRole("button", { name: /nouveau coordinateur/i }));
    expect(await screen.findByText(/Ajouter Coordinateur/i)).toBeInTheDocument();
  });
});
