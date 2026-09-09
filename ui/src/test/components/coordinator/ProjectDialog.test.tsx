import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { ProjectDialog } from "@/components/coordinator/ProjectDialog";
import { useCoordinatorTeachersList, useCoordinatorStudents, useCreateProject, useUpdateProject } from "@/hooks/queries";
import { useEntityForm } from "@/hooks/use-entity-form";
import { validate } from "@/lib/validations";
import { toast } from "sonner";
import type { Teacher, Project, Student } from "@/types";
import type { UseQueryResult, UseMutationResult } from "@tanstack/react-query";
import type { PaginatedResponse } from "@/types";
import type { CreateProjectPayload, UpdateProjectPayload } from "@/lib/api-coordinator";

vi.mock("@/hooks/queries", () => ({
  useCoordinatorTeachersList: vi.fn(),
  useCoordinatorStudents: vi.fn(),
  useCreateProject: vi.fn(),
  useUpdateProject: vi.fn(),
}));

vi.mock("@/hooks/use-entity-form", () => ({
  useEntityForm: vi.fn(),
}));

vi.mock("@/lib/validations", () => ({
  validate: vi.fn(),
  projectSchema: {},
}));

vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const mockTeachersResponse = {
  items: [
    { id: 1, role: "teacher", departmentId: 1, firstName: "Teacher", lastName: "1", email: "t1@univ.ma", isActive: true },
  ],
  total: 1,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};
const mockStudents = {
  items: [{ id: 1, firstName: "Student", lastName: "A", email: "s1@univ.ma", cne: "CNE1", majorId: 1, levelId: 1, isActive: true, role: "student" as const }],
  total: 1,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

describe("ProjectDialog", () => {
  const defaultProps = {
    open: true,
    onOpenChange: vi.fn(),
    onSuccess: vi.fn(),
    project: null,
  };

  const mockForm = {
    formData: { title: "", description: "", supervisorId: "", studentIds: [] as string[], defenseType: "pfe" },
    setFormData: vi.fn(),
    fieldErrors: {},
    setFieldErrors: vi.fn(),
    resetForm: vi.fn(),
    validateForm: vi.fn().mockReturnValue(true),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useCoordinatorTeachersList).mockReturnValue({ data: mockTeachersResponse, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Teacher>, Error>);
    vi.mocked(useCoordinatorStudents).mockReturnValue({ data: mockStudents, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Student>, Error>);
    vi.mocked(useEntityForm).mockReturnValue(mockForm as unknown as ReturnType<typeof useEntityForm>);
    vi.mocked(validate).mockReturnValue(null);
    vi.mocked(useCreateProject).mockReturnValue({ mutateAsync: vi.fn().mockResolvedValue({}), isPending: false } as unknown as UseMutationResult<Project, Error, CreateProjectPayload, unknown>);
    vi.mocked(useUpdateProject).mockReturnValue({ mutateAsync: vi.fn().mockResolvedValue({}), isPending: false } as unknown as UseMutationResult<Project, Error, { id: number; data: UpdateProjectPayload }, unknown>);
  });

  it("renders in create mode", () => {
    render(<ProjectDialog {...defaultProps} />);
    expect(screen.getByText("Nouveau projet")).toBeInTheDocument();
    expect(screen.getByText("Ajoutez un sujet et son encadrant.")).toBeInTheDocument();
  });

  it("renders in edit mode with pre-filled data", () => {
    const project: Project = {
      id: 1,
      title: "Existing Project",
      description: "Desc",
      groupId: 0,
      supervisorName: "Teacher 1",
      studentNames: [],
      defenseType: "pfe",
    };
    
    render(<ProjectDialog {...defaultProps} project={project} />);
    expect(screen.getByText("Modifier le projet")).toBeInTheDocument();
    expect(screen.getByText("Mettez à jour le sujet et l'encadrement.")).toBeInTheDocument();
  });

  it("shows validation errors when validate returns errors", async () => {
    vi.mocked(validate).mockReturnValue({ title: "Titre requis" });
    
    render(<ProjectDialog {...defaultProps} />);
    
    fireEvent.submit(screen.getByTestId("coord-project-dialog-form"));
    
    expect(mockForm.setFieldErrors).toHaveBeenCalledWith({ title: "Titre requis" });
  });

  it("creates a project successfully", async () => {
    const createMutation = { mutateAsync: vi.fn().mockResolvedValue({}), isPending: false };
    vi.mocked(useCreateProject).mockReturnValue(createMutation as unknown as UseMutationResult<Project, Error, CreateProjectPayload, unknown>);
    
    mockForm.formData = { 
      title: "New Project", 
      description: "Desc", 
      supervisorId: "1", 
      studentIds: ["1"], 
      defenseType: "pfe" 
    };

    render(<ProjectDialog {...defaultProps} />);
    
    const submitBtn = screen.getByTestId("coord-project-dialog-submit");
    fireEvent.click(submitBtn);
    
    await waitFor(() => {
      expect(createMutation.mutateAsync).toHaveBeenCalledWith({
        title: "New Project",
        description: "Desc",
        supervisorId: 1,
        defenseType: "pfe",
        studentIds: [1],
      });
      expect(toast.success).toHaveBeenCalledWith("Projet créé avec succès");
      expect(defaultProps.onSuccess).toHaveBeenCalled();
      expect(defaultProps.onOpenChange).toHaveBeenCalledWith(false);
    });
  });

  it("updates a project successfully", async () => {
    const project: Project = {
      id: 1,
      title: "Old Project",
      description: "Old Desc",
      groupId: 0,
      supervisorName: "Teacher 1",
      studentNames: [],
      defenseType: "pfe",
    };
    const updateMutation = { mutateAsync: vi.fn().mockResolvedValue({}), isPending: false };
    vi.mocked(useUpdateProject).mockReturnValue(updateMutation as unknown as UseMutationResult<Project, Error, { id: number; data: UpdateProjectPayload }, unknown>);
    
    mockForm.formData = { 
      title: "New Title", 
      description: "New Desc", 
      supervisorId: "1", 
      studentIds: ["1"], 
      defenseType: "pfe" 
    };

    render(<ProjectDialog {...defaultProps} project={project} />);
    
    const submitBtn = screen.getByTestId("coord-project-dialog-submit");
    fireEvent.click(submitBtn);
    
    await waitFor(() => {
      expect(updateMutation.mutateAsync).toHaveBeenCalledWith({
        id: 1,
        data: {
          title: "New Title",
          description: "New Desc",
          defenseType: "pfe",
        },
      });
      expect(toast.success).toHaveBeenCalledWith("Projet mis à jour");
      expect(defaultProps.onSuccess).toHaveBeenCalled();
      expect(defaultProps.onOpenChange).toHaveBeenCalledWith(false);
    });
  });

  it("handles mutation error", async () => {
    const createMutation = { mutateAsync: vi.fn().mockRejectedValue(new Error("API Error")), isPending: false };
    vi.mocked(useCreateProject).mockReturnValue(createMutation as unknown as UseMutationResult<Project, Error, CreateProjectPayload, unknown>);
    
    mockForm.formData = { 
      title: "New Project", 
      description: "Desc", 
      supervisorId: "1", 
      studentIds: ["1"], 
      defenseType: "pfe" 
    };

    render(<ProjectDialog {...defaultProps} />);
    
    const submitBtn = screen.getByTestId("coord-project-dialog-submit");
    fireEvent.click(submitBtn);
    
    await waitFor(() => {
      expect(createMutation.mutateAsync).toHaveBeenCalled();
    });
  });
});

