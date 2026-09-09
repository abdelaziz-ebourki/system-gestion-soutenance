import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { RoomSearchSelect } from "@/components/coordinator/RoomSearchSelect";
import type { Room } from "@/types";

vi.mock("@/components/ui/select", () => ({
  SimpleSelect: ({ label, options, value, onChange }: { label: string; options: Array<{ label: string; value: string }>; value: string | undefined; onChange: (val: string) => void }) => (
    <div>
      <label>{label}</label>
      <select 
        data-testid="mock-simple-select" 
        value={value || ""} 
        onChange={(e) => onChange(e.target.value)}
      >
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>
    </div>
  ),
}));

const mockRooms: Room[] = [
  { id: 1, name: "Salle A01", capacity: 30, departmentId: 1 },
  { id: 2, name: "Salle B02", capacity: 20, departmentId: 1 },
];

describe("RoomSearchSelect", () => {
  it("renders correctly with label", () => {
    render(<RoomSearchSelect rooms={mockRooms} value={null} onChange={vi.fn()} />);
    expect(screen.getByText("Salle")).toBeInTheDocument();
  });

  it("formats options correctly", () => {
    render(<RoomSearchSelect rooms={mockRooms} value={null} onChange={vi.fn()} />);
    expect(screen.getByText("Salle A01 (30 places)")).toBeInTheDocument();
    expect(screen.getByText("Salle B02 (20 places)")).toBeInTheDocument();
  });

  it("triggers onChange when selection changes", () => {
    const onChange = vi.fn();
    render(<RoomSearchSelect rooms={mockRooms} value={null} onChange={onChange} />);
    const select = screen.getByTestId("mock-simple-select");
    fireEvent.change(select, { target: { value: "1" } });
    expect(onChange).toHaveBeenCalledWith("1");
  });
});
