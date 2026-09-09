import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { DeleteAlert } from "@/components/admin/DeleteAlert";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";

function renderWithProviders(ui: React.ReactElement) {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={qc}>
      <BrowserRouter>
        {ui}
      </BrowserRouter>
    </QueryClientProvider>
  );
}

describe("DeleteAlert", () => {
  it("renders correctly when open", () => {
    renderWithProviders(<DeleteAlert isOpen={true} onOpenChange={vi.fn()} onDelete={vi.fn()} isPending={false} entityName="Test" />);
    expect(screen.getByTestId("delete-alert")).toBeInTheDocument();
    expect(screen.getByText("Confirmation")).toBeInTheDocument();
    expect(screen.getByText(/L'élément "Test" sera définitivement supprimé/)).toBeInTheDocument();
  });

  it("calls onDelete when confirm is clicked", () => {
    const onDelete = vi.fn();
    renderWithProviders(<DeleteAlert isOpen={true} onOpenChange={vi.fn()} onDelete={onDelete} isPending={false} />);
    fireEvent.click(screen.getByTestId("delete-alert-confirm"));
    expect(onDelete).toHaveBeenCalled();
  });

  it("calls onOpenChange when cancel is clicked", () => {
    const onOpenChange = vi.fn();
    renderWithProviders(<DeleteAlert isOpen={true} onOpenChange={onOpenChange} onDelete={vi.fn()} isPending={false} />);
    fireEvent.click(screen.getByText("Annuler"));
    expect(onOpenChange).toHaveBeenCalledWith(false);
  });

  it("shows custom title and description", () => {
    renderWithProviders(
      <DeleteAlert 
        isOpen={true} 
        onOpenChange={vi.fn()} 
        onDelete={vi.fn()} 
        isPending={false} 
        title="Custom Title" 
        description="Custom Description" 
      />
    );
    expect(screen.getByText("Custom Title")).toBeInTheDocument();
    expect(screen.getByText("Custom Description")).toBeInTheDocument();
  });
});
