import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { ModeToggle } from "@/components/coordinator/ModeToggle";

describe("ModeToggle", () => {
  it("renders with default value", () => {
    render(<ModeToggle value="click" onChange={() => {}} />);
    expect(screen.getByTestId("coord-toggle-mode")).toBeInTheDocument();
  });

  it("calls onChange when clicking a different mode", () => {
    const onChange = vi.fn();
    render(<ModeToggle value="click" onChange={onChange} />);
    fireEvent.click(screen.getByTestId("coord-toggle-dnd"));
    expect(onChange).toHaveBeenCalledWith("dnd");
  });
});
