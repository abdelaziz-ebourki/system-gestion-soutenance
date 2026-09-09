import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { CreateJuryDialog } from "@/components/coordinator/CreateJuryDialog";
import { useCoordinatorTeachersList, useProjects, useJuryRoleTemplates, useCreateJury, useUpdateJury } from "@/hooks/queries";
import { useEntityForm } from "@/hooks/use-entity-form";
import { validate } from "@/lib/validations";
import { toast } from "sonner";
import type { Teacher, Project, JuryRoleTemplate, Jury, PaginatedResponse } from "@/types";
import type { UseQueryResult, UseMutationResult } from "@tanstack/react-query";
import type { CreateJuryPayload, UpdateJuryPayload } from "@/lib/api-coordinator";

vi.mock("@/hooks/queries", () => ({
  useCoordinatorTeachersList: vi.fn(),
  useProjects: vi.fn(),
  useJuryRoleTemplates: vi.fn(),
  useCreateJury: vi.fn(),
  useUpdateJury: vi.fn(),
}));

vi.mock("@/hooks/use-entity-form", () => ({
  useEntityForm: vi.fn(),
}));

vi.mock("@/lib/validations", () => ({
  validate: vi.fn(),
  jurySchema: {},
}));

vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const mockTeachers: PaginatedResponse<Teacher> = {
  items: [
    { id: 1, role: "teacher", departmentId: 1, firstName: "Teacher", lastName: "1", email: "t1@univ.ma", isActive: true },
    { id: 2, role: "teacher", departmentId: 1, firstName: "Teacher", lastName: "2", email: "t2@univ.ma", isActive: true },
    { id: 3, role: "teacher", departmentId: 1, firstName: "Teacher", lastName: "3", email: "t3@univ.ma", isActive: true },
  ],
  total: 3,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};
const mockProjects: PaginatedResponse<Project> = {
  items: [
    { id: 1, title: "Project 1", description: "", defenseType: "pfe", groupId: 0, supervisorName: "T1", studentNames: [] },
  ],
  total: 1,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};
