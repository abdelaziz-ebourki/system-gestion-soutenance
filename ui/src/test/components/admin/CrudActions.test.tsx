import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { CrudActions } from "@/components/admin/CrudActions";
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

describe("CrudActions", () => {
  const mockEntity = { id: 1, name: "Test Entity" };
  const onEdit = vi.fn();
  const onDelete = vi.fn();

  it("renders the trigger button", () => {
    renderWithProviders(<CrudActions entity={mockEntity} onEdit={onEdit} onDelete={onDelete} />);
    expect(screen.getByTestId("crud-actions-trigger")).toBeInTheDocument();
  });

  it("opens the menu and calls onEdit", async () => {
    const user = userEvent.setup();
    renderWithProviders(<CrudActions entity={mockEntity} onEdit={onEdit} onDelete={onDelete} />);
    await user.click(screen.getByTestId("crud-actions-trigger"));
    
    const editItem = await screen.findByTestId("crud-actions-edit");
    await user.click(editItem);
    
    expect(onEdit).toHaveBeenCalledWith(mockEntity);
  });

  it("opens the menu and calls onDelete", async () => {
    const user = userEvent.setup();
    renderWithProviders(<CrudActions entity={mockEntity} onEdit={onEdit} onDelete={onDelete} />);
    await user.click(screen.getByTestId("crud-actions-trigger"));
    
    const deleteItem = await screen.findByTestId("crud-actions-delete");
    await user.click(deleteItem);
    
    expect(onDelete).toHaveBeenCalledWith(mockEntity);
  });
});
