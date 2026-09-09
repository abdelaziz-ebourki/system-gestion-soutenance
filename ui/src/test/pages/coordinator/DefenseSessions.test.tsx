import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import CoordinatorDefenseSessions from "@/pages/coordinator/DefenseSessions";
import type { UseQueryResult, UseMutationResult } from "@tanstack/react-query";
import type { DefenseSession, JuryRoleTemplate, PaginatedResponse } from "@/types";

type CreateDefenseSessionPayload = {
  name: string;
  defenseType: string;
  status: string;
  maxGroupSize: number;
  defenseDuration: number;
  breakDuration: number;
  submissionDeadline: string;
  juryRoleTemplateId: number;
  startDate: string;
  endDate: string;
  evaluationCoefficients: Record<string, number>;
};

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

const mockSessions = [
  { id: 1, name: "Session PFE 2025", defenseType: "pfe", status: "draft", maxGroupSize: 3, defenseDuration: 30, breakDuration: 15, submissionDeadline: "2025-06-01", startDate: "2025-06-15", endDate: "2025-06-30", evaluationCoefficients: {}, juryRoleTemplateId: 1 },
  { id: 2, name: "Session Mémoires", defenseType: "memoire", status: "active", maxGroupSize: 2, defenseDuration: 20, breakDuration: 10, submissionDeadline: "2025-05-15", startDate: "2025-06-01", endDate: "2025-06-14", evaluationCoefficients: {}, juryRoleTemplateId: 1 },
  { id: 3, name: "Archived Session", defenseType: "these", status: "archived", maxGroupSize: 1, defenseDuration: 45, breakDuration: 20, submissionDeadline: "2025-04-01", startDate: "2025-04-15", endDate: "2025-04-30", evaluationCoefficients: {}, juryRoleTemplateId: 1 },
];

const mockTemplates = [
  { id: 1, name: "Standard", defenseType: "pfe", roles: [{ name: "Président", count: 1, coefficient: 40 }, { name: "Examinateur", count: 1, coefficient: 60 }] },
];

const mockSessionsResponse = {
  items: mockSessions,
  total: mockSessions.length,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};
