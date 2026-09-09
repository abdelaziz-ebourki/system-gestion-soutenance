import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { DndContext, useDraggable } from "@dnd-kit/core";
import { ProjectList } from "@/components/coordinator/ProjectList";
import type { Project } from "@/types";

type UseDraggableReturn = ReturnType<typeof useDraggable>;

vi.mock(import("@dnd-kit/core"), async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useDraggable: () => ({
      attributes: {},
      listeners: {},
      setNodeRef: () => {},
      transform: null,
      isDragging: false,
    }) as unknown as UseDraggableReturn,
  };
});

const mockProjects: Project[] = [
  { id: 1, title: "Project Alpha", studentNames: ["Alice"], supervisorName: "Dr. X", description: "", groupId: 1, defenseType: "pfe" },
  { id: 2, title: "Project Beta", studentNames: ["Bob", "Carol"], supervisorName: "Dr. Y", description: "", groupId: 2, defenseType: "pfe" },
];

describe("ProjectList", () => {
  it("renders all projects", () => {
    render(
      <DndContext>
        <ProjectList projects={mockProjects} assignedProjectIds={new Set()} mode="click" selectedProjectId={null} onSelect={() => {}} />
      </DndContext>
    );
    expect(screen.getByTestId("coord-project-card-1")).toBeInTheDocument();
    expect(screen.getByTestId("coord-project-card-2")).toBeInTheDocument();
  });

  it("calls onSelect when clicking unassigned project in click mode", () => {
    const onSelect = vi.fn();
    render(
      <DndContext>
        <ProjectList projects={mockProjects} assignedProjectIds={new Set()} mode="click" selectedProjectId={null} onSelect={onSelect} />
      </DndContext>
    );
    fireEvent.click(screen.getByTestId("coord-project-card-1"));
    expect(onSelect).toHaveBeenCalledWith(1);
  });

  it("highlights selected project", () => {
    render(
      <DndContext>
        <ProjectList projects={mockProjects} assignedProjectIds={new Set()} mode="click" selectedProjectId={1} onSelect={() => {}} />
      </DndContext>
    );
    const card = screen.getByTestId("coord-project-card-1");
    expect(card.className).toContain("border-primary");
  });

  it("shows assigned projects with opacity", () => {
    render(
      <DndContext>
        <ProjectList projects={mockProjects} assignedProjectIds={new Set([1])} mode="click" selectedProjectId={null} onSelect={() => {}} />
      </DndContext>
    );
    const card = screen.getByTestId("coord-project-card-1");
    expect(card.className).toContain("opacity-50");
  });

  it("renders project title and student names", () => {
    render(
      <DndContext>
        <ProjectList projects={mockProjects} assignedProjectIds={new Set()} mode="click" selectedProjectId={null} onSelect={() => {}} />
      </DndContext>
    );
    expect(screen.getByTestId("coord-project-title-1")).toHaveTextContent("Project Alpha");
    expect(screen.getByTestId("coord-project-students-1")).toHaveTextContent("Alice");
  });
});
