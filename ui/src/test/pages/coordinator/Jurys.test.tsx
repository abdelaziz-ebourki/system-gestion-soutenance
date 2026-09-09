import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Jurys from "@/pages/coordinator/Jurys";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

vi.mock("@/components/coordinator/CreateJuryDialog", () => ({
  CreateJuryDialog: ({ open }: { open: boolean }) => open ? <div data-testid="coord-jury-create-dialog" /> : null,
}));

vi.mock("@/components/admin/DeleteAlert", () => ({
  DeleteAlert: ({ isOpen, onDelete, isPending }: { isOpen: boolean; onDelete: () => void; isPending: boolean }) =>
    isOpen ? (
      <div data-testid="delete-alert">
        <p>Confirmation</p>
        <button data-testid="delete-alert-confirm" onClick={onDelete} disabled={isPending}>
          Confirmer
        </button>
      </div>
    ) : null,
}));

const mockJuries = [
  {
    id: 1,
    projectId: 1,
    projectTitle: "Application CI/CD",
    studentNames: ["Ahmed Benali", "Fatima Amrani"],
    members: [
      { teacherId: 1, teacherName: "Dr. Alami", roleName: "Président" },
      { teacherId: 1, teacherName: "Pr. Bennani", roleName: "Examinateur" },
    ],
  },
  {
    id: 2,
    projectId: 2,
    projectTitle: "IA pour la santé",
    studentNames: ["Mohammed"],
    members: [
      { teacherId: 1, teacherName: "Dr. Chafik", roleName: "Président" },
    ],
  },
];

const mockJuriesResponse = {
  items: mockJuries,
  total: mockJuries.length,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};
const mockProjects = [
  { id: 1, title: "Application CI/CD" },
  { id: 2, title: "IA pour la santé" },
  { id: 3, title: "Site e-commerce" },
];

const mockProjectsResponse = {
  items: mockProjects,
  total: mockProjects.length,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

const { mockUseJuries, mockUseProjects, mockUseDeleteJury } = vi.hoisted(() => ({
  mockUseJuries: vi.fn(() => ({ data: mockJuriesResponse, isLoading: false })),
  mockUseProjects: vi.fn(() => ({ data: mockProjectsResponse, isLoading: false })),
  mockUseDeleteJury: vi.fn(() => ({ isPending: false, mutateAsync: vi.fn() })),
}));

vi.mock("@/hooks/queries", () => ({
  useJuries: mockUseJuries,
  useProjects: mockUseProjects,
  useDeleteJury: mockUseDeleteJury,
}));

function renderJurys() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Jurys />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Jurys (Coordinator)", () => {
  beforeEach(() => {
    mockUseJuries.mockReset();
    mockUseProjects.mockReset();
    mockUseDeleteJury.mockReset();
    mockUseJuries.mockReturnValue({ data: mockJuriesResponse, isLoading: false });
    mockUseProjects.mockReturnValue({ data: mockProjectsResponse, isLoading: false });
    mockUseDeleteJury.mockReturnValue({ isPending: false, mutateAsync: vi.fn() });
  });

  it("renders the page title", () => {
    renderJurys();
    expect(screen.getByText("Gestion des Jurys")).toBeInTheDocument();
  });

  it("renders stat cards", async () => {
    renderJurys();
    expect(await screen.findByText("Total Jurys")).toBeInTheDocument();
    expect(await screen.findByText("Projets sans Jury")).toBeInTheDocument();
    expect(await screen.findByText("Vérification")).toBeInTheDocument();
  });

  it("displays juries data from API", async () => {
    renderJurys();
    expect(await screen.findByText("Application CI/CD")).toBeInTheDocument();
    expect(await screen.findByText("Dr. Alami")).toBeInTheDocument();
  });

  it("shows the add jury button", () => {
    renderJurys();
    expect(screen.getByText("Nouveau Jury")).toBeInTheDocument();
  });

  it("shows projects without jury count in stat card", async () => {
    renderJurys();
    expect(await screen.findByText("1")).toBeInTheDocument();
  });

  it("renders loading skeleton when data is loading", async () => {
    mockUseJuries.mockReturnValue({ data: { items: [], total: 0, pageCount: 1, currentPage: 0, size: 10 }, isLoading: true });
    mockUseProjects.mockReturnValue({ data: { items: [], total: 0, pageCount: 1, currentPage: 0, size: 10 }, isLoading: true });
    renderJurys();
    expect(screen.getByTestId("coord-juries-page")).toBeInTheDocument();
    const skeleton = document.querySelector('[data-slot="skeleton"]');
    expect(skeleton).toBeInTheDocument();
  });

  it("shows empty state when no juries exist", async () => {
    mockUseJuries.mockReturnValue({ data: { items: [], total: 0, pageCount: 1, currentPage: 0, size: 10 }, isLoading: false });
    mockUseProjects.mockReturnValue({ data: { items: [], total: 0, pageCount: 1, currentPage: 0, size: 10 }, isLoading: false });
    renderJurys();
    expect(await screen.findByText("Total Jurys")).toBeInTheDocument();
    const zeros = screen.getAllByText("0");
    expect(zeros.length).toBeGreaterThanOrEqual(1);
  });

  it("opens create dialog when add button is clicked", async () => {
    const user = userEvent.setup();
    renderJurys();
    const addBtn = await screen.findByTestId("coord-juries-add-button");
    await user.click(addBtn);
    expect(await screen.findByTestId("coord-jury-create-dialog")).toBeInTheDocument();
  });

  it("shows delete confirmation when delete is clicked", async () => {
    const user = userEvent.setup();
    renderJurys();
    const deleteBtns = await screen.findAllByText("Supprimer");
    await user.click(deleteBtns[0]);
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    expect(screen.getByText("Confirmation")).toBeInTheDocument();
  });

  it("calls delete mutation when delete is confirmed", async () => {
    const user = userEvent.setup();
    const deleteMutate = vi.fn();
    mockUseDeleteJury.mockReturnValue({ isPending: false, mutateAsync: deleteMutate });
    renderJurys();
    const deleteBtns = await screen.findAllByText("Supprimer");
    await user.click(deleteBtns[0]);
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(deleteMutate).toHaveBeenCalledWith(1);
    });
  });
});