const mockTemplatesResponse = {
  items: mockTemplates,
  total: mockTemplates.length,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

vi.mock("@/hooks/queries", () => ({
  useCoordinatorDefenseSessions: vi.fn(),
  useTransitionDefenseSession: vi.fn(),
  useCreateDefenseSession: vi.fn(),
  useUpdateDefenseSession: vi.fn(),
  useDeleteDefenseSession: vi.fn(),
  useJuryRoleTemplates: vi.fn(),
}));

function createMutateMock() {
  return {
    isPending: false,
    mutateAsync: vi.fn(),
  };
}

function renderSessions() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <CoordinatorDefenseSessions />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("DefenseSessions (Coordinator)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading skeleton", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: true } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: { items: mockTemplates, total: 1, pageCount: 1, currentPage: 0, size: 10 } } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    const { container } = renderSessions();
    expect(container.querySelector('[data-slot="skeleton"]')).toBeInTheDocument();
  });

  it("renders empty state when no sessions", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: { items: mockTemplates, total: 1, pageCount: 1, currentPage: 0, size: 10 } } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    expect(await screen.findByText("Aucune session de soutenance pour le moment.")).toBeInTheDocument();
  });

  it("renders session cards", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    expect(await screen.findByText("Sessions de soutenance")).toBeInTheDocument();
    expect(screen.getByTestId("coord-sessions-page")).toBeInTheDocument();
    expect(screen.getByText("Session PFE 2025")).toBeInTheDocument();
    expect(screen.getByText("Session Mémoires")).toBeInTheDocument();
  });

  it("renders new session button", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    expect(await screen.findByTestId("coord-sessions-add-button")).toBeInTheDocument();
  });

  it("shows create dialog when add button is clicked", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const addBtn = await screen.findByTestId("coord-sessions-add-button");
    await user.click(addBtn);
    expect(screen.getByTestId("coord-sessions-dialog")).toBeInTheDocument();
  });

  it("shows status badges for each session", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    expect(await screen.findByText("Brouillon")).toBeInTheDocument();
    const activeElements = screen.getAllByText("Active");
    expect(activeElements.length).toBeGreaterThanOrEqual(1);
  });

  it("renders transition buttons for active sessions", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const transitionButtons = await screen.findAllByText(/Planifiée|Programmée/);
    expect(transitionButtons.length).toBeGreaterThanOrEqual(1);
  });

  it("opens edit dialog with pre-filled form", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /modifier/i }));
    expect(screen.getByTestId("coord-sessions-dialog")).toBeInTheDocument();
    const nameInput = screen.getByTestId("coord-sessions-input-name") as HTMLInputElement;
    expect(nameInput.value).toBe("Session PFE 2025");
  });

  it("creates a session via dialog submit", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    const createMutate = vi.fn().mockResolvedValue(undefined);
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue({ isPending: false, mutateAsync: createMutate } as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const addBtn = await screen.findByTestId("coord-sessions-add-button");
    await user.click(addBtn);
    fireEvent.change(screen.getByTestId("coord-sessions-input-name"), { target: { value: "Nouvelle Session" } });
    fireEvent.change(screen.getByTestId("coord-sessions-input-deadline"), { target: { value: "2025-06-01" } });
    fireEvent.change(screen.getByTestId("coord-sessions-input-start"), { target: { value: "2025-07-01" } });
    fireEvent.change(screen.getByTestId("coord-sessions-input-end"), { target: { value: "2025-07-15" } });
    await user.click(screen.getByTestId("coord-sessions-input-template"));
    await user.click(screen.getByRole("option", { name: "Standard" }));
    fireEvent.submit(screen.getByTestId("coord-sessions-dialog").querySelector("form")!);
    await waitFor(() => {
      expect(createMutate).toHaveBeenCalled();
    });
  });

  it("shows date validation error when endDate precedes startDate", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    const createMutate = vi.fn();
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue({ isPending: false, mutateAsync: createMutate } as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const addBtn = await screen.findByTestId("coord-sessions-add-button");
    await user.click(addBtn);
    fireEvent.change(screen.getByTestId("coord-sessions-input-start"), { target: { value: "2025-06-30" } });
    fireEvent.change(screen.getByTestId("coord-sessions-input-end"), { target: { value: "2025-06-15" } });
    fireEvent.submit(screen.getByTestId("coord-sessions-dialog").querySelector("form")!);
    await waitFor(() => {
      expect(createMutate).not.toHaveBeenCalled();
    });
  });

  it("deletes a session via CrudActions", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    const deleteMutate = vi.fn();
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue({ isPending: false, mutateAsync: deleteMutate } as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /supprimer/i }));
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(deleteMutate).toHaveBeenCalledWith(1);
    });
  });

  it("calls transition mutation when transition button is clicked", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    const transitionMutate = vi.fn();
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue({ isPending: false, mutateAsync: transitionMutate } as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const planifieeBtn = await screen.findByText("Planifiée");
    await user.click(planifieeBtn);
    await waitFor(() => {
      expect(transitionMutate).toHaveBeenCalledWith({ id: 2, toStatus: "scheduled" });
    });
  });

  it("shows archived text for archived sessions", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    expect(await screen.findByText(/Session archivée/i)).toBeInTheDocument();
    expect(screen.getByText("Archivée")).toBeInTheDocument();
  });

  it("renders session details in info boxes", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    expect(await screen.findByText((content) => content.includes("30 min par passage"))).toBeInTheDocument();
    expect(screen.getByText("3 étudiants")).toBeInTheDocument();
  });

  it("selects jury template and shows evaluation coefficients", async () => {
    const user = userEvent.setup({ pointerEventsCheck: 0 });
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const addBtn = await screen.findByTestId("coord-sessions-add-button");
    await user.click(addBtn);
    expect(screen.getByText("Sélectionnez un modèle de jury")).toBeInTheDocument();
    fireEvent.click(screen.getByTestId("coord-sessions-input-template"));
    const option = await screen.findByRole("option", { name: /standard/i });
    fireEvent.click(option);
    expect(await screen.findByText("Président: 40%")).toBeInTheDocument();
    expect(screen.getByText("Examinateur: 60%")).toBeInTheDocument();
  });

  it("closes dialog via cancel button", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const addBtn = await screen.findByTestId("coord-sessions-add-button");
    await user.click(addBtn);
    expect(screen.getByTestId("coord-sessions-dialog")).toBeInTheDocument();
    await user.click(screen.getByTestId("coord-sessions-dialog-cancel"));
    await waitFor(() => {
      expect(screen.queryByTestId("coord-sessions-dialog")).not.toBeInTheDocument();
    });
  });

  it("shows loading state on submit button during creation", async () => {
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue({ isPending: true, mutateAsync: vi.fn() } as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const addBtn = await screen.findByTestId("coord-sessions-add-button");
    await userEvent.setup().click(addBtn);
    const submitBtn = screen.getByTestId("coord-sessions-dialog-submit");
    expect(submitBtn).toHaveAttribute("disabled");
  });

  it("closes delete alert via cancel", async () => {
    const user = userEvent.setup();
    const queries = await import("@/hooks/queries");
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /supprimer/i }));
    expect(await screen.findByTestId("delete-alert")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /annuler/i }));
    await waitFor(() => {
      expect(screen.queryByTestId("delete-alert")).not.toBeInTheDocument();
    });
  });

  it("shows error toast when delete fails", async () => {
    const user = userEvent.setup();
    const toast = await import("sonner");
    const queries = await import("@/hooks/queries");
    const deleteMutate = vi.fn().mockRejectedValue(new Error("fail"));
    vi.mocked(queries.useCoordinatorDefenseSessions).mockReturnValue({ data: mockSessionsResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<DefenseSession>, Error>);
    vi.mocked(queries.useJuryRoleTemplates).mockReturnValue({ data: mockTemplatesResponse } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(queries.useTransitionDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; toStatus: string }, unknown>);
    vi.mocked(queries.useCreateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, CreateDefenseSessionPayload, unknown>);
    vi.mocked(queries.useUpdateDefenseSession).mockReturnValue(createMutateMock() as unknown as UseMutationResult<DefenseSession, Error, { id: number; data: CreateDefenseSessionPayload }, unknown>);
    vi.mocked(queries.useDeleteDefenseSession).mockReturnValue({ isPending: false, mutateAsync: deleteMutate } as unknown as UseMutationResult<void, Error, number, unknown>);
    renderSessions();
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /supprimer/i }));
    await user.click(screen.getByTestId("delete-alert-confirm"));
    await waitFor(() => {
      expect(toast.toast.error).toHaveBeenCalledWith("fail");
    });
  });
});
