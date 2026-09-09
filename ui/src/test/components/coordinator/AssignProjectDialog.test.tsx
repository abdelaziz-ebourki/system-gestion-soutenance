import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { AssignProjectDialog } from "@/components/coordinator/AssignProjectDialog";
import { useProjects, useGroups, useAssignProjectToGroup } from "@/hooks/queries";
import { toast } from "sonner";
import type { Project, Group, PaginatedResponse } from "@/types";
import type { UseQueryResult, UseMutationResult } from "@tanstack/react-query";

vi.mock("@/hooks/queries", () => ({
  useProjects: vi.fn(),
  useGroups: vi.fn(),
  useAssignProjectToGroup: vi.fn(),
}));

vi.mock("sonner", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock("@/components/ui/select", () => ({
  Select: ({ children, value, onValueChange }: { children: React.ReactNode; value: string; onValueChange: (val: string) => void }) => (
    <select 
      data-testid="mock-assign-project-select" 
      value={value} 
      onChange={(e) => onValueChange(e.target.value)}
    >
      {children}
    </select>
  ),
  SelectTrigger: () => null,
  SelectValue: () => null,
  SelectContent: ({ children }: { children: React.ReactNode }) => children,
  SelectGroup: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  SelectItem: ({ children, value }: { children: React.ReactNode; value: string }) => <option value={value}>{children}</option>,
}));

const mockGroup: Group = {
  id: 1,
  groupName: "Groupe 1",
  projectId: 0,
  memberCount: 2,
  studentNames: ["Student A", "Student B"],
};

const mockProjects: PaginatedResponse<Project> = {
  items: [
    { id: 1, title: "Project 1", description: "", defenseType: "pfe", groupId: 2, supervisorName: "T1", studentNames: [] },
    { id: 2, title: "Project 2", description: "", defenseType: "pfe", groupId: 0, supervisorName: "T1", studentNames: [] },
  ],
  total: 2,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

const mockGroups: PaginatedResponse<Group> = {
  items: [
    { id: 2, projectId: 1, groupName: "Group 2", memberCount: 0, studentNames: [] },
  ],
  total: 1,
  pageCount: 1,
  currentPage: 0,
  size: 10,
};

describe("AssignProjectDialog", () => {
  const defaultProps = {
    group: mockGroup,
    open: true,
    onOpenChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useProjects).mockReturnValue({ data: mockProjects, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    vi.mocked(useGroups).mockReturnValue({ data: mockGroups, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Group>, Error>);
    vi.mocked(useAssignProjectToGroup).mockReturnValue({ 
      mutateAsync: vi.fn().mockResolvedValue({}), 
      isPending: false,
    } as unknown as UseMutationResult<Group, Error, { projectId: number; groupId: number }, unknown>);
  });

  it("renders group information", () => {
    render(<AssignProjectDialog {...defaultProps} />);
    expect(screen.getByText("Groupe :")).toBeInTheDocument();
    expect(screen.getByText("Groupe 1")).toBeInTheDocument();
    expect(screen.getByText("Student A, Student B")).toBeInTheDocument();
  });

  it("renders only available projects", () => {
    render(<AssignProjectDialog {...defaultProps} />);
    expect(screen.queryByText("Project 1")).not.toBeInTheDocument();
    expect(screen.getByText("Project 2")).toBeInTheDocument();
  });

  it("shows empty state when no projects are available", () => {
    vi.mocked(useProjects).mockReturnValue({ data: { items: [], total: 0, pageCount: 0, currentPage: 0, size: 10 }, isLoading: false } as unknown as UseQueryResult<PaginatedResponse<Project>, Error>);
    render(<AssignProjectDialog {...defaultProps} />);
    expect(screen.getByText("Aucun projet disponible. Créez d'abord un projet.")).toBeInTheDocument();
  });

  it("successfully assigns a project", async () => {
    const assignMutation = { mutateAsync: vi.fn().mockResolvedValue({}), isPending: false };
    vi.mocked(useAssignProjectToGroup).mockReturnValue(assignMutation as unknown as UseMutationResult<Group, Error, { projectId: number; groupId: number }, unknown>);

    render(<AssignProjectDialog {...defaultProps} />);
    
    const select = screen.getByTestId("mock-assign-project-select");
    fireEvent.change(select, { target: { value: "2" } });
    
    const submitBtn = screen.getByTestId("coord-assign-project-submit");
    fireEvent.click(submitBtn);
    
    await waitFor(() => {
      expect(assignMutation.mutateAsync).toHaveBeenCalledWith({ 
        projectId: 2, 
        groupId: 1 
      });
      expect(toast.success).toHaveBeenCalledWith('Projet assigné au groupe "Groupe 1"');
      expect(defaultProps.onOpenChange).toHaveBeenCalledWith(false);
    });
  });

  it("disables submit button when no project is selected", () => {
    render(<AssignProjectDialog {...defaultProps} />);
    const submitBtn = screen.getByTestId("coord-assign-project-submit");
    expect(submitBtn).toBeDisabled();
  });

  it("handles mutation error", async () => {
    const assignMutation = { mutateAsync: vi.fn().mockRejectedValue(new Error("API Error")), isPending: false };
    vi.mocked(useAssignProjectToGroup).mockReturnValue(assignMutation as unknown as UseMutationResult<Group, Error, { projectId: number; groupId: number }, unknown>);

    render(<AssignProjectDialog {...defaultProps} />);
    
    const select = screen.getByTestId("mock-assign-project-select");
    fireEvent.change(select, { target: { value: "2" } });
    
    const submitBtn = screen.getByTestId("coord-assign-project-submit");
    fireEvent.click(submitBtn);
    
    await waitFor(() => {
      expect(assignMutation.mutateAsync).toHaveBeenCalled();
    });
  });
});

