import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import DroppableCalendarCell from "@/components/coordinator/DroppableCalendarCell";
import { useDroppable } from "@dnd-kit/core";
type UseDroppableReturn = ReturnType<typeof useDroppable>;
import type { Jury } from "@/types";

vi.mock("@dnd-kit/core", () => ({
  useDroppable: vi.fn(() => ({
    setNodeRef: vi.fn(),
    isOver: false,
  })),
}));

const mockJury: Jury = {
  id: 1,
  projectId: 1,
  projectTitle: "Project 1",
  members: [],
  defenseType: "pfe",
};

describe("DroppableCalendarCell", () => {
  it("renders empty cell correctly", () => {
    render(
      <table>
        <tbody>
          <tr>
            <DroppableCalendarCell id="cell-1" jury={null} studentNames={[]} onRemove={vi.fn()} />
          </tr>
        </tbody>
      </table>
    );
    const cell = screen.getByTestId("coord-cell-cell-1");
    expect(cell).toBeInTheDocument();
  });

  it("renders cell with jury correctly", () => {
    render(
      <table>
        <tbody>
          <tr>
            <DroppableCalendarCell id="cell-1" jury={mockJury} studentNames={[]} onRemove={vi.fn()} />
          </tr>
        </tbody>
      </table>
    );
    expect(screen.getByText("Project 1")).toBeInTheDocument();
    expect(screen.getByTestId("coord-cell-remove-cell-1")).toBeInTheDocument();
  });

  it("triggers onRemove when remove button is clicked", () => {
    const onRemove = vi.fn();
    render(
      <table>
        <tbody>
          <tr>
            <DroppableCalendarCell id="cell-1" jury={mockJury} studentNames={[]} onRemove={onRemove} />
          </tr>
        </tbody>
      </table>
    );
    
    fireEvent.click(screen.getByTestId("coord-cell-remove-cell-1"));
    expect(onRemove).toHaveBeenCalled();
  });

  it("applies highlight when isOver is true", () => {
    vi.mocked(useDroppable).mockReturnValueOnce({
      setNodeRef: vi.fn(),
      isOver: true,
    } as unknown as UseDroppableReturn);

    render(
      <table>
        <tbody>
          <tr>
            <DroppableCalendarCell id="cell-1" jury={null} studentNames={[]} onRemove={vi.fn()} />
          </tr>
        </tbody>
      </table>
    );
    const cell = screen.getByTestId("coord-cell-cell-1");
    expect(cell.className).toContain("bg-primary/10");
    expect(cell.className).toContain("ring-primary");
  });
});