const mockTemplates: PaginatedResponse<JuryRoleTemplate> = {
  items: [
    {
      id: 1,
      name: "Standard Jury",
      defenseType: "pfe",
      roles: [
        { name: "Président", count: 1, coefficient: 1 },
        { name: "Examinateur", count: 1, coefficient: 1 },
      ],
    },
  ],
  total: 1,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

describe("CreateJuryDialog", () => {
  const defaultProps = {
    open: true,
    onOpenChange: vi.fn(),
    onSuccess: vi.fn(),
    jury: null,
  };

  const mockForm = {
    formData: { projectId: "", templateId: "", members: [] as { roleName: string; teacherId: string }[] },
    setFormData: vi.fn(),
    fieldErrors: {},
    setFieldErrors: vi.fn(),
    resetForm: vi.fn(),
    validateForm: vi.fn().mockReturnValue(true),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useCoordinatorTeachersList).mockReturnValue({ data: mockTeachers, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Teacher>, Error>);
    vi.mocked(useProjects).mockReturnValue({ data: mockProjects, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    vi.mocked(useJuryRoleTemplates).mockReturnValue({ data: mockTemplates, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<JuryRoleTemplate>, Error>);
    vi.mocked(useEntityForm).mockReturnValue(mockForm as unknown as ReturnType<typeof useEntityForm>);
    vi.mocked(validate).mockReturnValue(null);
    vi.mocked(useCreateJury).mockReturnValue({ mutateAsync: vi.fn().mockResolvedValue({}), isPending: false } as unknown as UseMutationResult<Jury, Error, CreateJuryPayload, unknown>);
    vi.mocked(useUpdateJury).mockReturnValue({ mutateAsync: vi.fn().mockResolvedValue({}), isPending: false } as unknown as UseMutationResult<Jury, Error, { id: number; data: UpdateJuryPayload }, unknown>);
  });

  it("renders header and project selection", () => {
    render(<CreateJuryDialog {...defaultProps} />);
    expect(screen.getByText("Nouveau jury")).toBeInTheDocument();
    expect(screen.getByTestId("coord-jury-create-project")).toBeInTheDocument();
  });

  it("shows template select and slots after project is selected", () => {
    mockForm.formData = { projectId: "1", templateId: "", members: [] };
    render(<CreateJuryDialog {...defaultProps} />);
    
    expect(screen.getByTestId("coord-jury-create-template")).toBeInTheDocument();
    
    mockForm.formData = { projectId: "1", templateId: "1", members: [] };
    render(<CreateJuryDialog {...defaultProps} />);
    
    expect(screen.getByTestId("coord-jury-create-slot-0")).toBeInTheDocument();
    expect(screen.getByTestId("coord-jury-create-slot-1")).toBeInTheDocument();
  });

  it("filters teachers to prevent duplicate assignments in the same jury", () => {
    mockForm.formData = { 
      projectId: "1", 
      templateId: "1", 
      members: [
        { roleName: "Président", teacherId: "1" },
        { roleName: "Examinateur", teacherId: "" },
      ] 
    };
    
    render(<CreateJuryDialog {...defaultProps} />);
  });

  it("creates a jury successfully", async () => {
    const createMutation = { mutateAsync: vi.fn().mockResolvedValue({}), isPending: false };
    vi.mocked(useCreateJury).mockReturnValue(createMutation as unknown as UseMutationResult<Jury, Error, CreateJuryPayload, unknown>);
    
    mockForm.formData = { 
      projectId: "1", 
      templateId: "1", 
      members: [
        { roleName: "Président", teacherId: "1" },
        { roleName: "Examinateur", teacherId: "1" },
      ] 
    };

    render(<CreateJuryDialog {...defaultProps} />);
    
    const submitBtn = screen.getByTestId("coord-jury-create-submit");
    fireEvent.click(submitBtn);
    
    await waitFor(() => {
      expect(createMutation.mutateAsync).toHaveBeenCalledWith({
        projectId: 1,
        members: [
          { roleName: "Président", teacherId: 1 },
          { roleName: "Examinateur", teacherId: 1 },
        ],
      });
      expect(toast.success).toHaveBeenCalledWith("Jury créé avec succès");
      expect(defaultProps.onSuccess).toHaveBeenCalled();
      expect(defaultProps.onOpenChange).toHaveBeenCalledWith(false);
    });
  });

  it("updates a jury successfully", async () => {
    const jury: Jury = {
      id: 1,
      projectId: 1,
      projectTitle: "Project 1",
      defenseType: "pfe",
      members: [
        { roleName: "Président", teacherId: 1, teacherName: "Teacher 1" },
        { roleName: "Examinateur", teacherId: 1, teacherName: "Teacher 2" },
      ],
    };
    const updateMutation = { mutateAsync: vi.fn().mockResolvedValue({}), isPending: false };
    vi.mocked(useUpdateJury).mockReturnValue(updateMutation as unknown as UseMutationResult<Jury, Error, { id: number; data: UpdateJuryPayload }, unknown>);
    
    mockForm.formData = { 
      projectId: "1", 
      templateId: "1", 
      members: [
        { roleName: "Président", teacherId: "1" },
        { roleName: "Examinateur", teacherId: "1" },
      ] 
    };

    render(<CreateJuryDialog {...defaultProps} jury={jury} />);
    
    const submitBtn = screen.getByTestId("coord-jury-create-submit");
    fireEvent.click(submitBtn);
    
    await waitFor(() => {
      expect(updateMutation.mutateAsync).toHaveBeenCalledWith({
        id: 1,
        data: {
          projectId: 1,
          members: [
            { roleName: "Président", teacherId: 1 },
            { roleName: "Examinateur", teacherId: 1 },
          ],
        },
      });
      expect(toast.success).toHaveBeenCalledWith("Jury modifié avec succès");
      expect(defaultProps.onSuccess).toHaveBeenCalled();
      expect(defaultProps.onOpenChange).toHaveBeenCalledWith(false);
    });
  });
});

