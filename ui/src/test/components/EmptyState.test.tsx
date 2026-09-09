import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { EmptyState } from "@/components/ui/empty-state";
import { AlertTriangle } from "lucide-react";

describe("EmptyState", () => {
  it("renders default variant with description", () => {
    render(<EmptyState description="Nothing here" />);
    expect(screen.getByText("Nothing here")).toBeInTheDocument();
  });

  it("renders with icon and title", () => {
    render(<EmptyState icon={AlertTriangle} title="Warning" description="No data" />);
    expect(screen.getByText("Warning")).toBeInTheDocument();
    expect(screen.getByText("No data")).toBeInTheDocument();
  });

  it("renders card variant", () => {
    const { container } = render(<EmptyState variant="card" description="Card content" />);
    expect(container.querySelector(".rounded-lg.border.bg-secondary")).toBeInTheDocument();
  });

  it("renders dashed variant", () => {
    const { container } = render(<EmptyState variant="dashed" description="Dashed content" />);
    expect(container.querySelector(".border-dashed")).toBeInTheDocument();
  });

  it("renders action element", () => {
    render(<EmptyState description="desc" action={<button>Click</button>} />);
    expect(screen.getByRole("button", { name: "Click" })).toBeInTheDocument();
  });
});
