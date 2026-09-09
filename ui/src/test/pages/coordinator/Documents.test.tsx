import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Documents from "@/pages/coordinator/Documents";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

beforeEach(() => {
  vi.stubGlobal("open", vi.fn());
});

afterEach(() => {
  vi.unstubAllGlobals();
});

function renderDocuments() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Documents />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Documents (Coordinator)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the page title", async () => {
    renderDocuments();
    expect(await screen.findByText("Génération de documents")).toBeInTheDocument();
  });

  it("renders all document type cards", async () => {
    renderDocuments();
    expect(await screen.findByText("Fiches d'évaluation")).toBeInTheDocument();
    expect(screen.getByText("Procès-Verbaux (PV)")).toBeInTheDocument();
    expect(screen.getByText("Listes de présence")).toBeInTheDocument();
    expect(screen.getByText("Convocations jury")).toBeInTheDocument();
    expect(screen.getByText("Planning des soutenances")).toBeInTheDocument();
  });

  it("opens picker dialog and shows projects for evaluation sheets", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const generateBtns = await screen.findAllByText("Générer");
    await user.click(generateBtns[0]);
    expect(await screen.findByTestId("coord-documents-picker-dialog")).toBeInTheDocument();
    expect(await screen.findByText("Application CI/CD")).toBeInTheDocument();
    expect(screen.getByText("Analyse des données")).toBeInTheDocument();
  });

  it("opens picker dialog and shows juries for convocations", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const convBtns = await screen.findAllByText("Générer");
    await user.click(convBtns[2]);
    expect(await screen.findByTestId("coord-documents-picker-dialog")).toBeInTheDocument();
    expect(await screen.findByText("Application CI/CD")).toBeInTheDocument();
  });

  it("filters projects in picker dialog", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const generateBtns = await screen.findAllByText("Générer");
    await user.click(generateBtns[0]);
    await screen.findByTestId("coord-documents-picker-dialog");
    await user.type(screen.getByTestId("coord-documents-picker-search"), "CI/CD");
    expect(screen.getByText("Application CI/CD")).toBeInTheDocument();
    expect(screen.queryByText("Analyse des données")).not.toBeInTheDocument();
  });

  it("shows generate planning button when sessions exist", async () => {
    renderDocuments();
    const btns = await screen.findAllByText("Générer");
    expect(btns.length).toBeGreaterThanOrEqual(3);
  });

  it("shows date picker dialog when attendance list is clicked", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const dateBtn = await screen.findByText("Choisir une date");
    await user.click(dateBtn);
    expect(screen.getByTestId("coord-documents-date-dialog")).toBeInTheDocument();
  });

  it("opens a new tab with blob URL when project is selected in picker", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const generateBtns = await screen.findAllByText("Générer");
    await user.click(generateBtns[0]);
    const projectBtn = await screen.findByText("Application CI/CD");
    await user.click(projectBtn);
    await waitFor(() => {
      expect(window.open).toHaveBeenCalled();
    });
  });

  it("opens a new tab with blob URL when generate planning is clicked", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const btns = await screen.findAllByText("Générer");
    const scheduleBtn = btns[btns.length - 1];
    await user.click(scheduleBtn);
    await waitFor(() => {
      expect(window.open).toHaveBeenCalled();
    });
  });

  it("opens a new tab with blob URL when date is selected and generate is clicked", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const dateBtn = await screen.findByText("Choisir une date");
    await user.click(dateBtn);
    expect(screen.getByTestId("coord-documents-date-dialog")).toBeInTheDocument();
    await user.click(screen.getByTestId("coord-documents-date-generate"));
    await waitFor(() => {
      expect(window.open).toHaveBeenCalled();
    });
  });

  it("cancels date dialog without generating", async () => {
    const user = userEvent.setup();
    renderDocuments();
    await user.click(await screen.findByText("Choisir une date"));
    expect(screen.getByTestId("coord-documents-date-dialog")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /annuler/i }));
    await waitFor(() => {
      expect(screen.queryByTestId("coord-documents-date-dialog")).not.toBeInTheDocument();
    });
  });

  it("cancels picker dialog without generating", async () => {
    const user = userEvent.setup();
    renderDocuments();
    const generateBtns = await screen.findAllByText("Générer");
    await user.click(generateBtns[0]);
    await screen.findByTestId("coord-documents-picker-dialog");
    await user.click(screen.getByTestId("coord-documents-picker-cancel"));
    await waitFor(() => {
      expect(screen.queryByTestId("coord-documents-picker-dialog")).not.toBeInTheDocument();
    });
  });
});
