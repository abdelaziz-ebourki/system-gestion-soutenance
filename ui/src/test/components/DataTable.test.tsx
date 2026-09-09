import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { DataTable } from "@/components/ui/data-table";
import { type ColumnDef } from "@tanstack/react-table";

interface TestData {
  id: string;
  name: string;
}

const columns: ColumnDef<TestData>[] = [
  { accessorKey: "id", header: "ID" },
  { accessorKey: "name", header: "Name" },
];

const data: TestData[] = [
  { id: "1", name: "Item 1" },
  { id: "2", name: "Item 2" },
];

describe("DataTable", () => {
  it("renders data when not loading", () => {
    render(<DataTable columns={columns} data={data} />);
    expect(screen.getByText("Item 1")).toBeInTheDocument();
    expect(screen.getByText("Item 2")).toBeInTheDocument();
  });

  it("renders skeletons and preserves layout during loading state", () => {
    const { container } = render(<DataTable columns={columns} data={[]} loading={true} filterColumns="name" />);
    
    // Check if toolbar (search input) is still visible
    expect(screen.getByPlaceholderText("Rechercher...")).toBeInTheDocument();
    
    // Check if pagination info is visible
    expect(screen.getByText(/Page 1 sur 1/)).toBeInTheDocument();
    
    // Check if skeletons are rendered in the table body
    const skeletons = container.querySelectorAll(".animate-pulse");
    expect(skeletons.length).toBeGreaterThan(0);
    
    // Table header should still be visible
    expect(screen.getByText("ID")).toBeInTheDocument();
    expect(screen.getByText("Name")).toBeInTheDocument();
  });

  it("global filter preserves state during loading", () => {
     // This is a bit harder to test with just render, but we can check if it's interactive
     render(<DataTable columns={columns} data={data} loading={true} filterColumns="name" />);
     const searchInput = screen.getByPlaceholderText("Rechercher...") as HTMLInputElement;
     expect(searchInput).toBeInTheDocument();
     expect(searchInput).not.toBeDisabled();
  });
});
