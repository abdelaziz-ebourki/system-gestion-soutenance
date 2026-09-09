import { render, screen, fireEvent, within } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import TeacherEvaluations from "@/pages/teacher/TeacherEvaluations";
import { useTeacherEvaluations } from "@/hooks/queries";
import { useEvaluationForm } from "@/hooks/use-evaluation-form";
import type { UseQueryResult } from "@tanstack/react-query";
import type { TeacherEvaluation, PaginatedResponse } from "@/types";

vi.mock("@/hooks/queries", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/hooks/queries")>();
  return {
    ...actual,
    useTeacherEvaluations: vi.fn(),
  };
});

vi.mock("@/hooks/use-evaluation-form", () => ({
  useEvaluationForm: vi.fn(),
}));

const mockEvaluations: PaginatedResponse<TeacherEvaluation> = {
  items: [
    {
      id: 1,
      projectId: 1,
      projectTitle: "Application CI/CD",
      finalGrade: 0,
      comment: "",
      status: "pending",
    },
    {
      id: 2,
      projectId: 2,
      projectTitle: "IA pour la santé",
      finalGrade: 15,
      comment: "Bon travail",
      status: "submitted",
    },
  ],
  total: 2,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

function renderEvaluations() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <TeacherEvaluations />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("TeacherEvaluations", () => {
  const mockForm = {
    isDialogOpen: false,
    setIsDialogOpen: vi.fn(),
    selected: null,
    formData: { score: 0, comment: "" },
    setFormData: vi.fn(),
    openEdit: vi.fn(),
    handleSubmit: vi.fn(),
    isPending: false,
    fieldErrors: {},
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useEvaluationForm).mockReturnValue(mockForm as unknown as ReturnType<typeof useEvaluationForm>);
  });

  it("renders loading state", async () => {
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: null, isLoading: true } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderEvaluations();
    expect(screen.getByTestId("teacher-evaluations-header")).toBeInTheDocument();
    expect(screen.queryByTestId(/teacher-evaluations-pending-item-/)).not.toBeInTheDocument();
  });

  it("renders header and description", async () => {
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderEvaluations();
    expect(screen.getByTestId("teacher-evaluations-header")).toHaveTextContent("Évaluations");
    expect(screen.getByTestId("teacher-evaluations-description")).toHaveTextContent("Gérez les notes et les appréciations des soutenances.");
  });

  it("calculates and renders stats correctly", async () => {
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderEvaluations();
    
    expect(screen.getByTestId("teacher-evaluations-stats-pending")).toHaveTextContent("1");
    expect(screen.getByTestId("teacher-evaluations-stats-submitted")).toHaveTextContent("1");
    expect(screen.getByTestId("teacher-evaluations-stats-comments")).toHaveTextContent("1");
  });

  it("renders pending evaluations list", async () => {
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderEvaluations();
    
    expect(screen.getByTestId("teacher-evaluations-pending-item-1")).toBeInTheDocument();
    expect(screen.getByText("Application CI/CD")).toBeInTheDocument();
    
    const btn = screen.getByTestId("teacher-evaluations-pending-btn-1");
    fireEvent.click(btn);
    expect(mockForm.openEdit).toHaveBeenCalledWith(expect.objectContaining({ id: 1 }));
  });

  it("renders submitted evaluations list", async () => {
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderEvaluations();
    
    expect(screen.getByTestId("teacher-evaluations-submitted-item-2")).toBeInTheDocument();
    expect(screen.getByText("IA pour la santé")).toBeInTheDocument();
    expect(screen.getByText("Note: 15/20")).toBeInTheDocument();
    expect(screen.getByText("Bon travail")).toBeInTheDocument();
  });

  it("shows empty state for submitted evaluations", async () => {
    const onlyPending = { ...mockEvaluations, items: [mockEvaluations.items[0]] };
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: onlyPending, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    renderEvaluations();
    
    expect(screen.getByText("Aucune évaluation soumise.")).toBeInTheDocument();
  });

  it("renders evaluation dialog when open", async () => {
    vi.mocked(useEvaluationForm).mockReturnValue({
      ...mockForm,
      isDialogOpen: true,
       selected: mockEvaluations.items[0],
    } as unknown as ReturnType<typeof useEvaluationForm>);
    
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    
    renderEvaluations();
    
    const dialog = screen.getByTestId("teacher-evaluations-dialog");
    expect(dialog).toBeInTheDocument();
    expect(screen.getByTestId("teacher-evaluations-dialog-title")).toHaveTextContent("Compléter une évaluation");
    expect(within(dialog).getByText("Application CI/CD")).toBeInTheDocument();
  });

  it("handles form input and submission", async () => {
    vi.mocked(useEvaluationForm).mockReturnValue({
      ...mockForm,
      isDialogOpen: true,
       selected: mockEvaluations.items[0],
    } as unknown as ReturnType<typeof useEvaluationForm>);
    
    vi.mocked(useTeacherEvaluations).mockReturnValue({ data: mockEvaluations, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<TeacherEvaluation>, Error>);
    
    renderEvaluations();
    
    const scoreInput = screen.getByTestId("teacher-evaluations-score");
    const commentInput = screen.getByTestId("teacher-evaluations-comment");
    const form = screen.getByTestId("teacher-evaluations-form");
    
    fireEvent.change(scoreInput, { target: { value: "18" } });
    expect(mockForm.setFormData).toHaveBeenCalledWith(expect.objectContaining({ score: 18 }));
    
    fireEvent.change(commentInput, { target: { value: "Excellent travail" } });
    expect(mockForm.setFormData).toHaveBeenCalledWith(expect.objectContaining({ comment: "Excellent travail" }));
    
    fireEvent.submit(form);
    expect(mockForm.handleSubmit).toHaveBeenCalled();
  });
});

