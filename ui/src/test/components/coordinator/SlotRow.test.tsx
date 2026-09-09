import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { SlotRow, type ScheduledCard } from "@/components/coordinator/SlotRow";
import type { Project, Jury } from "@/types";

vi.mock("@dnd-kit/core", () => ({
  useDroppable: vi.fn(() => ({
    setNodeRef: vi.fn(),
    isOver: false,
  })),
}));

const mockProjects: Project[] = [
  {
    id: 1,
    title: "Project 1",
    description: "Description",
    studentNames: ["Student A", "Student B"],
    supervisorName: "T1",
    defenseType: "pfe",
    groupId: 1,
  },
];

const mockJuries: Jury[] = [
  {
    id: 1,
    projectId: 1,
    projectTitle: "Project 1",
    defenseType: "pfe",
    members: [
      { teacherId: 1, roleName: "Président", teacherName: "Teacher 1" },
      { teacherId: 1, roleName: "Examinateur", teacherName: "Teacher 2" },
    ],
  },
];

describe("SlotRow", () => {
  const createProps = () => ({
    slotKey: "2025-06-16-09:00",
    time: "09:00 - 10:00",
    scheduled: undefined,
    projects: mockProjects,
    juries: mockJuries,
    mode: "dnd" as const,
    selectedProjectId: null,
    onPlace: vi.fn(),
    onRemove: vi.fn(),
  });

  it("renders free slot correctly", () => {
    const props = createProps();
    render(<SlotRow {...props} />);
    expect(screen.getByTestId("coord-slot-2025-06-16-09:00")).toBeInTheDocument();
    expect(screen.getByTestId("coord-slot-time-2025-06-16-09:00")).toHaveTextContent("09:00 - 10:00");
    expect(screen.getByTestId("coord-slot-status-2025-06-16-09:00")).toHaveTextContent("Libre");
  });

  it("renders occupied slot correctly", () => {
    const props = createProps();
    const scheduled: ScheduledCard = {
      id: 1,
      title: "Project 1",
      roomName: "Room A",
      date: "2025-06-16",
      time: "09:00 - 10:00",
    };
    render(<SlotRow {...props} scheduled={scheduled} />);
    
    expect(screen.getByTestId("coord-slot-status-2025-06-16-09:00")).toHaveTextContent("Occupé");
    expect(screen.getByText("Project 1")).toBeInTheDocument();
    expect(screen.getByText("Student A, Student B")).toBeInTheDocument();
    expect(screen.getByText("Président: Teacher 1 | Examinateur: Teacher 2")).toBeInTheDocument();
  });

  it("triggers onPlace in click mode when project is selected", () => {
    const props = createProps();
    render(
      <SlotRow 
        {...props} 
        mode="click" 
        selectedProjectId={1} 
      />
    );
    
    fireEvent.click(screen.getByTestId("coord-slot-2025-06-16-09:00"));
    expect(props.onPlace).toHaveBeenCalledWith("2025-06-16-09:00");
  });

  it("does not trigger onPlace in click mode when no project is selected", () => {
    const props = createProps();
    render(
      <SlotRow 
        {...props} 
        mode="click" 
        selectedProjectId={null} 
      />
    );
    
    fireEvent.click(screen.getByTestId("coord-slot-2025-06-16-09:00"));
    expect(props.onPlace).not.toHaveBeenCalled();
  });

  it("triggers onRemove when remove button is clicked", () => {
    const props = createProps();
    const scheduled: ScheduledCard = {
      id: 1,
      title: "Project 1",
      roomName: "Room A",
      date: "2025-06-16",
      time: "09:00 - 10:00",
    };
    render(<SlotRow {...props} scheduled={scheduled} />);
    
    fireEvent.click(screen.getByTestId("coord-slot-remove-2025-06-16-09:00"));
    expect(props.onRemove).toHaveBeenCalledWith("2025-06-16-09:00");
  });

  it("applies click highlight when in click mode and project selected", () => {
    const props = createProps();
    render(
      <SlotRow 
        {...props} 
        mode="click" 
        selectedProjectId={1} 
      />
    );
    
    const element = screen.getByTestId("coord-slot-2025-06-16-09:00");
    expect(element.className).toContain("cursor-pointer");
    expect(element.className).toContain("border-primary");
  });
});
