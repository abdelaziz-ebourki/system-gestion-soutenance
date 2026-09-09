import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { StatsCard } from "@/components/ui/stats-card";
import { Users } from "lucide-react";

describe("StatsCard", () => {
  it("renders label and value", () => {
    render(<StatsCard label="Students" value={42} icon={Users} />);
    expect(screen.getByText("Students")).toBeInTheDocument();
    expect(screen.getByText("42")).toBeInTheDocument();
  });

  it("renders skeleton when loading", () => {
    render(<StatsCard label="Students" value={42} icon={Users} loading />);
    expect(screen.getByText("Students")).toBeInTheDocument();
    expect(screen.queryByText("42")).not.toBeInTheDocument();
  });

  it("renders skeleton when value is null", () => {
    render(<StatsCard label="Students" value={null} icon={Users} />);
    expect(screen.getByText("Students")).toBeInTheDocument();
  });

  it("renders skeleton when value is undefined", () => {
    render(<StatsCard label="Students" value={undefined} icon={Users} />);
    expect(screen.getByText("Students")).toBeInTheDocument();
  });
});
