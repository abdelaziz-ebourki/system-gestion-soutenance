import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import DraggableJurySlot from "@/components/coordinator/DraggableJurySlot";
import { useDraggable } from "@dnd-kit/core";
type UseDraggableReturn = ReturnType<typeof useDraggable>;
import { CSS } from "@dnd-kit/utilities";
import type { Jury } from "@/types";
import type { ReactNode } from "react";

vi.mock("@dnd-kit/core", () => ({
  useDraggable: vi.fn(() => ({
    attributes: {},
    listeners: {},
    setNodeRef: vi.fn(),
    transform: null,
    isDragging: false,
  } as unknown as UseDraggableReturn)),
}));

vi.mock("@dnd-kit/utilities", () => ({
  CSS: {
    Translate: {
      toString: vi.fn(),
    },
  },
}));

vi.mock("@/hooks/queries", () => ({
  useProjects: vi.fn(() => ({
    data: {
      items: [
        { id: 1, title: "Project 1", description: "", defenseType: "pfe", groupId: 1, supervisorName: "Supervisor", studentNames: ["Student A", "Student B"] },
      ],
      total: 1,
      pageCount: 1,
      currentPage: 0,
      size: 10,
    },
    isLoading: false,
  })),
}));

const mockJury: Jury = {
  id: 1,
  projectId: 1,
  projectTitle: "Project 1",
  members: [
    { teacherId: 1, roleName: "Président", teacherName: "Teacher 1" },
    { teacherId: 2, roleName: "Examinateur", teacherName: "Teacher 2" },
  ],
  defenseType: "pfe",
};

function renderWithProviders(ui: ReactNode) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={qc}>{ui}</QueryClientProvider>);
}

describe("DraggableJurySlot", () => {
  it("renders jury information correctly", () => {
    renderWithProviders(<DraggableJurySlot jury={mockJury} />);
    
    expect(screen.getByTestId("coord-jury-slot-1")).toBeInTheDocument();
    expect(screen.getByTestId("coord-jury-slot-title-1")).toHaveTextContent("Project 1");
    expect(screen.getByTestId("coord-jury-slot-students-1")).toHaveTextContent("Student A, Student B");
    expect(screen.getByTestId("coord-jury-slot-member-1-1")).toHaveTextContent("Teacher 1");
    expect(screen.getByTestId("coord-jury-slot-member-1-2")).toHaveTextContent("Teacher 2");
  });

  it("applies dragging styles when isDragging is true", () => {
    vi.mocked(useDraggable).mockReturnValueOnce({
      attributes: {},
      listeners: {},
      setNodeRef: vi.fn(),
      transform: null,
      isDragging: true,
    } as unknown as UseDraggableReturn);

    renderWithProviders(<DraggableJurySlot jury={mockJury} />);
    const element = screen.getByTestId("coord-jury-slot-1");
    expect(element.className).toContain("opacity-30");
  });

  it("applies overlay styles and hides grip handle when isOverlay is true", () => {
    renderWithProviders(<DraggableJurySlot jury={mockJury} isOverlay />);
    const element = screen.getByTestId("coord-jury-slot-1");
    expect(element.className).toContain("shadow-lg");
    expect(element.className).not.toContain("cursor-grab");
    
    expect(element.querySelector(".lucide-grip-vertical")).toBeNull();
  });

  it("applies transform style when transform is provided", () => {
    vi.mocked(useDraggable).mockReturnValueOnce({
      attributes: {},
      listeners: {},
      setNodeRef: vi.fn(),
      transform: { x: 10, y: 20, scaleX: 1, scaleY: 1 },
      isDragging: false,
    } as unknown as UseDraggableReturn);

    vi.mocked(CSS.Translate.toString).mockReturnValue("translate3d(10px, 20px, 0)");

    renderWithProviders(<DraggableJurySlot jury={mockJury} />);
    const element = screen.getByTestId("coord-jury-slot-1");
    expect(element.style.transform).toBe("translate3d(10px, 20px, 0)");
  });
});
