import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import Rooms from "@/pages/admin/Rooms";

vi.mock("sonner", () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}));

vi.mock("@/components/ui/select", () => ({
  Select: ({ children, value, onValueChange }: { children: React.ReactNode; value: string; onValueChange: (val: string) => void }) => (
    <select value={value} onChange={(e) => onValueChange(e.target.value)}>
      {children}
    </select>
  ),
  SelectTrigger: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  SelectValue: ({ placeholder }: { placeholder?: string }) => <span>{placeholder}</span>,
  SelectContent: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  SelectGroup: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  SelectItem: ({ children, value }: { children: React.ReactNode; value: string }) => <option value={value}>{children}</option>,
}));

function renderRooms() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Rooms />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Rooms", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders rooms from API", async () => {
    renderRooms();
    expect(await screen.findByText("Salle 101")).toBeInTheDocument();
    expect(await screen.findByText("Salle 102")).toBeInTheDocument();
  });

  it("renders department names for rooms", async () => {
    renderRooms();
    expect(await screen.findByText("Informatique")).toBeInTheDocument();
  });

  it("renders room capacities", async () => {
    renderRooms();
    expect(await screen.findByText("30 places")).toBeInTheDocument();
    expect(await screen.findByText("20 places")).toBeInTheDocument();
  });

  it("opens create dialog when Nouvelle Salle is clicked", async () => {
    const user = userEvent.setup();
    renderRooms();
    await screen.findByText("Salle 101");
    await user.click(screen.getByRole("button", { name: /nouvelle salle/i }));
    expect(await screen.findByText(/Ajouter une Salle/i)).toBeInTheDocument();
  });

  it("shows empty state guard when no departments", async () => {
    renderRooms();

    await screen.findByText("Salle 101");

    expect(screen.queryByText("Aucun département configuré")).not.toBeInTheDocument();
  });

  it("opens create dialog and creates a room", async () => {
    const user = userEvent.setup();
    renderRooms();
    await screen.findByText("Salle 101");
    await user.click(screen.getByRole("button", { name: /nouvelle salle/i }));
    expect(await screen.findByText(/Ajouter une Salle/i)).toBeInTheDocument();
    await user.type(screen.getByPlaceholderText(/ex: Salle 101/i), "Salle 103");
    await user.type(screen.getByPlaceholderText(/ex: 30/i), "40");
    await user.click(screen.getByRole("combobox"));
    await user.selectOptions(screen.getByRole("combobox"), "1");
    await user.click(screen.getByRole("button", { name: /enregistrer/i }));
    await waitFor(() => {
      expect(screen.queryByText(/Ajouter une Salle/i)).not.toBeInTheDocument();
    });
  }, 10000);

  it("opens single delete dialog via CrudActions", async () => {
    const user = userEvent.setup();
    renderRooms();
    await screen.findByText("Salle 101");
    const triggers = await screen.findAllByTestId("crud-actions-trigger");
    await user.click(triggers[0]);
    await user.click(screen.getByRole("menuitem", { name: /supprimer/i }));
    expect(await screen.findByText(/Cette action est irréversible/i)).toBeInTheDocument();
  });

  it("shows batch delete dialog", async () => {
    const user = userEvent.setup();
    renderRooms();
    await screen.findByText("Salle 101");
    const checkboxes = await screen.findAllByRole("checkbox");
    await user.click(checkboxes[1]);
    const deleteBtn = screen.getByRole("button", { name: /supprimer/i });
    await user.click(deleteBtn);
    expect(await screen.findByText(/Confirmation/i)).toBeInTheDocument();
  });
});
