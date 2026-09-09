import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import JurySidebar from "@/components/coordinator/JurySidebar";
import type { Jury } from "@/types";

vi.mock("@/components/coordinator/DraggableJurySlot", () => ({
  default: ({ jury }: { jury: Jury }) => (
    <div data-testid={`mock-jury-slot-${jury.id}`}>{jury.projectTitle}</div>
  ),
}));

const mockJuries: Jury[] = [
  {
    id: 1,
    projectId: 1,
    projectTitle: "Project 1",
    members: [],
    defenseType: "pfe",
  },
  {
    id: 2,
    projectId: 2,
    projectTitle: "Project 2",
    members: [],
    defenseType: "pfe",
  },
];

describe("JurySidebar", () => {
  const defaultProps = {
    juries: mockJuries,
    searchQuery: "",
    onSearchChange: vi.fn(),
  };

  it("renders header and jury count", () => {
    render(<JurySidebar {...defaultProps} />);
    expect(screen.getByTestId("coord-designer-jury-sidebar")).toBeInTheDocument();
    expect(screen.getByTestId("coord-designer-jury-count")).toHaveTextContent("2");
  });

  it("renders search input and triggers onSearchChange", () => {
    render(<JurySidebar {...defaultProps} />);
    const input = screen.getByTestId("coord-designer-jury-search");
    fireEvent.change(input, { target: { value: "Project 1" } });
    expect(defaultProps.onSearchChange).toHaveBeenCalledWith("Project 1");
  });

  it("renders list of juries", () => {
    render(<JurySidebar {...defaultProps} />);
    expect(screen.getByTestId("mock-jury-slot-1")).toBeInTheDocument();
    expect(screen.getByTestId("mock-jury-slot-2")).toBeInTheDocument();
    expect(screen.getByText("Project 1")).toBeInTheDocument();
    expect(screen.getByText("Project 2")).toBeInTheDocument();
  });

  it("renders empty state when no juries are provided", () => {
    render(<JurySidebar {...defaultProps} juries={[]} />);
    expect(screen.getByText("Aucun jury en attente")).toBeInTheDocument();
    expect(screen.queryByTestId("mock-jury-slot-1")).not.toBeInTheDocument();
  });
});
